package main.java.foundation.web;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import main.java.foundation.auth.LoginFormData;
import main.java.foundation.auth.RegisterFormData;
import main.java.foundation.auth.TokenManager;
import main.java.foundation.database.PostgresFoundationDatabase;
import main.java.foundation.database.PostgresFoundationDatabaseTransaction;
import main.java.foundation.database.structure.*;
import main.java.foundation.map.MapImageColorizer;
import main.java.foundation.map.MapImageGetter;
import main.java.foundation.map.tomtom.TileGridUtils;
import main.java.observability.PerformanceUtils;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.Callback;
import org.infinispan.commons.dataconversion.internal.Json;
import org.mindrot.jbcrypt.BCrypt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EndpointController {
    private static final int MAX_PRESENCES_PER_MINUTE = 5;

    private MapImageGetter mapImageGetter;
    private MapImageColorizer mapImageColorizer;
    private PostgresFoundationDatabase dbController;
    private TokenManager tokenManager;
    private RegisterFormData registerFormData;

    public EndpointController(
            MapImageGetter mapImageGetter, PostgresFoundationDatabase dbController,
            TokenManager tokenManager) {
        this.mapImageGetter = mapImageGetter;
        this.mapImageColorizer = new MapImageColorizer(dbController);
        this.dbController = dbController;
        this.tokenManager = tokenManager;
    }

    public boolean handleMapTileImage(Request request, Response response, Callback callback) {
        Fields queryParams = null;
        try {
            queryParams = Request.getParameters(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int searchId = 0, x = 0, y = 0, z = 0;
        try {
            searchId = Integer.parseInt(queryParams.getValue("searchId"));

                x = Integer.parseInt(queryParams.getValue("x"));
                y = Integer.parseInt(queryParams.getValue("y"));
                z = Integer.parseInt(queryParams.getValue("z"));
        } catch (NumberFormatException e) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Bad request - invalid numeric value", callback);

            return true;
        }

        BufferedImage img = mapImageGetter.getMapTile(x, y, z);
        if (img == null) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get map image", callback);

            return true;
        }

        try {
            int finalSearchId = searchId;
            int finalZ = z;
            int finalX = x;
            int finalY = y;
            PerformanceUtils.logDuration(() -> {
                try {
                    mapImageColorizer.colorizeImage(img, TileGridUtils.tileZXYToLatLonBBox(finalZ, finalX, finalY),
                            3, 3, 0, finalSearchId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, "colorize image");

            ImageIO.write(img, "png", Content.Sink.asOutputStream(response));
            callback.succeeded();
            response.setStatus(200);

            return true;

        } catch (Exception e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not colorize image" + e.getMessage(), callback);

            return true;
        }
    }

    public boolean handleGetAdminSearchesMetadata(Request request, Response response, Callback callback) {
        String username;
        try {
            username = getActorUsernameFromHeader(request);
        } catch (JWTVerificationException e) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - invalid JWT: " + e.getMessage(), callback);

            callback.succeeded();
            return true;
        }

        List<SearchMetadata> searchMetadataList;
        try {
            searchMetadataList = dbController.getSearchesMetadataByUsername(username);
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get searches metadata: " + e.getMessage(), callback);

            return true;
        }

        Gson gson = new Gson();
        String json = gson.toJson(searchMetadataList);

        response.setStatus(200);
        response.getHeaders().put("Content-Type", "application/json");
        Content.Sink.write(response, true, json, callback);

        return true;
    }

    private String getAuthorizationHeaderValueBrowser(Request request) throws JWTVerificationException {
        String authHeader = request.getHeaders().get("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JWTVerificationException("Authorization bearer header is missing");
        }

        return authHeader.substring(8, authHeader.length() - 1); // Remove "Bearer "" prefix and " suffix
    }

    private String getAuthorizationHeaderValueMobile(Request request) throws JWTVerificationException {
        String authHeader = request.getHeaders().get("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JWTVerificationException("Authorization bearer header is missing");
        }

        return authHeader.substring(7); // Remove 'Bearer ' prefix
    }

    private String getActorUsernameFromHeader(Request request) {
        String jwt = getAuthorizationHeaderValueBrowser(request);
        return tokenManager.getUsernameFromToken(jwt);
    }

    private String getActorUsernameFromCookie(Request request) {
        List<HttpCookie> cookies = Request.getCookies(request);
        HttpCookie jwtCookie = cookies.stream()
                .filter(cookie -> "jwt".equals(cookie.getName()))
                .findFirst()
                .orElse(null);
        if (jwtCookie == null) {
            throw new JWTVerificationException("JWT cookie is missing");
        }

        return tokenManager.getUsernameFromToken(jwtCookie.getValue());
    }

    private TokenPair handleLoginLogic(Request request, Response response, Callback callback) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Content.Source.asInputStream(request)));
        Gson gson = new Gson();
        LoginFormData loginFormData = gson.fromJson(reader, LoginFormData.class);

        User user;
        String refreshToken, jwt;
        try (PostgresFoundationDatabaseTransaction tx = dbController.createTransaction()) {
            try {
                user = tx.getUserByUsername(loginFormData.username());
            } catch (SQLException e) {
                response.setStatus(500);
                Content.Sink.write(response, true, "Internal server error - could not get user: " + e.getMessage(), callback);

                tx.rollback();
                callback.succeeded();
                return null;
            }

            if (user == null) {
                response.setStatus(401);
                Content.Sink.write(response, true, "Username or passowrd is wrong", callback);

                tx.rollback();
                return null;
            }

            if (!BCrypt.checkpw(loginFormData.password(), user.passwordHash())) {
                response.setStatus(401);
                Content.Sink.write(response, true, "Username or passowrd is wrong", callback);

                tx.rollback();
                return null;
            }

            try {
                refreshToken = createRefreshTokenForUser(tx, user.username());
            } catch (SQLException e) {
                response.setStatus(500);
                Content.Sink.write(response, true, "Internal server error - could not create refresh token: " + e.getMessage(), callback);

                tx.rollback();
                return null;
            }
        } catch (Exception e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not create transaction: " + e.getMessage(), callback);
            return null;
        }

        jwt = tokenManager.generateToken(user.username());

        return new TokenPair(jwt, refreshToken);
    }

    public boolean handleLogin(Request request, Response response, Callback callback) {
        TokenPair tokenPair = handleLoginLogic(request, response, callback);
        if (tokenPair == null) return true;

        response.setStatus(200);
        setAuthCookies(response, tokenPair.jwt(), tokenPair.refreshToken());

        callback.succeeded();
        return true;
    }

    public boolean handleLoginMobile(Request request, Response response, Callback callback) {
        TokenPair tokenPair = handleLoginLogic(request, response, callback);
        if (tokenPair == null) return true;

        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("jwt", tokenPair.jwt());
        jsonResponse.addProperty("refresh_token", tokenPair.refreshToken());

        response.setStatus(200);
        response.getHeaders().put("Content-Type", "application/json");
        Content.Sink.write(response, true, gson.toJson(jsonResponse), callback);
        return true;
    }

    public boolean handleRegister(Request request, Response response, Callback callback) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Content.Source.asInputStream(request)));
        Gson gson = new Gson();
        RegisterFormData registerFormData = gson.fromJson(reader, RegisterFormData.class);
        if (registerFormData.username() == null || registerFormData.password() == null) {
            response.setStatus(400);
            Content.Sink.write(response, true, "Bad request - username and password are required", callback);

            return true;
        }

        String hashedPassword = BCrypt.hashpw(registerFormData.password(), BCrypt.gensalt());
        User newUser = new User(registerFormData.username(), hashedPassword,
                registerFormData.firstName(), registerFormData.lastName());

        String refreshToken, jwt;
        try (PostgresFoundationDatabaseTransaction tx = dbController.createTransaction()) {
            try {
                tx.createUser(newUser);
            } catch (SQLException e) {
                response.setStatus(500);
                Content.Sink.write(response, true, "Internal server error - could not add user: " + e.getMessage(), callback);

                tx.rollback();
                return true;
            }

            try {
                refreshToken = createRefreshTokenForUser(tx, newUser.username());
            } catch (SQLException e) {
                response.setStatus(500);
                Content.Sink.write(response, true, "Internal server error - could not create refresh token: " + e.getMessage(), callback);

                tx.rollback();
                return true;
            }
        } catch (Exception e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not register: " + e.getMessage(), callback);
            return true;
        }

        jwt = tokenManager.generateToken(newUser.username());

        response.setStatus(200);
        setAuthCookies(response, jwt, refreshToken);

        callback.succeeded();
        return true;
    }

    private String createRefreshTokenForUser(PostgresFoundationDatabaseTransaction tx, String username) throws SQLException {
        RefreshToken existingToken = tx.getRefreshTokenByUsername(username);
        if (existingToken != null && existingToken.expiresAt().before(Timestamp.from(Instant.now()))) {
            tx.deleteRefreshToken(existingToken.token());
            existingToken = null;
        }

        String refreshTokenValue;
        if (existingToken == null) {
            refreshTokenValue = UUID.randomUUID().toString();
            RefreshToken refreshToken = new RefreshToken(username, refreshTokenValue,
                    Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));

            tx.createRefreshToken(refreshToken);
        } else {
            refreshTokenValue = existingToken.token();
        }

        return refreshTokenValue;
    }

    public boolean handleRefreshJWT(Request request, Response response, Callback callback) {
        String refreshTokenValue;
        try {
            refreshTokenValue = getAuthorizationHeaderValueBrowser(request);
        } catch (JWTVerificationException e) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - " + e.getMessage(), callback);
            return true;
        }

        RefreshToken refreshToken = refreshJWTLogic(response, callback, refreshTokenValue);
        if (refreshToken == null) return true; // failed

        String jwt = tokenManager.generateToken(refreshToken.username());

        response.setStatus(200);
        setAuthCookies(response, jwt, refreshToken.token());

        callback.succeeded();
        return true;
    }

    public boolean handleRefreshJWTMobile(Request request, Response response, Callback callback) {
        String refreshTokenValue;
        try {
            refreshTokenValue = getAuthorizationHeaderValueMobile(request);
        } catch (JWTVerificationException e) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - " + e.getMessage(), callback);
            return true;
        }

        RefreshToken refreshToken = refreshJWTLogic(response, callback, refreshTokenValue);
        if (refreshToken == null) return true; // failed

        String jwt = tokenManager.generateToken(refreshToken.username());

        response.setStatus(200);
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("jwt", jwt);

        response.getHeaders().put("Content-Type", "application/json");
        Content.Sink.write(response, true, jsonResponse.toString(), callback);
        return true;
    }

    private RefreshToken refreshJWTLogic(Response response, Callback callback, String refreshTokenValue) {
        RefreshToken refreshToken;
        try (PostgresFoundationDatabaseTransaction tx = dbController.createTransaction()) {
            try {
                refreshToken = tx.getRefreshToken(refreshTokenValue);
            } catch (SQLException e) {
                response.setStatus(500);
                Content.Sink.write(response, true, "Internal server error - could not get refresh token: " + e.getMessage(), callback);

                tx.rollback();
                return null;
            }

            if (refreshToken == null) {
                response.setStatus(401);
                Content.Sink.write(response, true, "Unauthorized - invalid or missing refresh token", callback);

                tx.rollback();
                return null;
            }

            if (refreshToken.expiresAt().before(Timestamp.from(Instant.now()))) {
                try {
                    tx.deleteRefreshToken(refreshToken.token());
                } catch (SQLException e) {
                    response.setStatus(500);
                    Content.Sink.write(response, true, "Internal server error - could not delete expired refresh token: " + e.getMessage(), callback);

                    tx.rollback();
                    return null;
                }

                response.setStatus(401);
                Content.Sink.write(response, true, "Unauthorized - refresh token has expired", callback);

                return null;
            }
        } catch (Exception e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not create transaction: " + e.getMessage(), callback);
            return null;
        }

        return refreshToken;
    }

    public boolean handleCheckAuth(Request request, Response response, Callback callback) {
        List<HttpCookie> cookies = Request.getCookies(request);
        HttpCookie refreshTokenCookie = cookies.stream()
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .orElse(null);
        HttpCookie jwtCookie = cookies.stream()
                .filter(cookie -> "jwt".equals(cookie.getName()))
                .findFirst()
                .orElse(null);

        if (jwtCookie != null && tokenManager.verify(jwtCookie.getValue())) {
            response.setStatus(204);

            callback.succeeded();
            return true;
        }

        if (refreshTokenCookie == null) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - no refresh token provided", callback);

            callback.succeeded();
            return true;
        }

        String username;
        try {
            RefreshToken refreshToken = dbController.getRefreshToken(refreshTokenCookie.getValue());
            if (refreshToken == null || refreshToken.expiresAt().before(Timestamp.from(Instant.now()))) {
                response.setStatus(401);
                Content.Sink.write(response, true, "Unauthorized - invalid or expired refresh token", callback);
                return true;
            }

            username = refreshToken.username();
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get refresh token: " + e.getMessage(), callback);
            return true;
        }

        String jwt = tokenManager.generateToken(username);
        setAuthCookies(response, jwt, refreshTokenCookie.getValue());

        response.setStatus(200);

        callback.succeeded();
        return true;
    }

    public boolean handleWhoAmI(Request request, Response response, Callback callback) {
        List<HttpCookie> cookies = Request.getCookies(request);
        HttpCookie jwtCookie = cookies.stream()
                .filter(cookie -> "jwt".equals(cookie.getName()))
                .findFirst()
                .orElse(null);

        if (jwtCookie == null || !tokenManager.verify(jwtCookie.getValue())) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - no valid JWT provided", callback);

            callback.succeeded();
            return true;
        }

        String username;
        try {
            username = tokenManager.getUsernameFromToken(jwtCookie.getValue());
        } catch (Exception e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not verify JWT: " + e.getMessage(), callback);

            callback.succeeded();
            return true;
        }

        User user;
        try {
            user = dbController.getUserByUsername(username);
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get user: " + e.getMessage(), callback);

            return true;
        }

        response.setStatus(200);
        response.getHeaders().put("Content-Type", "application/json");
        Gson gson = new Gson();
        JsonObject maskedObj = gson.toJsonTree(user).getAsJsonObject();
        maskedObj.remove("password_hash"); // mask password hash
        String json = gson.toJson(maskedObj);

        Content.Sink.write(response, true, json, callback);

        callback.succeeded();
        return true;
    }

    public boolean handleLogout(Request request, Response response, Callback callback) {
        List<HttpCookie> cookies = Request.getCookies(request);
        HttpCookie refreshTokenCookie = cookies.stream()
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .orElse(null);
        if (refreshTokenCookie == null) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - no refresh token provided", callback);

            callback.succeeded();
            return true;
        }

        try {
            dbController.deleteRefreshToken(refreshTokenCookie.getValue());
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not delete refresh token: " + e.getMessage(), callback);
            return true;
        }

        setAuthCookies(response, "", ""); // unset all tokens

        response.setStatus(200);
        Content.Sink.write(response, true, "Logged out successfully", callback);

        callback.succeeded();
        return true;
    }

    private static void setAuthCookies(Response response, String jwt, String refreshToken) {
        String jwtCookieString = String.format(
                "%s=\"%s\"; Path=%s; Max-Age=%d; SameSite=None; Secure",
                "jwt", jwt, "/", 60 * 20
        );
        response.getHeaders().add("Set-Cookie", jwtCookieString);

        String cookieString = String.format(
                "%s=\"%s\"; Path=%s; Max-Age=%d; HttpOnly; SameSite=None; Secure",
                "refresh_token", refreshToken, "/", 60 * 60 * 24 * 7
        );
        response.getHeaders().add("Set-Cookie", cookieString);
    }

    public boolean handleUpdateSearch(Request request, Response response, Callback callback) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Content.Source.asInputStream(request)));
        Gson gson = new Gson();
        Search updatedSearch = gson.fromJson(reader, Search.class);

        List<HttpCookie> cookies = Request.getCookies(request);
        HttpCookie jwtCookie = cookies.stream()
                .filter(cookie -> "jwt".equals(cookie.getName()))
                .findFirst()
                .orElse(null);
        if (jwtCookie == null) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - no JWT provided", callback);

            callback.succeeded();
            return true;
        }

        String username;
        try {
            username = tokenManager.getUsernameFromToken(jwtCookie.getValue());
        } catch (JWTVerificationException e) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - invalid JWT: " + e.getMessage(), callback);

            callback.succeeded();
            return true;
        }

        try (PostgresFoundationDatabaseTransaction tx = dbController.createTransaction()) {
            Search existingSearch;
            try {
                existingSearch = tx.getSearchById(updatedSearch.id());
            } catch (SQLException e) {
                response.setStatus(500);
                Content.Sink.write(response, true, "Internal server error - could not get search: " + e.getMessage(), callback);

                tx.rollback();
                callback.succeeded();
                return true;
            }

            if (!username.equals(existingSearch.owner_username())) {
                response.setStatus(403);
                Content.Sink.write(response, true, "Forbidden - you are not the owner of this search", callback);

                tx.rollback();
                callback.succeeded();
                return true;
            }

            Search patchedSearch = new Search(
                existingSearch.id(),
                updatedSearch.title(),
                updatedSearch.description(),
                existingSearch.created_at(),
                existingSearch.owner_username()
            );

            try {
                tx.updateSearch(patchedSearch);
            } catch (SQLException e) {
                response.setStatus(500);
                Content.Sink.write(response, true, "Internal server error - could not update search metadata: " + e.getMessage(), callback);

                tx.rollback();
                callback.succeeded();
                return true;
            }

            response.setStatus(200);
            Content.Sink.write(response, true, "Search metadata updated successfully", callback);
        } catch (Exception e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not create transaction: " + e.getMessage(), callback);
        }

        callback.succeeded();
        return true;
    }

    public boolean handleGetSearchById(Request request, Response response, Callback callback) {
        int searchId;
        Fields queryParams = null;
        try {
            queryParams = Request.getParameters(request);
            searchId = Integer.parseInt(queryParams.getValue("id"));
        } catch (Exception e) {
            response.setStatus(400);
            Content.Sink.write(response, true, "Bad request - invalid search ID", callback);

            return true;
        }

        Search search;
        try {
            search = dbController.getSearchById(searchId);
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get search: " + e.getMessage(), callback);

            return true;
        }

        if (search == null) {
            response.setStatus(404);
            Content.Sink.write(response, true, "Not found - search with this ID does not exist", callback);

            return true;
        }

        Gson gson = new Gson();
        String json = gson.toJson(search);

        response.setStatus(200);
        response.getHeaders().put("Content-Type", "application/json");
        Content.Sink.write(response, true, json, callback);

        return true;
    }

    public boolean handleCreateSearch(Request request, Response response, Callback callback) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Content.Source.asInputStream(request)));
        Gson gson = new Gson();
        Search searchData = gson.fromJson(reader, Search.class);

        List<HttpCookie> cookies = Request.getCookies(request);
        HttpCookie jwtCookie = cookies.stream()
                .filter(cookie -> "jwt".equals(cookie.getName()))
                .findFirst()
                .orElse(null);
        if (jwtCookie == null) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - no JWT provided", callback);

            return true;
        }

        String username;
        try {
            username = tokenManager.getUsernameFromToken(jwtCookie.getValue());
        } catch (JWTVerificationException e) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - invalid JWT: " + e.getMessage(), callback);

            return true;
        }

        Search newSearch = new Search(
                0, // ID will be set by the database
                searchData.title(),
                searchData.description(),
                Timestamp.from(Instant.now()),
                username
        );

        try {
            dbController.createSearch(newSearch);
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not create search: " + e.getMessage(), callback);

            return true;
        }

        response.setStatus(201);
        callback.succeeded();
        return true;
    }

    public boolean handleDeleteSearch(Request request, Response response, Callback callback) {
        int searchId;
        Fields queryParams = null;
        try {
            queryParams = Request.getParameters(request);
            searchId = Integer.parseInt(queryParams.getValue("id"));
        } catch (Exception e) {
            response.setStatus(400);
            Content.Sink.write(response, true, "Bad request - invalid search ID", callback);

            return true;
        }

        String username;
        try {
            username = getActorUsernameFromHeader(request);
        } catch (JWTVerificationException e) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - invalid JWT: " + e.getMessage(), callback);

            return true;
        }

        try (PostgresFoundationDatabaseTransaction tx = dbController.createTransaction()) {
            Search search = tx.getSearchById(searchId);
            if (search == null) {
                response.setStatus(404);
                Content.Sink.write(response, true, "Not found - search with this ID does not exist", callback);

                return true;
            }
            if (!search.owner_username().equals(username)) {
                response.setStatus(403);
                Content.Sink.write(response, true, "Forbidden - you are not the owner of this search", callback);

                return true;
            }

            tx.deleteSearchPresenceAssociations(searchId);
            tx.deletePresencesWithoutSearch();
            tx.deleteSearch(searchId);
        } catch (Exception e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get search: " + e.getMessage(), callback);

            return true;
        }

        response.setStatus(204);
        callback.succeeded();
        return true;
    }

    public boolean handleGetSearchesMetadata(Request request, Response response, Callback callback) {
        List<SearchMetadata> searchMetadataList;
        try {
            searchMetadataList = dbController.getSearchesMetadata();
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get searches metadata: " + e.getMessage(), callback);

            return true;
        }

        Gson gson = new Gson();
        String json = gson.toJson(searchMetadataList);

        response.setStatus(200);
        response.getHeaders().put("Content-Type", "application/json");
        Content.Sink.write(response, true, json, callback);

        return true;
    }

    public boolean handleInjectPresences(Request request, Response response, Callback callback) {
        Gson gson = new Gson();
        BufferedReader reader = new BufferedReader(new InputStreamReader(Content.Source.asInputStream(request)));
        InjectPresencesRequest injectPresencesRequest = gson.fromJson(reader, InjectPresencesRequest.class);

        String username;
        try {
            username = tokenManager.getUsernameFromToken(injectPresencesRequest.jwt());
        } catch (JWTVerificationException e) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - invalid JWT: " + e.getMessage(), callback);
            return true;
        }

        Arrays.sort(injectPresencesRequest.presences(), Comparator.comparingLong(Presence::recordedAt));

        try (PostgresFoundationDatabaseTransaction tx = dbController.createTransaction()) {
            List<RateLimiterPresence> existingPresences = tx.getRateLimiterPresencesForUser(username);

            if (!existingPresences.isEmpty() &&
                    existingPresences.get(existingPresences.size() - 1).presence().timestamp() > injectPresencesRequest.presences()[0].recordedAt()) {
                response.setStatus(400);
                Content.Sink.write(response, true, "Bad request - presences are not in chronological order", callback);

                tx.rollback();
                return true;
            }

            ArrayList<main.java.foundation.database.structure.Presence> newPresences = new ArrayList<>(
                    Arrays.stream(injectPresencesRequest.presences()).map(p -> new main.java.foundation.database.structure.Presence(
                            p.recordedAt(), p.lat(), p.lon()
                    )).toList()
            );

            ArrayList<main.java.foundation.database.structure.Presence> allPresences = new ArrayList<>(existingPresences.stream().map(RateLimiterPresence::presence).toList());
            allPresences.addAll(newPresences);

            int ptr = 0;
            for (int i = 0; i < allPresences.size(); i++) {
                if (ptr < i) ptr = i;
                while (ptr + 1 < allPresences.size() && Duration.ofMillis(allPresences.get(ptr + 1).timestamp() - allPresences.get(i).timestamp()).toMinutes() < 1) {
                    ptr++;
                }

                if (ptr - i + 1 > MAX_PRESENCES_PER_MINUTE) {
                    response.setStatus(400);
                    Content.Sink.write(response, true, "Bad request - too many presences in a row for the same time", callback);

                    tx.rollback();
                    return true;
                }
                if (i + 1 < allPresences.size()
                        && Duration.ofMillis(allPresences.get(i + 1).timestamp() - allPresences.get(i).timestamp()).toMinutes() < 5
                        && allPresences.get(i).distanceTo(allPresences.get(i+1)) > 0.0001) {
                    response.setStatus(400);
                    Content.Sink.write(response, true, "Bad request - presences are too far from each other", callback);

                    tx.rollback();
                    return true;
                }
            }

            tx.deleteRateLimiterPresencesForUser(username);
            tx.insertRateLimiterPresences(username, newPresences);

            List<Long> presenceIds = tx.insertPresences(newPresences);
            tx.linkSearchesAndPresences(injectPresencesRequest.searchIds(), presenceIds);
        } catch (Exception e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not inject presences: " + e.getMessage(), callback);
            return true;
        }

        response.setStatus(204);
        callback.succeeded();
        return true;
    }
}

record Presence (
    @SerializedName("lat") double lat,
    @SerializedName("lon") double lon,
    @SerializedName("recorded_at") long recordedAt
) {}

record InjectPresencesRequest(
        @SerializedName("search_ids") int[] searchIds,
        @SerializedName("presences") Presence[] presences,
        @SerializedName("jwt") String jwt
) {}

record TokenPair (
        @SerializedName("jwt") String jwt,
        @SerializedName("refresh_token") String refreshToken
) {}