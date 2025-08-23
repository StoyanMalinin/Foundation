package main.java.foundation.database.structure;

import java.sql.Timestamp;

public record RateLimiterPresence(
    String username,
    Presence presence
) {}
