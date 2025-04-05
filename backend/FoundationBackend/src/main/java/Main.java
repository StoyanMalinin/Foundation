import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import foundation.database.FoundationDatabaseController;
import foundation.database.PostgresFoundationDatabaseController;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.BaiscTomTomAPICommunicator;
import foundation.map.tomtom.CachedTomTomAPICommunicator;
import foundation.web.EndpointController;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("server");

        Server server = new Server(threadPool);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("../secrets/keystore-dev.jks");
        sslContextFactory.setKeyStorePassword("123456");
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

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/foundation");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource dataSource = new HikariDataSource(config);

        try {
            FoundationDatabaseController dbController = new PostgresFoundationDatabaseController(dataSource);
            MapImageGetter mapImageGetter = new CachedTomTomAPICommunicator(new BaiscTomTomAPICommunicator());
            EndpointController controller = new EndpointController(mapImageGetter, dbController);

            PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/test"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            System.out.println("are bate");

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
                            return controller.handleMapTileImage(request, response, callback);
                        }
                    });

            pathMappingsHandler.addMapping(
                    new ServletPathSpec("/searches-metadata"),
                    new Handler.Abstract() {
                        @Override
                        public boolean handle(Request request, Response response, Callback callback) throws Exception {
                            return controller.handleGetSearchesMetadata(request, response, callback);
                        }
                    });

            server.setHandler(pathMappingsHandler);

            server.start();
            System.out.println("Application started");
        } catch (SQLException e) {
            System.out.println("Unhandled sql exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unhandled exception: " + e.getMessage());
        }
    }
}