package com.routefinder.model;

/**
 * The three route-optimization criteria a user can choose from.
 *
 * <p><b>OOP & Viva Note (Enum Constant-Specific Polymorphism):</b>
 * In Java, enums are specialized classes. By defining an abstract {@link #cost(RouteSegment)}
 * method with constant-specific method bodies, we replace procedural switch statements with
 * polymorphic dispatch. When the algorithm calls {@code costFunction.cost(edge)}, the JVM
 * resolves the cost dynamically based on the active constant.</p>
 */
public enum CostFunction {

    DISTANCE {
        @Override
        public double cost(RouteSegment segment) {
            return segment.getDistanceKm();
        }
    },

    ELEVATION_GAIN {
        @Override
        public double cost(RouteSegment segment) {
            return segment.getElevationGainM();
        }
    },

    SAFETY {
        @Override
        public double cost(RouteSegment segment) {
            return segment.getSafetyScore();
        }
    };

    /**
     * Polymorphically evaluates the weight/cost of a trail segment for this objective.
     */
    public abstract double cost(RouteSegment segment);
}
