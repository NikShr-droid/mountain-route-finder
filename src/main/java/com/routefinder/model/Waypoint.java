package com.routefinder.model;

/**
 * Abstract base class representing a geographic point of interest on a mountain trail.
 *
 * <p><b>OOP & Viva Note (Inheritance & Abstraction):</b>
 * This abstract class defines the shared attributes (coordinates, elevation, mountain reference)
 * and contracts for all mountain waypoints. Concrete subclasses ({@link Trailhead}, {@link Camp},
 * {@link Checkpoint}, {@link Junction}, {@link Summit}) represent specific terrain roles
 * and provide polymorphic behaviors via {@link #describe()} and role predicates.</p>
 */
public abstract class Waypoint {

    private final int id;
    private final int mountainId;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final int elevationM;

    /**
     * Protected constructor used exclusively by concrete Waypoint subclasses.
     */
    protected Waypoint(int id, int mountainId, String name, double latitude, double longitude, int elevationM) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Waypoint name cannot be null or blank");
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude out of range [-90, 90]: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude out of range [-180, 180]: " + longitude);
        }

        this.id = id;
        this.mountainId = mountainId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevationM = elevationM;
    }

    public int getId() {
        return id;
    }

    public int getMountainId() {
        return mountainId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getElevationM() {
        return elevationM;
    }

    /**
     * Returns the lowercase type identifier matching the database schema CHECK constraint
     * ('trailhead', 'camp', 'checkpoint', 'junction', 'summit').
     */
    public abstract String getWaypointType();

    /**
     * Returns a human-readable description of this waypoint with its specific terrain role.
     */
    public abstract String describe();

    /**
     * Polymorphic predicate indicating whether this waypoint serves as a trail starting point.
     */
    public boolean isTrailhead() {
        return false;
    }

    /**
     * Polymorphic predicate indicating whether this waypoint is the mountain summit.
     */
    public boolean isSummit() {
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
