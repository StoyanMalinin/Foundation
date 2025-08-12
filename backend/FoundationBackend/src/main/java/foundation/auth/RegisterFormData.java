package foundation.auth;

import com.google.gson.annotations.SerializedName;

public record RegisterFormData(
        @SerializedName("username") String username,
        @SerializedName("password") String password
) {}