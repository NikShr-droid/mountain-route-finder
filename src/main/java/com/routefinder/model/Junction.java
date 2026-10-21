package com.routefinder.model;

/**
 * Represents a trail junction or crossroads where paths diverge or converge.
 */
public class Junction extends Waypoint {

    public Junction(int id, int mountainId, String name, double latitude, double longitude, int elevationM) {
        super(id, mountainId, name, latitude, longitude, elevationM);
    }

    @Override
    public String getWaypointType() {
        return "junction";
    }

    @Override
    public String describe() {
        return String.format("Trail Junction: %s (Elevation: %dm)", getName(), getElevationM());
    }
}
