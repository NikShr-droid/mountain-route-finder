package com.routefinder.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies defensive encapsulation, immutability, and constructor validation
 * across domain models (Requirement R1).
 */
public class ModelEncapsulationTest {

    @Test
    void routeSegmentValidatesSafetyScoreRange() {
        assertThrows(IllegalArgumentException.class, () ->
                new RouteSegment(1, 1, 10, 20, 5.0, 500, 0, null, "[]"));

        assertThrows(IllegalArgumentException.class, () ->
                new RouteSegment(1, 1, 10, 20, 5.0, 500, 6, null, "[]"));

        assertDoesNotThrow(() ->
                new RouteSegment(1, 1, 10, 20, 5.0, 500, 1, null, "[]"));

        assertDoesNotThrow(() ->
                new RouteSegment(1, 1, 10, 20, 5.0, 500, 5, null, "[]"));
    }

    @Test
    void routeSegmentRejectsSelfReferencingEdge() {
        assertThrows(IllegalArgumentException.class, () ->
                new RouteSegment(1, 1, 10, 10, 5.0, 500, 2, null, "[]"));
    }

    @Test
    void routeSegmentRejectsNegativeDistanceOrGain() {
        assertThrows(IllegalArgumentException.class, () ->
                new RouteSegment(1, 1, 10, 20, -1.0, 500, 2, null, "[]"));

        assertThrows(IllegalArgumentException.class, () ->
                new RouteSegment(1, 1, 10, 20, 5.0, -100, 2, null, "[]"));
    }

    @Test
    void waypointValidatesCoordinatesAndName() {
        assertThrows(IllegalArgumentException.class, () ->
                WaypointFactory.create(1, 1, "", "camp", 0.0, 0.0, 1000));

        assertThrows(IllegalArgumentException.class, () ->
                new Camp(1, 1, "Base", 95.0, 0.0, 1000));

        assertThrows(IllegalArgumentException.class, () ->
                new Camp(1, 1, "Base", 0.0, -190.0, 1000));
    }

    @Test
    void mountainValidatesNameAndElevation() {
        assertThrows(IllegalArgumentException.class, () ->
                new Mountain(1, "   "));

        assertThrows(IllegalArgumentException.class, () ->
                new Mountain(1, "Everest", "Nepal", -500, "extreme", "May", "Desc", null));
    }

    @Test
    void pathResultEnforcesImmutability() {
        List<RouteSegment> segments = new ArrayList<>();
        RouteSegment seg1 = new RouteSegment(1, 1, 10, 20, 3.5, 400, 2, 60, "[]");
        segments.add(seg1);

        PathResult result = new PathResult(segments, 3.5);

        // Defensively copied
        segments.clear();
        assertEquals(1, result.getSegments().size());

        // Unmodifiable view
        assertThrows(UnsupportedOperationException.class, () ->
                result.getSegments().add(seg1));
    }

    @Test
    void waypointHierarchyPolymorphismAndFactory() {
        Waypoint trailhead = WaypointFactory.create(1, 1, "Moshi Gate", "trailhead", -3.2, 37.3, 1600);
        Waypoint camp = WaypointFactory.create(2, 1, "Machame Camp", "camp", -3.19, 37.33, 2850);
        Waypoint summit = WaypointFactory.create(3, 1, "Uhuru Peak", "summit", -3.07, 37.35, 5895);

        assertEquals("trailhead", trailhead.getWaypointType());
        assertEquals("camp", camp.getWaypointType());
        assertEquals("summit", summit.getWaypointType());

        // Polymorphic predicate check
        org.junit.jupiter.api.Assertions.assertTrue(trailhead.isTrailhead());
        org.junit.jupiter.api.Assertions.assertFalse(camp.isTrailhead());
        org.junit.jupiter.api.Assertions.assertTrue(summit.isSummit());
        org.junit.jupiter.api.Assertions.assertFalse(trailhead.isSummit());

        // Polymorphic describe() check
        org.junit.jupiter.api.Assertions.assertTrue(trailhead.describe().contains("Trailhead"));
        org.junit.jupiter.api.Assertions.assertTrue(summit.describe().contains("Summit"));
    }
}
