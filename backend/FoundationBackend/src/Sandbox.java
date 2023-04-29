import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import foundation.database.FoundationDatabaseController;
import foundation.database.SQLiteFoundationDatabaseController;
import foundation.database.structure.*;

import java.sql.SQLException;

public class Sandbox {
    public static void run() {
        String databaseUrl = "jdbc:sqlite:../db/db_foundation.db";

        try (ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);) {
            FoundationDatabaseController controller = new SQLiteFoundationDatabaseController(databaseUrl);

            for (Presence p : controller.getAllPresencesOfSearch(1)) {
                System.out.println(p.id());
            }

            System.out.println("--------");

            for (Presence p : controller.getAllPresencesOfSearchInsideBoundingBox(1, -0.5, +0.5, -0.5, +0.5)) {
                System.out.println(p.id());
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
