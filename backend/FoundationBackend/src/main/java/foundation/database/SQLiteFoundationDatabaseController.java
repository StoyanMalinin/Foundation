package foundation.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import foundation.database.FoundationDatabaseController;
import foundation.database.structure.*;
import jdk.jshell.spi.ExecutionControl;

import java.sql.SQLException;
import java.util.List;

public class SQLiteFoundationDatabaseController implements FoundationDatabaseController, AutoCloseable {

    private ConnectionSource connectionSource;

    private Dao<Search, Integer> searchesDao;
    private Dao<Presence, Integer> presenceDao;
    private Dao<Position, Integer> positionsDao;
    private Dao<User, Integer> usersDao;
    private Dao<SearchToPresence, Integer> searchToPresenceDao;

    public SQLiteFoundationDatabaseController(String databaseURL) throws SQLException {
        this.connectionSource = new JdbcConnectionSource(databaseURL);

        this.searchesDao = DaoManager.createDao(connectionSource, Search.class);
        this.presenceDao = DaoManager.createDao(connectionSource, Presence.class);
        this.positionsDao = DaoManager.createDao(connectionSource, Position.class);
        this.usersDao = DaoManager.createDao(connectionSource, User.class);
        this.searchToPresenceDao = DaoManager.createDao(connectionSource, SearchToPresence.class);
    }

    @Override
    public List<Search> getAllSearches() throws SQLException {
        return searchesDao.queryForAll();
    }

    @Override
    public List<Presence> getAllPresences() throws SQLException {
        return presenceDao.queryForAll();
    }

    @Override
    public List<Position> getAllPositions() throws SQLException {
        return positionsDao.queryForAll();
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        return usersDao.queryForAll();
    }

    @Override
    public List<SearchToPresence> getAllSearchToPresences() throws SQLException {
        return searchToPresenceDao.queryForAll();
    }

    @Override
    public Search getSearchById(int id) throws SQLException {
        return searchesDao.queryForId(id);
    }

    @Override
    public User getUserById(int id) throws SQLException {
        return usersDao.queryForId(id);
    }

    @Override
    public Presence getPresenceById(int id) throws SQLException {
        return presenceDao.queryForId(id);
    }

    @Override
    public SearchToPresence getSearchToPresenceById(int id) throws SQLException {
        return searchToPresenceDao.queryForId(id);
    }

    @Override
    public Position getPositionById(int id) throws SQLException {
        return positionsDao.queryForId(id);
    }

    @Override
    public List<Presence> getAllPresencesOfSearch(int searchId) throws SQLException {
        PreparedQuery<SearchToPresence> query = searchToPresenceDao.queryBuilder()
                .where().eq("searchId", searchId).prepare();

        List<SearchToPresence> queryRes = searchToPresenceDao.query(query);
        return queryRes.stream().map(SearchToPresence::presenceId).toList();
    }

    @Override
    public List<Presence> getAllPresencesOfSearch(Search s) throws SQLException {
        return getAllPresencesOfSearch(s.id());
    }

    @Override
    public List<Presence> getAllPresencesOfSearchInsideBoundingBox(int searchId,
                                                                   double minX, double maxX,
                                                                   double minY, double maxY) throws SQLException {
        QueryBuilder<Position, Integer> positionQueryBuilder = positionsDao.queryBuilder();
        positionQueryBuilder.where().ge("minX", minX).and().le("minX", maxX)
                .and().ge("minY", minY).and().le("minY", maxY);

        QueryBuilder<SearchToPresence, Integer> searchToPresenceQueryBuilder = searchToPresenceDao.queryBuilder();
        searchToPresenceQueryBuilder.join("presenceId", "id", positionQueryBuilder);
        searchToPresenceQueryBuilder.where().eq("searchId", searchId);

        List<SearchToPresence> queryRes = searchToPresenceDao.query(searchToPresenceQueryBuilder.prepare());
        return queryRes.stream().map(SearchToPresence::presenceId).toList();
    }

    @Override
    public void updatePosition(Position position) throws SQLException {
        positionsDao.update(position);
    }

    @Override
    public void updateUser(User user) throws SQLException {
        usersDao.update(user);
    }

    @Override
    public void updateSearch(Search search) throws SQLException {
        searchesDao.update(search);
    }

    @Override
    public void updateSearchToPresence(SearchToPresence searchToPresence) throws SQLException {
        searchToPresenceDao.update(searchToPresence);
    }

    @Override
    public void updatePresence(Presence presence) throws SQLException {
        presenceDao.update(presence);
    }

    @Override
    public void insertUser(User user) throws SQLException {
        usersDao.create(user);
    }

    @Override
    public void insertSearch(Search search) throws SQLException {
        searchesDao.create(search);
    }

    @Override
    public void insertPosition(Position position) throws SQLException {
        positionsDao.create(position);
    }

    @Override
    public void insertPresence(Presence presence) throws SQLException {
        presenceDao.create(presence);
    }

    @Override
    public void insertSearchToPresence(SearchToPresence searchToPresence) throws SQLException {
        searchToPresenceDao.create(searchToPresence);
    }

    @Override
    public void close() throws Exception {
        this.connectionSource.close();
    }
}
