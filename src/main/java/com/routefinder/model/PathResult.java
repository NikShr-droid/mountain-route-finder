package com.routefinder.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the result of a pathfinding search: the ordered trail segments
 * traversed and the accumulated cost under the selected CostFunction.
 *
 * Immutable by design: segment list is defensively copied and wrapped in an
 * unmodifiable view to prevent external mutation.
 */
public class PathResult {

    private final List<RouteSegment> segments;
    private final double totalCost;

    public PathResult(List<RouteSegment> segments, double totalCost) {
        if (segments == null) {
            throw new IllegalArgumentException("Segments list cannot be null");
        }
        if (totalCost < 0) {
            throw new IllegalArgumentException("Total cost cannot be negative: " + totalCost);
        }
        this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
        this.totalCost = totalCost;
    }

    public List<RouteSegment> getSegments() {
        return segments;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public int getSegmentCount() {
        return segments.size();
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("PathResult[segments=%d, totalCost=%.2f]", segments.size(), totalCost);
    }
}
