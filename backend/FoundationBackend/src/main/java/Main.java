import foundation.database.FoundationDatabaseController;
import foundation.database.SQLiteFoundationDatabaseController;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.BaiscTomTomAPICommunicator;
import foundation.map.tomtom.CachedTomTomAPICommunicator;
import foundation.map.tomtom.TomTomAPICommunicator;
import foundation.map.tomtom.TomTomMapImageGetter;
import foundation.web.EndpointController;

import java.sql.SQLException;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        port(6969);

        String dbConnectionString = "jdbc:sqlite:../db/db_foundation - Copy.db";
        try (FoundationDatabaseController dbController = new SQLiteFoundationDatabaseController(dbConnectionString);) {

            TomTomAPICommunicator tomtomAPI = new CachedTomTomAPICommunicator(new BaiscTomTomAPICommunicator());
            MapImageGetter mapImageGetter = new TomTomMapImageGetter(tomtomAPI);
            EndpointController controller = new EndpointController(mapImageGetter, dbController);

            get("/test", (req, resp) -> {
                return "<h>zdr " + req.queryMap().get("name") + "</h>";
            });

            get("/map", controller::handleMapImage);

            get("/map-tile", controller::handleMapTileImage);

            System.out.println("Application started");
        } catch (SQLException e) {
            System.out.println("Unhandled sql exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unhandled exception: " + e.getMessage());
        }
    }
}