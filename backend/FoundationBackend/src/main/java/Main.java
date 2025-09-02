package main.java;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import main.java.foundation.auth.TokenManager;
import main.java.foundation.database.PostgresFoundationDatabase;
import main.java.foundation.map.MapImageGetter;
import main.java.foundation.map.tomtom.BaiscTomTomAPICommunicator;
import main.java.foundation.map.tomtom.CachedTomTomAPICommunicator;
import main.java.foundation.web.EndpointController;
import main.java.foundation.web.middleware.HandlerFunction;
import main.java.foundation.web.middleware.MiddlewareUtils;
import main.java.foundation.web.middleware.BrowserMiddleware;
import main.java.foundation.web.middleware.EnsureHTTPMethodMiddleware;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Scanner;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        JsonObject config;
        try {
            config = readConfiguration();
        } catch (IOException e) {
            System.out.println("Failed to read configuration: " + e.getMessage());
            return;
        }

        String tokenSecret = config.get("jwt_secret").getAsString();
        TokenManager tokenManager = new TokenManager(tokenSecret);

        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("server");

        Server server = new Server(threadPool);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("../secrets/jetty.jks");
        sslContextFactory.setKeyStorePassword(config.get("cert").getAsJsonObject().get("password").getAsString());
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        sslContextFactory.setSniRequired(false);

        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.setSecureScheme("https");
        httpsConfig.setSecurePort(6969);

        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(httpsConfig);
        SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

        ServerConnector httpsConnector = new ServerConnector(server, ssl, alpn, http2, new HttpConnectionFactory(httpsConfig));
        httpsConnector.setPort(6969);
        server.addConnector(httpsConnector);

        JsonObject postgresConfig = config.get("db").getAsJsonObject().get("postgres").getAsJsonObject();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgresConfig.get("url").getAsString());
        hikariConfig.setUsername(postgresConfig.get("username").getAsString());
        hikariConfig.setPassword(postgresConfig.get("password").getAsString());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        BrowserMiddleware browserMiddleware = new BrowserMiddleware(config.get("frontend").getAsJsonObject().get("origin").getAsString());
        BrowserMiddleware mobileLocalDevelopmentMiddleware = new BrowserMiddleware("http://localhost:8081");

        try {
            PostgresFoundationDatabase dbController = new PostgresFoundationDatabase(dataSource);

            String redisURL = config.get("db").getAsJsonObject().get("redis").getAsJsonObject().get("url").getAsString();
            int redisPort = config.get("db").getAsJsonObject().get("redis").getAsJsonObject().get("port").getAsInt();
            MapImageGetter mapImageGetter = new CachedTomTomAPICommunicator(
                    redisURL, redisPort,
                    new BaiscTomTomAPICommunicator(config.get("tomtom_api_key").getAsString())
            );

            EndpointController controller = new EndpointController(mapImageGetter, dbController, tokenManager);

            PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/test"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            response.setStatus(200);
                            response.getHeaders().put("Content-Type", "text/html");
                            Content.Sink.write(response, true, "<h1>zdr " + Request.getParameters(request).get("name") + "</h1>", callback);

                            callback.succeeded();
                            return true;
                        }
                    });

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/map-tile"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) {
                            HandlerFunction fn = controller::handleMapTileImage;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    browserMiddleware, new EnsureHTTPMethodMiddleware("GET")
                            );

                            return fn.apply(request, response, callback);
                        }
                    });

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/admin-searches-metadata"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleGetAdminSearchesMetadata;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    new EnsureHTTPMethodMiddleware("GET")
                            );

                            return fn.apply(request, response, callback);
                        }
                    });

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/searches-metadata"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleGetSearchesMetadata;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    new EnsureHTTPMethodMiddleware("GET")
                            );

                            return fn.apply(request, response, callback);
                        }
                    });

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/login"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleLogin;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    browserMiddleware, new EnsureHTTPMethodMiddleware("POST")
                            );

                            return fn.apply(request, response, callback);
                        }
                    });

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/login-mobile"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleLoginMobile;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    mobileLocalDevelopmentMiddleware, new EnsureHTTPMethodMiddleware("POST")
                            );

                            return fn.apply(request, response, callback);
                        }
                    });

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/register"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleRegister;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    browserMiddleware, new EnsureHTTPMethodMiddleware("POST")
                            );

                            return fn.apply(request, response, callback);
                        }
                    });

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/logout"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleLogout;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    browserMiddleware, new EnsureHTTPMethodMiddleware("POST")
                            );

                            return fn.apply(request, response, callback);
                        }
                    });

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/refresh-jwt"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleRefreshJWT;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    browserMiddleware, new EnsureHTTPMethodMiddleware("POST")
                            );

                            return fn.apply(request, response, callback);
                        }
                    }
            );

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/check-auth"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleCheckAuth;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    new EnsureHTTPMethodMiddleware("GET")
                            );

                            return fn.apply(request, response, callback);
                        }
                    }
            );

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/who-am-i"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleWhoAmI;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    browserMiddleware, new EnsureHTTPMethodMiddleware("GET")
                            );

                            return fn.apply(request, response, callback);
                        }
                    }
            );

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/update-search"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleUpdateSearch;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    browserMiddleware, new EnsureHTTPMethodMiddleware("PUT")
                            );

                            return fn.apply(request, response, callback);
                        }
                    }
            );

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/create-search"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleCreateSearch;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    browserMiddleware, new EnsureHTTPMethodMiddleware("PUT")
                            );

                            return fn.apply(request, response, callback);
                        }
                    }
            );

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/search"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleGetSearchById;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    new EnsureHTTPMethodMiddleware("GET")
                            );

                            return fn.apply(request, response, callback);
                        }
                    }
            );

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/delete-search"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleDeleteSearch;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    new EnsureHTTPMethodMiddleware("DELETE")
                            );

                            return fn.apply(request, response, callback);
                        }
                    }
            );

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/inject-presences"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            HandlerFunction fn = controller::handleInjectPresences;
                            fn = MiddlewareUtils.applyMiddleware(
                                    fn,
                                    new EnsureHTTPMethodMiddleware("PUT")
                            );

                            return fn.apply(request, response, callback);
                        }
                    }
            );

            server.setHandler(pathMappingsHandler);

            server.start();
            System.out.println("Application started");
        } catch (SQLException e) {
            System.out.println("Unhandled sql exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unhandled exception: " + e.getMessage());
        }
    }

    private static JsonObject readConfiguration() throws IOException {
        String config = Files.readString(Path.of("../../deploy/config/config.json"));
        Gson gson = new Gson();

        return gson.fromJson(config, JsonObject.class);
    }
}
