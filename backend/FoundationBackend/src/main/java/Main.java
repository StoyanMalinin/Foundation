import foundation.database.FoundationDatabaseController;
import foundation.database.SQLiteFoundationDatabaseController;
import foundation.map.MapImageColorizer;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.TomTomMapImageGetter;
import foundation.web.EndpointController;

import java.sql.SQLException;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {

        String dbConnectionString = "jdbc:sqlite:../db/db_foundation - Copy.db";
        try (FoundationDatabaseController dbController = new SQLiteFoundationDatabaseController(dbConnectionString);) {

            MapImageGetter mapImageGetter = new TomTomMapImageGetter();
            EndpointController controller = new EndpointController(mapImageGetter, dbController);

            get("/test", (req, resp) -> {
                return "<h>zdr " + req.queryMap().get("name") + "</h>";
            });

            get("/map", controller::handleMapImage);

        } catch (SQLException e) {

        } catch (Exception e) {

        }
    }
}