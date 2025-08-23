package main.java.foundation.database.structure;

import com.google.gson.annotations.SerializedName;

public record User(
        @SerializedName("username") String username,
        @SerializedName("password_hash") String passwordHash,
        @SerializedName("first_name") String firstName,
        @SerializedName("last_name") String lastName
) {}
