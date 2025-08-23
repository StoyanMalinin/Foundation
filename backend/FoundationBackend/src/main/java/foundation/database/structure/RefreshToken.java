package main.java.foundation.database.structure;

import java.sql.Timestamp;

public record RefreshToken(String username, String token, Timestamp expiresAt) {}
