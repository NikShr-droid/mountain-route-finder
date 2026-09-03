package com.routefinder.ui;

import com.routefinder.algorithm.Dijkstra;
import com.routefinder.algorithm.Graph;
import com.routefinder.db.MountainDao;
import com.routefinder.db.RouteSegmentDao;
import com.routefinder.db.WaypointDao;
import com.routefinder.model.CostFunction;
import com.routefinder.model.Mountain;
import com.routefinder.model.RouteSegment;
import com.routefinder.model.Waypoint;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Route Finder screen: pick a mountain, hit "Find Route", see all three
 * cost-function results side by side (Google-Maps-style multi-route selector).
 *
 * Fully wired end to end: mountains come from MountainDao, "Find Route" builds
 * a real Graph from WaypointDao/RouteSegmentDao and runs Dijkstra once per
 * CostFunction, and each card is updated with the real totals for the route
 * Dijkstra actually returned under that cost function.
 */
public class RouteFinderView extends VBox {

    private final ComboBox<Mountain> mountainCombo = new ComboBox<>();
    private final Label statusLabel = new Label();

    // One set of labels per cost-function card, so onFindRoute() can update
    // the matching card without rebuilding the UI each time.
    private final Map<CostFunction, Label> distanceLabels = new EnumMap<>(CostFunction.class);
    private final Map<CostFunction, Label> elevationLabels = new EnumMap<>(CostFunction.class);
    private final Map<CostFunction, Label> safetyLabels = new EnumMap<>(CostFunction.class);

    public RouteFinderView() {
        setSpacing(20);
        setPadding(new Insets(24));

        Label heading = new Label("Route Finder");
        heading.setFont(Font.font("System", FontWeight.BOLD, 22));

        loadMountains();
        mountainCombo.setPromptText("Select a mountain");

        Button findRouteButton = new Button("Find Route");
        findRouteButton.setOnAction(e -> onFindRoute());

        HBox controls = new HBox(12, mountainCombo, findRouteButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        HBox routeCards = new HBox(16,
                buildRouteCard("Shortest Distance", CostFunction.DISTANCE),
                buildRouteCard("Least Elevation Gain", CostFunction.ELEVATION_GAIN),
                buildRouteCard("Safest Terrain", CostFunction.SAFETY)
        );
        routeCards.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(heading, controls, routeCards, statusLabel);
    }

    /** Pulls real mountains from SQLite via MountainDao and auto-selects the first. */
    private void loadMountains() {
        List<Mountain> mountains = new MountainDao().getAllMountains();
        mountainCombo.getItems().setAll(mountains);
        if (!mountains.isEmpty()) {
            mountainCombo.getSelectionModel().selectFirst();
        }
    }

    /**
     * For the selected mountain: build a Graph from its real waypoints/segments,
     * find the trailhead and summit, then run Dijkstra once per CostFunction
     * and push the results into the matching card.
     */
    private void onFindRoute() {
        Mountain selected = mountainCombo.getValue();
        if (selected == null) {
            statusLabel.setText("Select a mountain first.");
            return;
        }

        List<Waypoint> waypoints = new WaypointDao().getWaypointsForMountain(selected.getId());
        List<RouteSegment> segments = new RouteSegmentDao().getSegmentsForMountain(selected.getId());

        if (waypoints.isEmpty() || segments.isEmpty()) {
            statusLabel.setText("No route data seeded for " + selected.getName() + " yet.");
            return;
        }

        Integer trailheadId = findWaypointIdByType(waypoints, "trailhead");
        Integer summitId = findWaypointIdByType(waypoints, "summit");
        if (trailheadId == null || summitId == null) {
            statusLabel.setText("Missing trailhead/summit waypoint for " + selected.getName() + ".");
            return;
        }

        Graph graph = new Graph(segments);
        for (CostFunction costFunction : CostFunction.values()) {
            Dijkstra.PathResult result = Dijkstra.findRoute(graph, trailheadId, summitId, costFunction);
            updateCard(costFunction, result);
        }

        statusLabel.setText("Showing routes for " + selected.getName() + ".");
    }

    private Integer findWaypointIdByType(List<Waypoint> waypoints, String type) {
        return waypoints.stream()
                .filter(w -> w.getWaypointType().equals(type))
                .findFirst()
                .map(Waypoint::getId)
                .orElse(null);
    }

    /** Writes the real totals for a computed route into its card's labels. */
    private void updateCard(CostFunction costFunction, Dijkstra.PathResult result) {
        Label distanceLabel = distanceLabels.get(costFunction);
        Label elevationLabel = elevationLabels.get(costFunction);
        Label safetyLabel = safetyLabels.get(costFunction);

        if (result == null) {
            distanceLabel.setText("Distance: no route found");
            elevationLabel.setText("Elevation gain: --");
            safetyLabel.setText("Safety score: --");
            return;
        }

        double totalDistance = result.segments.stream().mapToDouble(RouteSegment::getDistanceKm).sum();
        int totalElevation = result.segments.stream().mapToInt(RouteSegment::getElevationGainM).sum();
        int totalSafety = result.segments.stream().mapToInt(RouteSegment::getSafetyScore).sum();

        distanceLabel.setText(String.format("Distance: %.1f km", totalDistance));
        elevationLabel.setText("Elevation gain: " + totalElevation + " m");
        safetyLabel.setText("Safety score: " + totalSafety + " (cumulative)");
    }

    /**
     * Factory method for a single route-option card. Registers this card's
     * labels in the maps above, keyed by costFunction, so onFindRoute() can
     * find and update them later.
     */
    private VBox buildRouteCard(String title, CostFunction costFunction) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label distance = new Label("Distance: --");
        Label elevation = new Label("Elevation gain: --");
        Label safety = new Label("Safety score: --");

        distanceLabels.put(costFunction, distance);
        elevationLabels.put(costFunction, elevation);
        safetyLabels.put(costFunction, safety);

        VBox card = new VBox(6, titleLabel, distance, elevation, safety);
        card.setPadding(new Insets(16));
        card.setPrefWidth(220);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );
        card.setUserData(costFunction);

        return card;
    }
}