package com.routefinder.model;

/**
 * Factory class for instantiating the appropriate {@link Waypoint} subclass
 * based on the string type stored in SQLite ('trailhead', 'camp', 'checkpoint', 'junction', 'summit').
 *
 * <p><b>Design Pattern:</b> Simple Factory / Static Factory Method.
 * Decouples database parsing from specific concrete classes and prevents
 * string comparison logic from polluting DAOs or UI code.</p>
 */
public final class WaypointFactory {

    private WaypointFactory() {
        // Prevent instantiation
    }

    /**
     * Creates a concrete Waypoint subtype based on the provided type identifier.
     *
     * @throws IllegalArgumentException if waypointType is unrecognized
     */
    public static Waypoint create(int id, int mountainId, String name, String waypointType,
                                  double latitude, double longitude, int elevationM) {
        if (waypointType == null || waypointType.isBlank()) {
            throw new IllegalArgumentException("waypointType cannot be null or blank");
        }

        return switch (waypointType.trim().toLowerCase()) {
            case "trailhead" -> new Trailhead(id, mountainId, name, latitude, longitude, elevationM);
            case "camp" -> new Camp(id, mountainId, name, latitude, longitude, elevationM);
            case "checkpoint" -> new Checkpoint(id, mountainId, name, latitude, longitude, elevationM);
            case "junction" -> new Junction(id, mountainId, name, latitude, longitude, elevationM);
            case "summit" -> new Summit(id, mountainId, name, latitude, longitude, elevationM);
            default -> throw new IllegalArgumentException("Unknown waypoint type: '" + waypointType + "'");
        };
    }
}
