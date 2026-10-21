package com.routefinder.algorithm;

import com.routefinder.exception.RouteNotFoundException;
import com.routefinder.model.Camp;
import com.routefinder.model.CostFunction;
import com.routefinder.model.PathResult;
import com.routefinder.model.RouteSegment;
import com.routefinder.model.Summit;
import com.routefinder.model.Trailhead;
import com.routefinder.model.Waypoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link AStarAlgorithm} and polymorphic interchangeability with {@link Dijkstra}.
 */
public class AStarTest {

    private final PathfindingAlgorithm astar = new AStarAlgorithm();
    private final PathfindingAlgorithm dijkstra = new Dijkstra();

    @Test
    void findsOptimalPathMatchingDijkstra() throws Exception {
        // Realistic coordinates where straight-line distance is <= trail distance (admissible heuristic)
        List<Waypoint> waypoints = List.of(
                new Trailhead(1, 1, "Base", 0.0, 0.0, 1000),
                new Camp(2, 1, "Camp 1", 0.005, 0.005, 2000),
                new Camp(3, 1, "Camp 2", 0.005, 0.005, 2200),
                new Summit(4, 1, "Summit", 0.01, 0.01, 3500)
        );

        List<RouteSegment> segments = List.of(
                seg(1, 1, 2, 5.0, 500, 1),
                seg(2, 2, 4, 5.0, 500, 1),
                seg(3, 1, 3, 2.0, 800, 5),
                seg(4, 3, 4, 2.0, 800, 5)
        );

        Graph graph = new Graph(segments, waypoints);

        PathResult astarResult = astar.findRoute(graph, 1, 4, CostFunction.DISTANCE);
        PathResult dijkstraResult = dijkstra.findRoute(graph, 1, 4, CostFunction.DISTANCE);

        assertNotNull(astarResult);
        assertEquals(dijkstraResult.getTotalCost(), astarResult.getTotalCost(), 0.0001);
        assertEquals(4.0, astarResult.getTotalCost(), 0.0001);
        assertEquals(2, astarResult.getSegments().size());
    }

    @Test
    void findsSaferPathUnderSafetyCost() throws Exception {
        List<RouteSegment> segments = List.of(
                seg(1, 1, 2, 5.0, 500, 1),
                seg(2, 2, 4, 5.0, 500, 1),
                seg(3, 1, 3, 2.0, 800, 5),
                seg(4, 3, 4, 2.0, 800, 5)
        );

        Graph graph = new Graph(segments); // Without waypoints, h(n) = 0, degenerates to Dijkstra

        PathResult result = astar.findRoute(graph, 1, 4, CostFunction.SAFETY);
        assertNotNull(result);
        assertEquals(2.0, result.getTotalCost(), 0.0001);
        assertEquals(2, result.getSegments().get(0).getToWaypointId());
    }

    @Test
    void throwsRouteNotFoundExceptionWhenDisconnected() {
        Graph graph = new Graph(List.of(seg(1, 1, 2, 3.0, 300, 2)));

        assertThrows(RouteNotFoundException.class, () ->
                astar.findRoute(graph, 1, 4, CostFunction.DISTANCE));
    }

    private RouteSegment seg(int id, int from, int to, double distanceKm, int elevationGainM, int safetyScore) {
        return new RouteSegment(id, 1, from, to, distanceKm, elevationGainM, safetyScore, null, "[]");
    }
}
