package foundation.web;

import com.google.gson.Gson;
import foundation.auth.LoginFormData;
import foundation.auth.RegisterFormData;
import foundation.auth.SuccessfulLoginResponse;
import foundation.auth.TokenManager;
import foundation.database.FoundationDatabaseController;
import foundation.database.structure.RefreshToken;
import foundation.database.structure.SearchMetadata;
import foundation.database.structure.User;
import foundation.map.MapImageColorizer;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.TileGridUtils;
import observability.PerformanceUtils;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.Callback;
import org.mindrot.jbcrypt.BCrypt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class EndpointController {
    private MapImageGetter mapImageGetter;
    private MapImageColorizer mapImageColorizer;
    private FoundationDatabaseController dbController;
    private TokenManager tokenManager;

    public EndpointController(
            MapImageGetter mapImageGetter, FoundationDatabaseController dbController,
            TokenManager tokenManager) {
        this.mapImageGetter = mapImageGetter;
        this.mapImageColorizer = new MapImageColorizer(dbController);
        this.dbController = dbController;
        this.tokenManager = tokenManager;
    }

    public boolean handleMapTileImage(Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(204);

            callback.succeeded();
            return true;
        }

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

    public boolean handleGetSearchesMetadata(Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(204);

            callback.succeeded();
            return true;
        }

        Gson gson = new Gson();

        List<SearchMetadata> searchMetadataList = null;
        try {
            searchMetadataList = dbController.getSearchesMetadata();
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get searches metadata: " + e.getMessage(), callback);

            return true;
        }

        String json = gson.toJson(searchMetadataList);

        response.setStatus(200);
        response.getHeaders().put("Content-Type", "application/json");
        Content.Sink.write(response, true, json, callback);

        return true;
    }

    public boolean handleLogin(Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(204);

            callback.succeeded();
            return true;
        }
        if (!request.getMethod().equals("POST")) {
            response.setStatus(405);
            Content.Sink.write(response, true, "Method not allowed - only POST is allowed", callback);

            callback.succeeded();
            return true;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(Content.Source.asInputStream(request)));

        Gson gson = new Gson();
        LoginFormData loginFormData = gson.fromJson(reader, LoginFormData.class);

        User user;
        try {
            user = dbController.getUserByUsername(loginFormData.username());
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get user: " + e.getMessage(), callback);

            callback.succeeded();
            return true;
        }

        if (user == null) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Username or passowrd is wrong", callback);

            callback.succeeded();
            return true;
        }

        if (!BCrypt.checkpw(loginFormData.password(), user.passwordHash())) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Username or passowrd is wrong", callback);

            callback.succeeded();
            return true;
        }

        createSuccessfulLoginResponse(user.username(), response, callback);

        callback.succeeded();
        return true;
    }

    public boolean handleRegister(Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(204);

            callback.succeeded();
            return true;
        }
        if (!request.getMethod().equals("POST")) {
            response.setStatus(405);
            Content.Sink.write(response, true, "Method not allowed - only POST is allowed", callback);

            callback.succeeded();
            return true;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(Content.Source.asInputStream(request)));

        Gson gson = new Gson();
        RegisterFormData registerFormData = gson.fromJson(reader, RegisterFormData.class);

        if (registerFormData.username() == null || registerFormData.password() == null) {
            response.setStatus(400);
            Content.Sink.write(response, true, "Bad request - username and password are required", callback);

            callback.succeeded();
            return true;
        }

        String hashedPassword = BCrypt.hashpw(registerFormData.password(), BCrypt.gensalt());

        User newUser = new User(registerFormData.username(), hashedPassword);
        try {
            dbController.createUser(newUser);
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not add user: " + e.getMessage(), callback);

            callback.succeeded();
            return true;
        }

        createSuccessfulLoginResponse(newUser.username(), response, callback);

        callback.succeeded();
        return true;
    }

    private void createSuccessfulLoginResponse(String username, Response response, Callback callback) {
        response.setStatus(200);

        String refreshTokenValue;
        try {
            RefreshToken existingToken = dbController.getRefreshTokenByUsername(username);
            if (existingToken != null && existingToken.expiresAt().before(Timestamp.from(Instant.now()))) {
                dbController.deleteRefreshToken(existingToken.token());
                existingToken = null;
            }

            if (existingToken == null) {
                refreshTokenValue = UUID.randomUUID().toString();
                RefreshToken refreshToken = new RefreshToken(username, refreshTokenValue,
                        Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)));

                dbController.createRefreshToken(refreshToken);
            } else {
                refreshTokenValue = existingToken.token();
            }
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not create refresh token: " + e.getMessage(), callback);
            return;
        }

        String cookieString = String.format(
                "%s=\"%s\"; Path=%s; Max-Age=%d; Secure; HttpOnly",
                "refresh_token", refreshTokenValue, "/", 60 * 60 * 24 * 7
        );
        response.getHeaders().put("Set-Cookie", cookieString);

        String token = tokenManager.generateToken(username);
        SuccessfulLoginResponse successfulLoginResponse = new SuccessfulLoginResponse(token);

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(successfulLoginResponse);
        response.getHeaders().put("Content-Type", "application/json");
        Content.Sink.write(response, true, jsonResponse, callback);
    }

    public boolean handleRefreshJWT(Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(204);

            callback.succeeded();
            return true;
        }
        if (!request.getMethod().equals("POST")) {
            response.setStatus(405);
            Content.Sink.write(response, true, "Method not allowed - only POST is allowed", callback);

            callback.succeeded();
            return true;
        }

        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

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

        RefreshToken refreshToken;
        try {
            refreshToken = dbController.getRefreshToken(refreshTokenCookie.getValue());
        } catch (SQLException e) {
            response.setStatus(500);
            Content.Sink.write(response, true, "Internal server error - could not get refresh token: " + e.getMessage(), callback);
            return true;
        }

        if (refreshToken == null) {
            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - invalid or missing refresh token", callback);

            callback.succeeded();
            return true;
        }

        if (refreshToken.expiresAt().before(Timestamp.from(Instant.now()))) {
            try {
                dbController.deleteRefreshToken(refreshToken.token());
            } catch (SQLException e) {
                response.setStatus(500);
                Content.Sink.write(response, true, "Internal server error - could not delete expired refresh token: " + e.getMessage(), callback);

                return true;
            }

            response.setStatus(401);
            Content.Sink.write(response, true, "Unauthorized - refresh token has expired", callback);

            callback.succeeded();
            return true;
        }

        createSuccessfulLoginResponse(refreshToken.username(), response, callback);

        callback.succeeded();
        return true;
    }

    public boolean handleLogout(Request request, Response response, Callback callback) {
        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(204);

            callback.succeeded();
            return true;
        }
        if (!request.getMethod().equals("POST")) {
            response.setStatus(405);
            Content.Sink.write(response, true, "Method not allowed - only POST is allowed", callback);

            callback.succeeded();
            return true;
        }

        response.getHeaders().put("Access-Control-Allow-Origin", "*");
        response.getHeaders().put("Access-Control-Allow-Headers", "Origin,X-Requested-With, Content-Type, Accept");

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

        String cookieString = "refresh_token=; Path=/; Max-Age=0; Secure; HttpOnly";
        response.getHeaders().put("Set-Cookie", cookieString);

        response.setStatus(200);
        Content.Sink.write(response, true, "Logged out successfully", callback);

        callback.succeeded();
        return true;
    }
}
