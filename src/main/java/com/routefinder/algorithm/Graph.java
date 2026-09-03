package com.routefinder.algorithm;

import com.routefinder.model.RouteSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Waypoint graph for a single mountain, built from its RouteSegment edges.
 * Adjacency list keyed by waypoint id -> outgoing (uphill) segments.
 */
public class Graph {

    private final Map<Integer, List<RouteSegment>> adjacency = new HashMap<>();

    public Graph(List<RouteSegment> segments) {
        for (RouteSegment segment : segments) {
            adjacency
                    .computeIfAbsent(segment.getFromWaypointId(), k -> new ArrayList<>())
                    .add(segment);
        }
    }

    /** Outgoing segments from a waypoint, or an empty list if it has none. */
    public List<RouteSegment> getEdgesFrom(int waypointId) {
        return adjacency.getOrDefault(waypointId, Collections.emptyList());
    }
}
