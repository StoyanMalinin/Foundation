package foundation.database;

import java.sql.Connection;
import foundation.database.structure.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresFoundationDatabaseController implements FoundationDatabaseController {

    private Connection connection;

    public PostgresFoundationDatabaseController(Connection connection) throws SQLException {
        this.connection = connection;
    }

    @Override
    public List<Presence> getAllPresencesOfSearchInsideBoundingBox(int searchId,
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
}
