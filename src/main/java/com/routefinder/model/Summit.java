package com.routefinder.model;

/**
 * Represents the peak or summit of the mountain (the ultimate destination of an ascent route).
 */
public class Summit extends Waypoint {

    public Summit(int id, int mountainId, String name, double latitude, double longitude, int elevationM) {
        super(id, mountainId, name, latitude, longitude, elevationM);
    }

    @Override
    public String getWaypointType() {
        return "summit";
    }

    @Override
    public String describe() {
        return String.format("Peak Summit: %s (Max Elevation: %dm)", getName(), getElevationM());
    }

    @Override
    public boolean isSummit() {
        return true;
    }
}
