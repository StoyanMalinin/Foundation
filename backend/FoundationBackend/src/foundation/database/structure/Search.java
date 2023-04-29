package foundation.database.structure;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.w3c.dom.html.HTMLImageElement;

import javax.sql.rowset.serial.SerialArray;

@DatabaseTable(tableName = "searches")
public class Search {
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String title;

    @DatabaseField
    private String description;

    public Search() {

    }
    public Search(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public int id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }
}