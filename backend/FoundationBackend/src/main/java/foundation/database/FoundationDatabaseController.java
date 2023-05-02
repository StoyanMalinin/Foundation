package foundation.database;

import foundation.database.structure.*;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

public interface FoundationDatabaseController extends AutoCloseable {
    List<Search> getAllSearches() throws SQLException;
    List<Presence> getAllPresences() throws SQLException;
    List<Position> getAllPositions() throws SQLException;
    List<User> getAllUsers() throws SQLException;
    List<SearchToPresence> getAllSearchToPresences() throws SQLException;

    Search getSearchById(int id) throws SQLException;
    User getUserById(int id) throws SQLException;
    Presence getPresenceById(int id) throws SQLException;
    SearchToPresence getSearchToPresenceById(int id) throws SQLException;
    Position getPositionById(int id) throws SQLException;

    List<Presence> getAllPresencesOfSearch(int searchId) throws SQLException;
    List<Presence> getAllPresencesOfSearch(Search s) throws SQLException;
    List<Presence> getAllPresencesOfSearchInsideBoundingBox(int searchId, double minX, double maxX, double minY, double maxY) throws SQLException;

    void updatePosition(Position position) throws SQLException;
    void updateUser(User user) throws SQLException;
    void updateSearch(Search search) throws SQLException;
    void updateSearchToPresence(SearchToPresence searchToPresence) throws SQLException;
    void updatePresence(Presence presence) throws SQLException;

    void insertUser(User user) throws SQLException;
    void insertSearch(Search search) throws SQLException;
    void insertPosition(Position position) throws SQLException;
    void insertPresence(Presence presence) throws SQLException;
    void insertSearchToPresence(SearchToPresence searchToPresence) throws SQLException;
}
