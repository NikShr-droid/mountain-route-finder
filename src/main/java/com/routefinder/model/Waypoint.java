package com.routefinder.model;

/**
 * Domain object for a waypoint, mirroring the `waypoints` table.
 * waypointType is one of: trailhead, camp, checkpoint, junction, summit
 * (matches the CHECK constraint in schema.sql).
 */
public class Waypoint {

    private final int id;
    private final int mountainId;
    private final String name;
    private final String waypointType;
    private final double latitude;
    private final double longitude;
    private final int elevationM;

    public Waypoint(int id, int mountainId, String name, String waypointType,
                     double latitude, double longitude, int elevationM) {
        this.id = id;
        this.mountainId = mountainId;
        this.name = name;
        this.waypointType = waypointType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevationM = elevationM;
    }

    public int getId() { return id; }
    public int getMountainId() { return mountainId; }
    public String getName() { return name; }
    public String getWaypointType() { return waypointType; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getElevationM() { return elevationM; }

    @Override
    public String toString() {
        return name;
    }
}
