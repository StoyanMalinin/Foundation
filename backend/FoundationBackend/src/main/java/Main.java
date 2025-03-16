import foundation.database.FoundationDatabaseController;
import foundation.database.SQLiteFoundationDatabaseController;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.BaiscTomTomAPICommunicator;
import foundation.map.tomtom.CachedTomTomAPICommunicator;
import foundation.map.tomtom.TomTomAPICommunicator;
import foundation.map.tomtom.TomTomMapImageGetter;
import foundation.web.EndpointController;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("server");

        Server server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(6969);
        server.addConnector(connector);

        ContextHandlerCollection contextCollection = new ContextHandlerCollection();

        String dbConnectionString = "jdbc:sqlite:../db/db_foundation - Copy.db";
        try (FoundationDatabaseController dbController = new SQLiteFoundationDatabaseController(dbConnectionString)) {
            TomTomAPICommunicator tomtomAPI = new CachedTomTomAPICommunicator(new BaiscTomTomAPICommunicator());
            MapImageGetter mapImageGetter = new TomTomMapImageGetter(tomtomAPI);
            EndpointController controller = new EndpointController(mapImageGetter, dbController);

            contextCollection.addHandler(new ContextHandler(new Handler.Abstract() {
                @Override
                public boolean handle(Request request, Response response, Callback callback) throws Exception {
                    System.out.println("aide");

                    response.setStatus(200);
                    response.getHeaders().put("Content-Type", "text/html");
                    Content.Sink.write(response, true, "<h1>zdr " + Request.getParameters(request).get("name") + "</h1>", callback);

                    callback.succeeded();
                    return true;
                }
            }, "/test"));

            contextCollection.addHandler(new ContextHandler(new Handler.Abstract() {
                @Override
                public boolean handle(Request request, Response response, Callback callback) throws Exception {
                    return controller.handleMapTileImage(request, response, callback);
                }
            }, "/map-tile"));

            server.setHandler(contextCollection);

            server.start();
            System.out.println("Application started");
        } catch (SQLException e) {
            System.out.println("Unhandled sql exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unhandled exception: " + e.getMessage());
        }
    }
}