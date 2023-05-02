package foundation.database.structure;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "position_index")
public class Position {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private double minX;

    @DatabaseField
    private double maxX;

    @DatabaseField
    private double minY;

    @DatabaseField
    private double maxY;

    public Position() {

    }
    public Position(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public int id() {
        return id;
    }

    public double minX() {
        return minX;
    }

    public double maxX() {
        return maxX;
    }

    public double minY() {
        return minY;
    }

    public double maxY() {
        return maxY;
    }
}
