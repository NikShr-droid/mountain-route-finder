package com.routefinder.algorithm;

import com.routefinder.exception.RouteNotFoundException;
import com.routefinder.model.CostFunction;
import com.routefinder.model.PathResult;
import com.routefinder.model.RouteSegment;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Implementation of Dijkstra's single-source shortest path algorithm over a {@link Graph}.
 *
 * <p><b>OOP & Viva Note (Inheritance & Polymorphism):</b>
 * Extends {@link PathfindingAlgorithm} and overrides {@link #findRoute(Graph, int, int, CostFunction)}.
 * Solves for non-negative edge weights (distance, elevation gain, and safety score are all $\ge 0$).
 * When invoked via a base class reference, the JVM dynamically dispatches to this implementation
 * at runtime.</p>
 */
public class Dijkstra extends PathfindingAlgorithm {

    private record Entry(int waypointId, double cost) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry other) {
            return Double.compare(cost, other.cost);
        }
    }

    @Override
    public String getName() {
        return "Dijkstra's Algorithm";
    }

    @Override
    public PathResult findRoute(Graph graph, int startWaypointId, int endWaypointId,
                                CostFunction costFunction) throws RouteNotFoundException {
        Map<Integer, Double> bestCost = new HashMap<>();
        Map<Integer, RouteSegment> cameFrom = new HashMap<>();
        PriorityQueue<Entry> queue = new PriorityQueue<>();

        bestCost.put(startWaypointId, 0.0);
        queue.add(new Entry(startWaypointId, 0.0));

        while (!queue.isEmpty()) {
            Entry current = queue.poll();

            // Skip stale priority queue entries if a lower-cost path was already settled
            if (current.cost() > bestCost.getOrDefault(current.waypointId(), Double.MAX_VALUE)) {
                continue;
            }

            // Early exit as soon as the target waypoint is popped from the priority queue
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

        // Verify that target was reached
        if (startWaypointId != endWaypointId && !cameFrom.containsKey(endWaypointId)) {
            throw new RouteNotFoundException(startWaypointId, endWaypointId);
        }

        return reconstructPath(cameFrom, startWaypointId, endWaypointId, bestCost.getOrDefault(endWaypointId, 0.0));
    }
}
