package main.java.foundation.database;

import java.sql.*;

import main.java.foundation.database.structure.*;

import javax.sql.DataSource;
import java.util.List;

public class PostgresFoundationDatabase implements FoundationDatabaseController {

    private final DataSource dataSource;

    public PostgresFoundationDatabase(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
    }

    @Override
    public List<Presence> getAllPresencesOfSearchInsideBoundingBox(int searchId,
                                                                   double minX, double maxX,
                                                                   double minY, double maxY) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseControllerMethods.getAllPresencesOfSearchInsideBoundingBox(
                    connection, searchId, minX, maxX, minY, maxY);
        }
    }

    @Override
    public List<SearchMetadata> getSearchesMetadataByUsername(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseControllerMethods.getSearchesMetadataByUsername(connection, username);
        }
    }

    @Override
    public User getUserByUsername(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseControllerMethods.getUserByUsername(connection, username);
        }
    }

    @Override
    public void createUser(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.createUser(connection, user);
        }
    }

    @Override
    public void createRefreshToken(RefreshToken refreshToken) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.createRefreshToken(connection, refreshToken);
        }
    }

    @Override
    public RefreshToken getRefreshToken(String token) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseControllerMethods.getRefreshToken(connection, token);
        }
    }

    @Override
    public RefreshToken getRefreshTokenByUsername(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseControllerMethods.getRefreshTokenByUsername(connection, username);
        }
    }

    @Override
    public void deleteRefreshToken(String token) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.deleteRefreshToken(connection, token);
        }
    }

    @Override
    public Search getSearchById(int id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseControllerMethods.getSearchById(connection, id);
        }
    }

    @Override
    public void updateSearch(Search search) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.updateSearch(connection, search);
        }
    }

    @Override
    public void createSearch(Search search) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.createSearch(connection, search);
        }
    }

    @Override
    public void deleteSearch(int searchId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.deleteSearch(connection, searchId);
        }
    }

    @Override
    public void deleteSearchPresenceAssociations(int searchId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.deleteSearchPresenceAssociations(connection, searchId);
        }
    }

    @Override
    public void deletePresencesWithoutSearch() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.deletePresencesWithoutSearch(connection);
        }
    }

    @Override
    public List<SearchMetadata> getSearchesMetadata() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseControllerMethods.getSearchesMetadata(connection);
        }
    }

    @Override
    public List<RateLimiterPresence> getRateLimiterPresencesForUser(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseControllerMethods.getRateLimiterPresencesForUser(connection, username);
        }
    }

    @Override
    public void deleteRateLimiterPresencesForUser(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.deleteRateLimiterPresencesForUser(connection, username);
        }
    }

    @Override
    public void insertRateLimiterPresences(String username, List<Presence> presences) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseControllerMethods.insertRateLimiterPresences(conn, username, presences);
        }
    }

    @Override
    public List<Long> insertPresences(List<Presence> presences) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseControllerMethods.insertPresences(connection, presences);
        }
    }

    @Override
    public void linkSearchesAndPresences(int[] searchIds, List<Long> presenceIds) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseControllerMethods.linkSearchesAndPresences(connection, searchIds, presenceIds);
        }
    }

    public PostgresFoundationDatabaseTransaction createTransaction() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        return new PostgresFoundationDatabaseTransaction(connection);
    }
}
