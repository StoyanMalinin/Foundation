package foundation.database.structure;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "presence_info")
public class Presence {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private int timestamp;

    @DatabaseField(foreign = true, foreignAutoCreate = true,
            foreignAutoRefresh = true, columnName = "posId")
    private Position posId;

    public Presence() {

    }
    public Presence(int id, int timestamp, Position posId) {
        this.id = id;
        this.timestamp = timestamp;
        this.posId = posId;
    }

    public int id() {
        return id;
    }

    public int timestamp() {
        return timestamp;
    }

    public Position posId() {
        return posId;
    }
}
