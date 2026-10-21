package com.routefinder.algorithm;

import com.routefinder.exception.RouteNotFoundException;
import com.routefinder.model.CostFunction;
import com.routefinder.model.PathResult;
import com.routefinder.model.RouteSegment;

import java.util.LinkedList;
import java.util.Map;

/**
 * Abstract base class for all pathfinding algorithms operating over a mountain trail {@link Graph}.
 *
 * <p><b>OOP & Viva Note (Inheritance & Abstraction):</b>
 * Defines the core contract for finding optimal ascent paths under different optimization criteria
 * ({@link CostFunction}). Subclasses ({@link Dijkstra}, {@link AStarAlgorithm}) implement their own
 * graph exploration strategies while sharing common path reconstruction logic via the template method
 * {@link #reconstructPath(Map, int, int, double)}.</p>
 */
public abstract class PathfindingAlgorithm {

    /**
     * Returns the human-readable name of the algorithm (e.g. "Dijkstra's Algorithm", "A* Search").
     */
    public abstract String getName();

    /**
     * Finds the optimal path from startWaypointId to endWaypointId under the given cost function.
     *
     * @param graph           the waypoint graph
     * @param startWaypointId origin waypoint ID (typically trailhead)
     * @param endWaypointId   target waypoint ID (typically summit)
     * @param costFunction    optimization objective (DISTANCE, ELEVATION_GAIN, or SAFETY)
     * @return PathResult containing the ordered route segments and total cost
     * @throws RouteNotFoundException if no path exists between the start and end waypoints
     */
    public abstract PathResult findRoute(Graph graph, int startWaypointId, int endWaypointId,
                                         CostFunction costFunction) throws RouteNotFoundException;

    /**
     * Overloaded convenience method defaulting to {@link CostFunction#DISTANCE}.
     *
     * <p><b>OOP Note (Method Overloading):</b> Provides an intuitive default for callers
     * who simply want the shortest physical distance without specifying a CostFunction.</p>
     */
    public PathResult findRoute(Graph graph, int startWaypointId, int endWaypointId)
            throws RouteNotFoundException {
        return findRoute(graph, startWaypointId, endWaypointId, CostFunction.DISTANCE);
    }

    /**
     * Shared path reconstruction routine: walks backwards from the target waypoint
     * using the cameFrom predecessor map to produce an ordered sequence of segments.
     *
     * @param cameFrom        map of waypoint ID -> edge used to reach that waypoint
     * @param startWaypointId starting waypoint ID
     * @param endWaypointId   destination waypoint ID
     * @param totalCost       accumulated cost under the chosen cost function
     * @return PathResult with segments in forward order (start to finish)
     */
    protected PathResult reconstructPath(Map<Integer, RouteSegment> cameFrom,
                                         int startWaypointId, int endWaypointId,
                                         double totalCost) {
        LinkedList<RouteSegment> path = new LinkedList<>();
        int currentId = endWaypointId;

        while (currentId != startWaypointId && cameFrom.containsKey(currentId)) {
            RouteSegment edge = cameFrom.get(currentId);
            path.addFirst(edge);
            currentId = edge.getFromWaypointId();
        }

        return new PathResult(path, totalCost);
    }

    /**
     * Evaluates the weight/cost of a segment polymorphically via the active CostFunction.
     */
    protected double costOf(RouteSegment segment, CostFunction costFunction) {
        return costFunction.cost(segment);
    }

    @Override
    public String toString() {
        return getName();
    }
}
