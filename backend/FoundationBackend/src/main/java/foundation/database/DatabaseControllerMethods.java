package foundation.database;

import foundation.database.structure.Presence;
import foundation.database.structure.RefreshToken;
import foundation.database.structure.SearchMetadata;
import foundation.database.structure.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class DatabaseControllerMethods {
    public static List<Presence> getAllPresencesOfSearchInsideBoundingBox(
        Connection connection,
        int searchId,
        double minX, double maxX,
        double minY, double maxY) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT presences.timestamp as timestamp, st_x(presences.point) as x, st_y(presences.point) as y FROM " +
                        "foundation.searches as searches JOIN foundation.search_to_presence as search_to_presence  ON searches.id = search_to_presence.search_id " +
                        "         JOIN foundation.presences as presences ON search_to_presence.presence_id = presences.id " +
                        "WHERE searches.id = ? AND ST_Contains(ST_MakeEnvelope(?, ?, ?, ?), point)");
        preparedStatement.setInt(1, searchId);
        preparedStatement.setDouble(2, minX);
        preparedStatement.setDouble(3, minY);
        preparedStatement.setDouble(4, maxX);
        preparedStatement.setDouble(5, maxY);

        List<Presence> presences = new ArrayList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()){
            while (resultSet.next()) {
                int timestamp = resultSet.getInt("timestamp");
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                presences.add(new Presence(timestamp, x, y));
            }
        }

        return presences;
    }

    public static List<SearchMetadata> getSearchesMetadataByUsername(Connection connection, String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT searches.title as title, searches.created_at as created_at FROM foundation.searches as searches WHERE searches.owner_username = ?");
        preparedStatement.setString(1, username);

        List<SearchMetadata> searchMetadataList = new ArrayList<>();
        try (ResultSet resultSet = preparedStatement.executeQuery()){
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                searchMetadataList.add(new SearchMetadata(title));
            }
        }

        return searchMetadataList;
    }

    public static User getUserByUsername(Connection connection, String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM foundation.users as users WHERE users.username = ?");
        preparedStatement.setString(1, username);

        try (ResultSet resultSet = preparedStatement.executeQuery()){
            if (resultSet.next()) {
                String userName = resultSet.getString("username");
                String passwordHash = resultSet.getString("password_hash");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                return new User(userName, passwordHash, firstName, lastName);
            }
        }

        return null;
    }

    public static void createUser(Connection connection, User user) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO foundation.users (username, password_hash, first_name, last_name) VALUES (?, ?, ?, ?)");
        preparedStatement.setString(1, user.username());
        preparedStatement.setString(2, user.passwordHash());
        preparedStatement.setString(3, user.firstName());
        preparedStatement.setString(4, user.lastName());

        preparedStatement.executeUpdate();
    }

    public static void createRefreshToken(Connection connection, RefreshToken refreshToken) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO foundation.refresh_tokens (username, token, expires_at) VALUES (?, ?, ?)");
        preparedStatement.setString(1, refreshToken.username());
        preparedStatement.setString(2, refreshToken.token());
        preparedStatement.setTimestamp(3, refreshToken.expiresAt());

        preparedStatement.executeUpdate();
    }

    public static RefreshToken getRefreshToken(Connection connection, String token) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT refresh_tokens.username as username, refresh_tokens.token as token, refresh_tokens.expires_at as expires_at " +
                        "FROM foundation.refresh_tokens as refresh_tokens WHERE refresh_tokens.token = ?");
        preparedStatement.setString(1, token);

        try (ResultSet resultSet = preparedStatement.executeQuery()){
            if (resultSet.next()) {
                String username = resultSet.getString("username");
                String tokenValue = resultSet.getString("token");
                Timestamp expiresAt = resultSet.getTimestamp("expires_at");

                return new RefreshToken(username, tokenValue, expiresAt);
            }
        }

        return null;
    }

    public static RefreshToken getRefreshTokenByUsername(Connection connection, String username) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT refresh_tokens.username as username, refresh_tokens.token as token, refresh_tokens.expires_at as expires_at " +
                        "FROM foundation.refresh_tokens as refresh_tokens WHERE refresh_tokens.username = ?");
        preparedStatement.setString(1, username);

        try (ResultSet resultSet = preparedStatement.executeQuery()){
            if (resultSet.next()) {
                String tokenValue = resultSet.getString("token");
                Timestamp expiresAt = resultSet.getTimestamp("expires_at");

                return new RefreshToken(username, tokenValue, expiresAt);
            }
        }

        return null;
    }

    public static void deleteRefreshToken(Connection connection, String token) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM foundation.refresh_tokens WHERE token = ?");
        preparedStatement.setString(1, token);
        preparedStatement.executeUpdate();
    }
}
