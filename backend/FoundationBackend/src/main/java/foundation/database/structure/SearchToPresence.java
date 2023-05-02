package foundation.database.structure;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import javax.sql.rowset.serial.SerialArray;
import java.rmi.server.ServerNotActiveException;

@DatabaseTable(tableName = "search_to_presence")
public class SearchToPresence {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoCreate = true,
            foreignAutoRefresh = true, columnName = "searchId")
    private Search searchId;

    @DatabaseField(foreign = true, foreignAutoCreate = true,
            foreignAutoRefresh = true, columnName = "presenceId")
    private Presence presenceId;

    public SearchToPresence() {

    }
    public SearchToPresence(int id, Search searchId, Presence presenceId) {
        this.id = id;
        this.searchId = searchId;
        this.presenceId = presenceId;
    }

    public int id() {
        return id;
    }

    public Search searchId() {
        return searchId;
    }

    public Presence presenceId() {
        return presenceId;
    }
}
