package foundation.database.structure;

import com.google.gson.annotations.SerializedName;

public record Search(
        @SerializedName("id") int id,
        @SerializedName("title") String title,
        @SerializedName("description") String description,
        @SerializedName("created_at") String created_at,
        @SerializedName("owner_username") String owner_username
) {
}
