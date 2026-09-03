package com.routefinder.db;

import com.routefinder.algorithm.Dijkstra;
import com.routefinder.algorithm.Graph;
import com.routefinder.model.CostFunction;
import com.routefinder.model.Mountain;
import com.routefinder.model.RouteSegment;
import com.routefinder.model.Waypoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test: exercises MountainDao/WaypointDao/RouteSegmentDao
 * against the REAL SQLite database (data/mountainroutes.db), which
 * DatabaseManager auto-creates and auto-seeds from schema.sql +
 * seed-data.sql the first time getInstance() is called.
 *
 * Unlike DijkstraTest (hardcoded in-memory data), this verifies the
 * actual round trip: JDBC -> ResultSet -> domain objects -> Graph -> Dijkstra,
 * using the real Kilimanjaro Machame Route seed data.
 */
public class DaoIntegrationTest {

    private Mountain getKilimanjaro() {
        return new MountainDao().getAllMountains().stream()
                .filter(m -> m.getName().equals("Kilimanjaro"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Kilimanjaro not found — did seed-data.sql load?"));
    }

    @Test
    void mountainDaoReturnsSeededKilimanjaro() {
        List<Mountain> mountains = new MountainDao().getAllMountains();

        assertFalse(mountains.isEmpty(), "Expected at least one seeded mountain");
        Mountain kilimanjaro = getKilimanjaro();
        assertEquals(5895, kilimanjaro.getElevationM());
        assertEquals("Tanzania", kilimanjaro.getRegion());
    }

    @Test
    void waypointDaoReturnsSevenKilimanjaroWaypoints() {
        Mountain kilimanjaro = getKilimanjaro();
        List<Waypoint> waypoints = new WaypointDao().getWaypointsForMountain(kilimanjaro.getId());

        assertEquals(7, waypoints.size());
        assertTrue(waypoints.stream().anyMatch(
                w -> w.getName().equals("Machame Gate") && w.getWaypointType().equals("trailhead")));
        assertTrue(waypoints.stream().anyMatch(
                w -> w.getName().equals("Uhuru Peak") && w.getWaypointType().equals("summit")));
    }

    @Test
    void routeSegmentDaoReturnsSixSegmentsWithCorrectTotalDistance() {
        Mountain kilimanjaro = getKilimanjaro();
        List<RouteSegment> segments = new RouteSegmentDao().getSegmentsForMountain(kilimanjaro.getId());

        assertEquals(6, segments.size());
        double totalDistance = segments.stream().mapToDouble(RouteSegment::getDistanceKm).sum();
        assertEquals(40.0, totalDistance, 0.01);
    }

    @Test
    void dijkstraFindsRealRouteFromDatabaseData() {
        Mountain kilimanjaro = getKilimanjaro();
        List<Waypoint> waypoints = new WaypointDao().getWaypointsForMountain(kilimanjaro.getId());
        List<RouteSegment> segments = new RouteSegmentDao().getSegmentsForMountain(kilimanjaro.getId());

        int trailheadId = waypoints.stream()
                .filter(w -> w.getWaypointType().equals("trailhead"))
                .findFirst().orElseThrow().getId();
        int summitId = waypoints.stream()
                .filter(w -> w.getWaypointType().equals("summit"))
                .findFirst().orElseThrow().getId();

        Graph graph = new Graph(segments);
        Dijkstra.PathResult result = Dijkstra.findRoute(graph, trailheadId, summitId, CostFunction.DISTANCE);

        assertNotNull(result, "Expected a real route from Machame Gate to Uhuru Peak");
        assertEquals(6, result.segments.size());
        assertEquals(40.0, result.totalCost, 0.01);
    }
}
