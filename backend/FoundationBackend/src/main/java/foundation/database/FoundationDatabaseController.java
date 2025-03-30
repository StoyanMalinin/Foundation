package foundation.database;

import foundation.database.structure.*;

import java.sql.SQLException;
import java.util.List;

public interface FoundationDatabaseController {
    List<Presence> getAllPresencesOfSearchInsideBoundingBox(int searchId, double minX, double maxX, double minY, double maxY) throws SQLException;
}
