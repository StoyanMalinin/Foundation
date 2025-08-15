package foundation.database.structure;

public record User(String username, String passwordHash, String firstName, String lastName) {}
