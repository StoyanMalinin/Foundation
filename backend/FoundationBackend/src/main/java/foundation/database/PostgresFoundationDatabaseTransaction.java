package foundation.database;

import foundation.database.structure.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PostgresFoundationDatabaseTransaction implements FoundationDatabaseController, AutoCloseable {
    private final Connection connection;

    public PostgresFoundationDatabaseTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Presence> getAllPresencesOfSearchInsideBoundingBox(int searchId,
                                                                   double minX, double maxX,
                                                                   double minY, double maxY) throws SQLException {
        return DatabaseControllerMethods.getAllPresencesOfSearchInsideBoundingBox(
                connection, searchId, minX, maxX, minY, maxY);
    }

    @Override
    public List<SearchMetadata> getSearchesMetadataByUsername(String username) throws SQLException {
        return DatabaseControllerMethods.getSearchesMetadataByUsername(connection, username);
    }

    @Override
    public User getUserByUsername(String username) throws SQLException {
        return DatabaseControllerMethods.getUserByUsername(connection, username);
    }

    @Override
    public void createUser(User user) throws SQLException {
        DatabaseControllerMethods.createUser(connection, user);
    }

    @Override
    public void createRefreshToken(RefreshToken refreshToken) throws SQLException {
        DatabaseControllerMethods.createRefreshToken(connection, refreshToken);
    }

    @Override
    public RefreshToken getRefreshToken(String token) throws SQLException {
        return DatabaseControllerMethods.getRefreshToken(connection, token);
    }

    @Override
    public RefreshToken getRefreshTokenByUsername(String username) throws SQLException {
        return DatabaseControllerMethods.getRefreshTokenByUsername(connection, username);
    }

    @Override
    public void deleteRefreshToken(String token) throws SQLException {
        DatabaseControllerMethods.deleteRefreshToken(connection, token);
    }

    @Override
    public Search getSearchById(int id) throws SQLException {
        return DatabaseControllerMethods.getSearchById(connection, id);
    }

    @Override
    public void updateSearch(Search search) throws SQLException {
        DatabaseControllerMethods.updateSearch(connection, search);
    }

    @Override
    public void createSearch(Search search) throws SQLException {
        DatabaseControllerMethods.createSearch(connection, search);
    }

    @Override
    public void deleteSearch(int searchId) throws SQLException {
        DatabaseControllerMethods.deleteSearch(connection, searchId);
    }

    @Override
    public void deleteSearchPresenceAssociations(int searchId) throws SQLException {
        DatabaseControllerMethods.deleteSearchPresenceAssociations(connection, searchId);
    }

    @Override
    public void deletePresencesWithoutSearch() throws SQLException {
        DatabaseControllerMethods.deletePresencesWithoutSearch(connection);
    }

    @Override
    public List<SearchMetadata> getSearchesMetadata() throws SQLException {
        return DatabaseControllerMethods.getSearchesMetadata(connection);
    }

    @Override
    public void close() throws Exception {
        connection.commit();
        connection.close();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }
}
