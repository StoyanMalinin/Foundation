package foundation.database;

import foundation.database.structure.*;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

public interface FoundationDatabaseController {
    List<Search> getAllSearches() throws SQLException;
    List<Presence> getAllPresences() throws SQLException;
    List<Position> getAllPositions() throws SQLException;
    List<User> getAllUsers() throws SQLException;
    List<SearchToPresence> getAllSearchToPresences() throws SQLException;

    List<Presence> getAllPresencesOfSearch(int searchId) throws SQLException;
    List<Presence> getAllPresencesOfSearch(Search s) throws SQLException;

    List<Presence> getAllPresencesOfSearchInsideBoundingBox(int searchId, double minX, double maxX, double minY, double maxY) throws SQLException;

    void updatePosition(Position position) throws SQLException;
}
