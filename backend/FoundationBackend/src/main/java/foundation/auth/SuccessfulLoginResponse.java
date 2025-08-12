package foundation.auth;

import com.google.gson.annotations.SerializedName;

public record SuccessfulLoginResponse(@SerializedName("token") String token) {}
