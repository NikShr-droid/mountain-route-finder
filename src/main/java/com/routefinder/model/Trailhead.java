package com.routefinder.model;

/**
 * Represents the starting point or entry gate of a mountain ascent route.
 */
public class Trailhead extends Waypoint {

    public Trailhead(int id, int mountainId, String name, double latitude, double longitude, int elevationM) {
        super(id, mountainId, name, latitude, longitude, elevationM);
    }

    @Override
    public String getWaypointType() {
        return "trailhead";
    }

    @Override
    public String describe() {
        return String.format("Trailhead: %s (Base Elevation: %dm)", getName(), getElevationM());
    }

    @Override
    public boolean isTrailhead() {
        return true;
    }
}
