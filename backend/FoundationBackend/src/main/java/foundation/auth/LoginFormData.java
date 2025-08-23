package main.java.foundation.auth;

import com.google.gson.annotations.SerializedName;

public record LoginFormData(
        @SerializedName("username") String username,
        @SerializedName("password") String password
) {}
