package foundation.database;

import foundation.database.structure.*;

import java.sql.SQLException;
import java.util.List;

public interface FoundationDatabaseController {
    List<Presence> getAllPresencesOfSearchInsideBoundingBox(int searchId, double minX, double maxX, double minY, double maxY) throws SQLException;

    List<SearchMetadata> getSearchesMetadataByUsername(String username) throws SQLException;

    User getUserByUsername(String username) throws SQLException;

    void createUser(User user) throws SQLException;
    void createRefreshToken(RefreshToken refreshToken) throws SQLException;
    RefreshToken getRefreshToken(String token) throws SQLException;
    RefreshToken getRefreshTokenByUsername(String username) throws SQLException;
    void deleteRefreshToken(String token) throws SQLException;
    Search getSearchById(int id) throws SQLException;
    void updateSearch(Search search) throws SQLException;
}
