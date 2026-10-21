package com.routefinder.model;

/**
 * Domain object for a directed trail segment, mirroring `route_segments`.
 * Always uphill: fromWaypointId -> toWaypointId.
 */
public class RouteSegment {

    private final int id;
    private final int mountainId;
    private final int fromWaypointId;
    private final int toWaypointId;
    private final double distanceKm;
    private final int elevationGainM;
    private final int safetyScore;       // 1 (safest) - 5 (most dangerous)
    private final Integer estimatedTimeMin; // nullable
    private final String geometryJson;   // JSON array of [lat,lng] points

    public RouteSegment(int id, int mountainId, int fromWaypointId, int toWaypointId,
                         double distanceKm, int elevationGainM, int safetyScore,
                         Integer estimatedTimeMin, String geometryJson) {
        if (fromWaypointId == toWaypointId) {
            throw new IllegalArgumentException("Self-referencing segment not allowed: fromWaypointId and toWaypointId are both " + fromWaypointId);
        }
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative: " + distanceKm);
        }
        if (elevationGainM < 0) {
            throw new IllegalArgumentException("Elevation gain cannot be negative: " + elevationGainM);
        }
        if (safetyScore < 1 || safetyScore > 5) {
            throw new IllegalArgumentException("Safety score must be between 1 and 5 (inclusive), got: " + safetyScore);
        }
        if (geometryJson == null) {
            throw new IllegalArgumentException("geometryJson cannot be null");
        }

        this.id = id;
        this.mountainId = mountainId;
        this.fromWaypointId = fromWaypointId;
        this.toWaypointId = toWaypointId;
        this.distanceKm = distanceKm;
        this.elevationGainM = elevationGainM;
        this.safetyScore = safetyScore;
        this.estimatedTimeMin = estimatedTimeMin;
        this.geometryJson = geometryJson;
    }

    public int getId() { return id; }
    public int getMountainId() { return mountainId; }
    public int getFromWaypointId() { return fromWaypointId; }
    public int getToWaypointId() { return toWaypointId; }
    public double getDistanceKm() { return distanceKm; }
    public int getElevationGainM() { return elevationGainM; }
    public int getSafetyScore() { return safetyScore; }
    public Integer getEstimatedTimeMin() { return estimatedTimeMin; }
    public String getGeometryJson() { return geometryJson; }
}
