package com.routefinder.algorithm;

import com.routefinder.model.RouteSegment;
import com.routefinder.model.Waypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Directed waypoint graph for a mountain ascent route.
 *
 * <p>Edges represent uphill {@link RouteSegment} connections between waypoints.
 * The graph maintains an adjacency list mapping each waypoint ID to its outgoing
 * trail segments, as well as an optional lookup of {@link Waypoint} entities for
 * coordinate and elevation queries.</p>
 */
public class Graph {

    private final Map<Integer, List<RouteSegment>> adjacency = new HashMap<>();
    private final Map<Integer, Waypoint> waypoints = new HashMap<>();

    /**
     * Constructs a graph from a list of route segments.
     */
    public Graph(List<RouteSegment> segments) {
        this(segments, Collections.emptyList());
    }

    /**
     * Overloaded constructor accepting both route segments and waypoint metadata.
     * Waypoint coordinates enable geometric heuristics (e.g. A* haversine estimation).
     *
     * <p><b>OOP Note (Constructor Overloading):</b> Allows callers to supply optional
     * waypoint domain objects without breaking callers who only have edge segments.</p>
     */
    public Graph(List<RouteSegment> segments, List<Waypoint> waypoints) {
        if (segments != null) {
            for (RouteSegment segment : segments) {
                adjacency
                        .computeIfAbsent(segment.getFromWaypointId(), k -> new ArrayList<>())
                        .add(segment);
            }
        }
        if (waypoints != null) {
            for (Waypoint waypoint : waypoints) {
                this.waypoints.put(waypoint.getId(), waypoint);
            }
        }
    }

    /**
     * Returns the outgoing (uphill) segments originating from the specified waypoint.
     */
    public List<RouteSegment> getEdgesFrom(int waypointId) {
        return adjacency.getOrDefault(waypointId, Collections.emptyList());
    }

    /**
     * Looks up waypoint metadata by ID, or returns null if waypoints were not supplied.
     */
    public Waypoint getWaypoint(int waypointId) {
        return waypoints.get(waypointId);
    }

    /**
     * Returns the total count of vertices with outgoing edges.
     */
    public int getVertexCount() {
        return adjacency.size();
    }
}
