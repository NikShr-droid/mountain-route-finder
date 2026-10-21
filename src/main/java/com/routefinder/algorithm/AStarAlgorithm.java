package com.routefinder.algorithm;

import com.routefinder.exception.RouteNotFoundException;
import com.routefinder.model.CostFunction;
import com.routefinder.model.PathResult;
import com.routefinder.model.RouteSegment;
import com.routefinder.model.Waypoint;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A* pathfinding algorithm over a {@link Graph} using an admissible heuristic.
 *
 * <p><b>OOP & Viva Note (Inheritance & Algorithm Design):</b>
 * Second concrete subclass of {@link PathfindingAlgorithm}. Employs best-first search guided by
 * evaluation function $f(n) = g(n) + h(n)$, where $g(n)$ is the exact cost from start to node $n$,
 * and $h(n)$ is a heuristic estimate from $n$ to the target summit.</p>
 *
 * <p><b>Academic Honesty on Admissibility:</b>
 * The straight-line Haversine distance is strictly <i>admissible</i> (never overestimates) for the
 * {@link CostFunction#DISTANCE} objective. For {@link CostFunction#ELEVATION_GAIN} and
 * {@link CostFunction#SAFETY}, geographic distance has incompatible units and physics;
 * hence, $h(n)$ deliberately returns 0.0 for those criteria, correctly degenerating A* to Dijkstra
 * while preserving mathematical optimality.</p>
 */
public class AStarAlgorithm extends PathfindingAlgorithm {

    private record AStarEntry(int waypointId, double fScore) implements Comparable<AStarEntry> {
        @Override
        public int compareTo(AStarEntry other) {
            return Double.compare(fScore, other.fScore);
        }
    }

    @Override
    public String getName() {
        return "A* Search";
    }

    @Override
    public PathResult findRoute(Graph graph, int startWaypointId, int endWaypointId,
                                CostFunction costFunction) throws RouteNotFoundException {
        // gScore: cheapest known cost from start to this waypoint
        Map<Integer, Double> gScore = new HashMap<>();
        Map<Integer, RouteSegment> cameFrom = new HashMap<>();
        PriorityQueue<AStarEntry> openSet = new PriorityQueue<>();

        Waypoint targetWaypoint = graph.getWaypoint(endWaypointId);

        gScore.put(startWaypointId, 0.0);
        double initialH = calculateHeuristic(graph.getWaypoint(startWaypointId), targetWaypoint, costFunction);
        openSet.add(new AStarEntry(startWaypointId, initialH));

        while (!openSet.isEmpty()) {
            AStarEntry current = openSet.poll();

            // Destination reached when target is popped from the priority queue
            if (current.waypointId() == endWaypointId) {
                break;
            }

            double currentG = gScore.getOrDefault(current.waypointId(), Double.MAX_VALUE);
            double currentH = calculateHeuristic(graph.getWaypoint(current.waypointId()), targetWaypoint, costFunction);
            if (current.fScore() > currentG + currentH + 1e-9) {
                continue; // Skip stale queue entry
            }

            for (RouteSegment edge : graph.getEdgesFrom(current.waypointId())) {
                int neighbor = edge.getToWaypointId();
                double tentativeG = gScore.get(current.waypointId()) + costOf(edge, costFunction);

                if (tentativeG < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, edge);
                    gScore.put(neighbor, tentativeG);

                    double h = calculateHeuristic(graph.getWaypoint(neighbor), targetWaypoint, costFunction);
                    double f = tentativeG + h;
                    openSet.add(new AStarEntry(neighbor, f));
                }
            }
        }

        // Verify that target was reached
        if (startWaypointId != endWaypointId && !cameFrom.containsKey(endWaypointId)) {
            throw new RouteNotFoundException(startWaypointId, endWaypointId);
        }

        return reconstructPath(cameFrom, startWaypointId, endWaypointId, gScore.getOrDefault(endWaypointId, 0.0));
    }

    /**
     * Calculates the heuristic estimate $h(n)$ to destination.
     * Guaranteed admissible ($h(n) \le h^*(n)$) to preserve A* optimality.
     */
    private double calculateHeuristic(Waypoint current, Waypoint target, CostFunction costFunction) {
        if (costFunction != CostFunction.DISTANCE || current == null || target == null) {
            return 0.0;
        }
        return haversineKm(current.getLatitude(), current.getLongitude(),
                target.getLatitude(), target.getLongitude());
    }

    /**
     * Great-circle Haversine formula computing straight-line spherical distance in kilometers.
     */
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
