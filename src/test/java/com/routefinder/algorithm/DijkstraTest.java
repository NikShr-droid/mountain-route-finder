package com.routefinder.algorithm;

import com.routefinder.model.CostFunction;
import com.routefinder.model.RouteSegment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for Dijkstra using hardcoded graph data (no UI, no DB) so the
 * algorithm can be verified in isolation before real seed data exists.
 *
 * Graph shape used below (waypoint ids):
 *   1 --(5km, +500m, safety 1)--> 2 --(5km, +500m, safety 1)--> 4
 *   1 --(2km, +800m, safety 5)--> 3 --(2km, +800m, safety 5)--> 4
 * i.e. the 1-3-4 path is shorter but less safe; 1-2-4 is longer but safer.
 */
public class DijkstraTest {

    @Test
    void findsShortestPathByDistance() {
        Graph graph = new Graph(sampleSegments());

        Dijkstra.PathResult result = Dijkstra.findRoute(graph, 1, 4, CostFunction.DISTANCE);

        assertNotNull(result);
        assertEquals(4.0, result.totalCost, 0.0001); // 2km + 2km via waypoint 3
        assertEquals(2, result.segments.size());
        assertEquals(3, result.segments.get(0).getToWaypointId());
    }

    @Test
    void findsSaferPathEvenIfLonger() {
        Graph graph = new Graph(sampleSegments());

        Dijkstra.PathResult result = Dijkstra.findRoute(graph, 1, 4, CostFunction.SAFETY);

        assertNotNull(result);
        assertEquals(2.0, result.totalCost, 0.0001); // safety 1 + 1 via waypoint 2
        assertEquals(2, result.segments.get(0).getToWaypointId());
    }

    @Test
    void returnsNullWhenNoPathExists() {
        // Only one edge, and it doesn't reach waypoint 4.
        Graph graph = new Graph(List.of(seg(1, 1, 2, 3.0, 300, 2)));

        Dijkstra.PathResult result = Dijkstra.findRoute(graph, 1, 4, CostFunction.DISTANCE);

        assertNull(result);
    }

    private List<RouteSegment> sampleSegments() {
        return List.of(
                seg(1, 1, 2, 5.0, 500, 1),
                seg(2, 2, 4, 5.0, 500, 1),
                seg(3, 1, 3, 2.0, 800, 5),
                seg(4, 3, 4, 2.0, 800, 5)
        );
    }

    private RouteSegment seg(int id, int from, int to, double distanceKm, int elevationGainM, int safetyScore) {
        return new RouteSegment(id, /* mountainId */ 1, from, to, distanceKm, elevationGainM, safetyScore, null, "[]");
    }
}
