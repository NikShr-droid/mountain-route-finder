package com.routefinder.algorithm;

import com.routefinder.model.CostFunction;
import com.routefinder.model.RouteSegment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Dijkstra's algorithm over a Graph, parameterized by CostFunction.
 * UI-independent -- see DijkstraTest for hardcoded-data verification.
 */
public class Dijkstra {

    /** A computed route: the ordered segments walked, and their total cost. */
    public static class PathResult {
        public final List<RouteSegment> segments;
        public final double totalCost;

        public PathResult(List<RouteSegment> segments, double totalCost) {
            this.segments = segments;
            this.totalCost = totalCost;
        }
    }

    private record Entry(int waypointId, double cost) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry other) {
            return Double.compare(cost, other.cost);
        }
    }

    /**
     * Finds the lowest-cost path from startWaypointId to endWaypointId under
     * the given cost function. Returns null if no path exists.
     */
    public static PathResult findRoute(Graph graph, int startWaypointId, int endWaypointId,
                                        CostFunction costFunction) {
        Map<Integer, Double> bestCost = new HashMap<>();
        Map<Integer, RouteSegment> cameFrom = new HashMap<>();
        PriorityQueue<Entry> queue = new PriorityQueue<>();

        bestCost.put(startWaypointId, 0.0);
        queue.add(new Entry(startWaypointId, 0.0));

        while (!queue.isEmpty()) {
            Entry current = queue.poll();

            // Stale entry (a shorter path to this node was already found) -- skip it.
            if (current.cost() > bestCost.getOrDefault(current.waypointId(), Double.MAX_VALUE)) {
                continue;
            }
            if (current.waypointId() == endWaypointId) {
                break;
            }

            for (RouteSegment edge : graph.getEdgesFrom(current.waypointId())) {
                double newCost = current.cost() + costOf(edge, costFunction);
                int neighbor = edge.getToWaypointId();
                if (newCost < bestCost.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    bestCost.put(neighbor, newCost);
                    cameFrom.put(neighbor, edge);
                    queue.add(new Entry(neighbor, newCost));
                }
            }
        }

        if (startWaypointId != endWaypointId && !cameFrom.containsKey(endWaypointId)) {
            return null; // unreachable
        }

        // Walk backwards from the end, following cameFrom, to rebuild the path in order.
        LinkedList<RouteSegment> path = new LinkedList<>();
        int currentId = endWaypointId;
        while (currentId != startWaypointId) {
            RouteSegment edge = cameFrom.get(currentId);
            path.addFirst(edge);
            currentId = edge.getFromWaypointId();
        }

        return new PathResult(path, bestCost.get(endWaypointId));
    }

    /** Edge weight for a segment under the given cost function. */
    private static double costOf(RouteSegment segment, CostFunction costFunction) {
        return switch (costFunction) {
            case DISTANCE -> segment.getDistanceKm();
            case ELEVATION_GAIN -> segment.getElevationGainM();
            case SAFETY -> segment.getSafetyScore();
        };
    }
}
