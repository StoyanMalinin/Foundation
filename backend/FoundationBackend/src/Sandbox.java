import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import foundation.database.FoundationDatabaseController;
import foundation.database.SQLiteFoundationDatabaseController;
import foundation.database.structure.Position;
import foundation.database.structure.Presence;
import foundation.database.structure.SearchToPresence;
import foundation.map.MapImageColorizer;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.TileGridUtils;
import foundation.map.tomtom.TomTomMapImageGetter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;

public class Sandbox {
    public static void run() {
        String databaseUrl = "jdbc:sqlite:../db/db_foundation - Copy.db";

        try (ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);) {

            FoundationDatabaseController dbController = new SQLiteFoundationDatabaseController(databaseUrl);

            MapImageGetter mapImageGetter = new TomTomMapImageGetter();
            MapImageColorizer mapImageColorizer = new MapImageColorizer(dbController, dbController.getSearchById(1));

            final double sz = 1000;
            final double lat = 41.76795371636188;
            final double lon = 26.204573303326818;

            /*
            for (int i = 0; i < 100; i++) {
                Position position = new Position(lon, lon, lat, lat);
                Presence presence = new Presence(0, 2, position);
                SearchToPresence searchToPresence = new SearchToPresence(0, dbController.getSearchById(1), presence);

                dbController.insertPosition(position);
                dbController.insertPresence(presence);
                dbController.insertSearchToPresence(searchToPresence);
            }
            */

            BufferedImage img = mapImageGetter.getMap(lon, lat, sz);
            mapImageColorizer.colorizeImage(img, TileGridUtils.getBoundingBox(lon, lat, sz), 10, 10, 5);

            ImageIO.write(img, "png", new File("picture.png"));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
