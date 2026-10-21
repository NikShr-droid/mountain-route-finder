package com.routefinder.model;

/**
 * Domain entity representing a climber user profile.
 *
 * <p><b>OOP Note (Encapsulation):</b>
 * Immutable domain model with private final members and strict constructor validation.</p>
 */
public class User {

    private final int id;
    private final String username;
    private final String displayName;

    public User(int id, String username, String displayName) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name cannot be null or blank");
        }
        this.id = id;
        this.username = username.trim();
        this.displayName = displayName.trim();
    }

    public User(String username, String displayName) {
        this(0, username, displayName);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
