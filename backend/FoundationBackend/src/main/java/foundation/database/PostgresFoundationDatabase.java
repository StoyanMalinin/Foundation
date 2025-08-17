package foundation.database;

import java.sql.*;

import foundation.database.structure.*;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class PostgresFoundationDatabase implements FoundationDatabaseController {

    private DataSource dataSource;

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

    public PostgresFoundationDatabaseTransaction createTransaction() throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        return new PostgresFoundationDatabaseTransaction(connection);
    }
}
