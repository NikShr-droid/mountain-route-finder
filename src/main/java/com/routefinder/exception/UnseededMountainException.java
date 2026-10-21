package com.routefinder.exception;

/**
 * Unchecked domain exception thrown when a route calculation, elevation profile,
 * or export is requested for a catalog mountain that has no seeded waypoint or segment data.
 *
 * <p><b>OOP & Architecture Note:</b>
 * Distinguishes unseeded catalog mountains (e.g. Everest, Denali) from algorithm-level
 * routing disconnects ({@link RouteNotFoundException}). Allows the presentation layer
 * to intercept user interactions and present informative error states.</p>
 */
public class UnseededMountainException extends RuntimeException {

    private final int mountainId;
    private final String mountainName;

    public UnseededMountainException(int mountainId, String mountainName) {
        super(String.format("Mountain '%s' (ID #%d) is in the catalog for exploration only; no route data has been seeded yet.", mountainName, mountainId));
        this.mountainId = mountainId;
        this.mountainName = mountainName;
    }

    public int getMountainId() {
        return mountainId;
    }

    public String getMountainName() {
        return mountainName;
    }
}
