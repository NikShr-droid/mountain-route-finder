package com.routefinder.db;

import com.routefinder.algorithm.Dijkstra;
import com.routefinder.algorithm.Graph;
import com.routefinder.model.CostFunction;
import com.routefinder.model.Mountain;
import com.routefinder.model.PathResult;
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
 * using the demo seed data (3 routed mountains + browse-only dummy mountains).
 *
 * If you change seed-data.sql, the expected numbers below change with it.
 * Delete data/mountainroutes.db first so the seed reloads.
 */
public class DaoIntegrationTest {

    private Mountain getMountain(String name) {
        return new MountainDao().getAllMountains().stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError(name + " not found — did seed-data.sql load?"));
    }

    /**
     * Runs Dijkstra from the mountain's single trailhead (base node) to its
     * summit, asserts the total cost, and returns the name of the first
     * real gate/station the route goes through (i.e. which route was chosen).
     */
    private String firstStopAfterBase(String mountainName, CostFunction costFunction, double expectedCost) {
        Mountain mountain = getMountain(mountainName);
        List<Waypoint> waypoints = new WaypointDao().getWaypointsForMountain(mountain.getId());
        List<RouteSegment> segments = new RouteSegmentDao().getSegmentsForMountain(mountain.getId());

        int baseId = waypoints.stream()
                .filter(w -> w.getWaypointType().equals("trailhead"))
                .findFirst().orElseThrow().getId();
        int summitId = waypoints.stream()
                .filter(w -> w.getWaypointType().equals("summit"))
                .findFirst().orElseThrow().getId();

        PathResult result;
        try {
            result = new Dijkstra().findRoute(new Graph(segments, waypoints), baseId, summitId, costFunction);
        } catch (Exception e) {
            throw new AssertionError("Route finding failed unexpectedly for " + mountainName, e);
        }

        assertNotNull(result, "Expected a route for " + mountainName + " / " + costFunction);
        assertEquals(expectedCost, result.getTotalCost(), 0.01);

        int firstStopId = result.getSegments().get(0).getToWaypointId();
        return waypoints.stream()
                .filter(w -> w.getId() == firstStopId)
                .findFirst().orElseThrow().getName();
    }

    @Test
    void mountainDaoReturnsSeededMountains() {
        List<Mountain> mountains = new MountainDao().getAllMountains();

        assertFalse(mountains.isEmpty(), "Expected seeded mountains");
        assertTrue(mountains.size() >= 11, "Expected 3 routed + 8 dummy mountains");

        Mountain kilimanjaro = getMountain("Kilimanjaro");
        assertEquals(5895, kilimanjaro.getElevationM());
        assertEquals("Tanzania", kilimanjaro.getRegion());

        getMountain("Mont Blanc");
        getMountain("Mt Fuji");
    }

    @Test
    void waypointDaoReturnsSixteenKilimanjaroWaypoints() {
        Mountain kilimanjaro = getMountain("Kilimanjaro");
        List<Waypoint> waypoints = new WaypointDao().getWaypointsForMountain(kilimanjaro.getId());

        assertEquals(16, waypoints.size());
        assertTrue(waypoints.stream().anyMatch(
                w -> w.getName().equals("Moshi Base") && w.getWaypointType().equals("trailhead")));
        assertTrue(waypoints.stream().anyMatch(
                w -> w.getName().equals("Uhuru Peak") && w.getWaypointType().equals("summit")));
        assertEquals(1, waypoints.stream().filter(w -> w.getWaypointType().equals("trailhead")).count(),
                "RouteFinderView uses the first trailhead it finds, so there must be exactly one");
    }

    @Test
    void routeSegmentDaoReturnsSeventeenKilimanjaroSegmentsWithCorrectTotalDistance() {
        Mountain kilimanjaro = getMountain("Kilimanjaro");
        List<RouteSegment> segments = new RouteSegmentDao().getSegmentsForMountain(kilimanjaro.getId());

        assertEquals(17, segments.size());
        // Machame 40.0 + Marangu 35.5 + Lemosho 22.0 (transfers are 0 km)
        double totalDistance = segments.stream().mapToDouble(RouteSegment::getDistanceKm).sum();
        assertEquals(97.5, totalDistance, 0.01);
    }

    @Test
    void kilimanjaroCostFunctionsPickDifferentRoutes() {
        assertEquals("Marangu Gate",  firstStopAfterBase("Kilimanjaro", CostFunction.DISTANCE, 35.5));
        assertEquals("Londorot Gate", firstStopAfterBase("Kilimanjaro", CostFunction.ELEVATION_GAIN, 3856.0));
        assertEquals("Machame Gate",  firstStopAfterBase("Kilimanjaro", CostFunction.SAFETY, 17.0));
    }

    @Test
    void montBlancCostFunctionsPickExpectedRoutes() {
        assertEquals("Nid d'Aigle",      firstStopAfterBase("Mont Blanc", CostFunction.DISTANCE, 11.0));
        assertEquals("Aiguille du Midi", firstStopAfterBase("Mont Blanc", CostFunction.ELEVATION_GAIN, 1436.0));
        assertEquals("Aiguille du Midi", firstStopAfterBase("Mont Blanc", CostFunction.SAFETY, 16.0));
    }

    @Test
    void mtFujiCostFunctionsPickExpectedRoutes() {
        assertEquals("Fujinomiya 5th Station", firstStopAfterBase("Mt Fuji", CostFunction.DISTANCE, 5.5));
        assertEquals("Fujinomiya 5th Station", firstStopAfterBase("Mt Fuji", CostFunction.ELEVATION_GAIN, 1376.0));
        assertEquals("Yoshida 5th Station",    firstStopAfterBase("Mt Fuji", CostFunction.SAFETY, 9.0));
    }

    @Test
    void dummyMountainsHaveNoRouteData() {
        Mountain everest = getMountain("Everest");
        assertTrue(new WaypointDao().getWaypointsForMountain(everest.getId()).isEmpty());
        assertTrue(new RouteSegmentDao().getSegmentsForMountain(everest.getId()).isEmpty());
    }
}