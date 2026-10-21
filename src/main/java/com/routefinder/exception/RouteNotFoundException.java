package com.routefinder.exception;

/**
 * Checked exception thrown when no viable trail exists between the specified
 * start waypoint and destination waypoint in the mountain graph.
 *
 * <p><b>OOP & Viva Note:</b> This is intentionally a <i>checked exception</i>
 * (extends {@link Exception}) rather than an unchecked RuntimeException. In a route
 * finding system, disconnected waypoints are an expected real-world situation
 * (e.g. unseeded trail data, closed paths), not a programming bug. Making it checked
 * obligates caller code (like UI controllers) to handle the condition gracefully.</p>
 */
public class RouteNotFoundException extends Exception {

    private final int startWaypointId;
    private final int endWaypointId;

    public RouteNotFoundException(int startWaypointId, int endWaypointId) {
        super(String.format("No route exists connecting waypoint #%d to waypoint #%d", startWaypointId, endWaypointId));
        this.startWaypointId = startWaypointId;
        this.endWaypointId = endWaypointId;
    }

    public RouteNotFoundException(String message) {
        super(message);
        this.startWaypointId = -1;
        this.endWaypointId = -1;
    }

    public int getStartWaypointId() {
        return startWaypointId;
    }

    public int getEndWaypointId() {
        return endWaypointId;
    }
}
