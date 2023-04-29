package foundation.database.structure;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "position_index")
public class Position {
    @DatabaseField(id = true)
    public int id;

    @DatabaseField
    public double minX;

    @DatabaseField
    public double maxX;

    @DatabaseField
    public double minY;

    @DatabaseField
    public double maxY;
}
