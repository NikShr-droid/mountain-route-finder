package com.routefinder.model;

/**
 * Represents an official ranger station, permit checkpoint, or milestone gate.
 */
public class Checkpoint extends Waypoint {

    public Checkpoint(int id, int mountainId, String name, double latitude, double longitude, int elevationM) {
        super(id, mountainId, name, latitude, longitude, elevationM);
    }

    @Override
    public String getWaypointType() {
        return "checkpoint";
    }

    @Override
    public String describe() {
        return String.format("Checkpoint / Gate: %s (Elevation: %dm)", getName(), getElevationM());
    }
}
