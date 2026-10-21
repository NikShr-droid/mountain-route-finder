package com.routefinder.model;

/**
 * Represents an established rest or overnight camping site on a mountain trail.
 */
public class Camp extends Waypoint {

    public Camp(int id, int mountainId, String name, double latitude, double longitude, int elevationM) {
        super(id, mountainId, name, latitude, longitude, elevationM);
    }

    @Override
    public String getWaypointType() {
        return "camp";
    }

    @Override
    public String describe() {
        return String.format("Camp Site: %s (Elevation: %dm)", getName(), getElevationM());
    }
}
