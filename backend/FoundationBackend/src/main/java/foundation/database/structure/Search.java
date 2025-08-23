package main.java.foundation.database.structure;

import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;

public record Search(
        @SerializedName("id") int id,
        @SerializedName("title") String title,
        @SerializedName("description") String description,
        @SerializedName("created_at") Timestamp created_at,
        @SerializedName("owner_username") String owner_username
) {
}
