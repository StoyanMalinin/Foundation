import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import foundation.map.MapImageGetter;
import foundation.map.tomtom.TomTomMapImageGetter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;

public class Sandbox {
    public static void run() {
        String databaseUrl = "jdbc:sqlite:../db/db_foundation.db";

        try (ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);) {

            MapImageGetter mapImageGetter = new TomTomMapImageGetter();

            BufferedImage img = mapImageGetter.getMap(26.20512482624002, 41.76797529308914, 50);
            ImageIO.write(img, "png", new File("picture.png"));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
