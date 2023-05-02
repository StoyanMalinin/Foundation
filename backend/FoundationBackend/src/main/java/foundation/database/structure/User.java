package foundation.database.structure;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String username;

    @DatabaseField
    private String password;

    public User() {

    }
    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }
}
