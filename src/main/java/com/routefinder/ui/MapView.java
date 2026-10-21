package com.routefinder.ui;

import com.routefinder.algorithm.Dijkstra;
import com.routefinder.algorithm.Graph;
import com.routefinder.algorithm.PathfindingAlgorithm;
import com.routefinder.db.MountainDao;
import com.routefinder.db.RouteSegmentDao;
import com.routefinder.db.WaypointDao;
import com.routefinder.exception.RouteNotFoundException;
import com.routefinder.exception.UnseededMountainException;
import com.routefinder.model.CostFunction;
import com.routefinder.model.Mountain;
import com.routefinder.model.PathResult;
import com.routefinder.model.RouteSegment;
import com.routefinder.model.Waypoint;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elevation Profile & Topographic Chart Screen.
 *
 * <p>Styled with an Alpine-inspired theme (snow white & glacial blue in light mode,
 * starlit midnight blues in dark mode). Plots the vertical ascent profile.</p>
 */
public class MapView extends VBox {

    private final ComboBox<Mountain> mountainCombo = new ComboBox<>();
    private final ComboBox<CostFunction> costCombo = new ComboBox<>();
    private final Button renderBtn = new Button("Plot Profile");
    private final AreaChart<Number, Number> chart;
    private final VBox waypointListContainer = new VBox(8);
    private final Label statusLabel = new Label();
    private final WaypointDao waypointDao = new WaypointDao();

    public MapView() {
        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + ThemeManager.getBgPrimary() + ";");

        // Header Title
        Label title = new Label("Trail Elevation Profile");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Label subtitle = new Label("Visualize trail topography, vertical elevation gains, and camp milestones.");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        VBox header = new VBox(4, title, subtitle);

        // Controls
        loadMountains();
        mountainCombo.setPromptText("Select a Mountain...");
        costCombo.getItems().setAll(CostFunction.values());
        costCombo.getSelectionModel().select(CostFunction.DISTANCE);

        renderBtn.setStyle(
                "-fx-background-color: #0284c7;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 7 16;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        renderBtn.setOnAction(e -> renderElevationProfile());
        mountainCombo.setOnAction(e -> onMountainChange());

        Label lblMtn = new Label("Mountain:");
        lblMtn.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + "; -fx-font-weight: bold;");

        Label lblCrit = new Label("Criteria:");
        lblCrit.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + "; -fx-font-weight: bold;");

        HBox controls = new HBox(12,
                lblMtn, mountainCombo,
                lblCrit, costCombo,
                renderBtn
        );
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));
        controls.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        // Chart setup
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Distance (km)");
        xAxis.setAutoRanging(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Elevation (m)");
        yAxis.setAutoRanging(true);

        chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("Ascent Elevation Profile");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        VBox.setVgrow(chart, Priority.ALWAYS);

        // Waypoint Timeline Panel
        Label waypointTitle = new Label("Waypoints & Elevation Milestones");
        waypointTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        waypointTitle.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        ScrollPane scrollTimeline = new ScrollPane(waypointListContainer);
        scrollTimeline.setFitToWidth(true);
        scrollTimeline.setPrefHeight(150);
        scrollTimeline.setMaxHeight(180);
        scrollTimeline.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 6;"
        );

        statusLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        getChildren().addAll(header, controls, chart, waypointTitle, scrollTimeline, statusLabel);

        // Initial state
        onMountainChange();
    }

    private void loadMountains() {
        List<Mountain> mountains = new MountainDao().getAllMountains();
        mountainCombo.getItems().setAll(mountains);

        mountainCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Mountain item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    boolean seeded = waypointDao.hasRouteData(item.getId());
                    setText(item.getName() + (seeded ? "  ✓" : "  (Catalog Only)"));
                }
            }
        });
        mountainCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Mountain item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    boolean seeded = waypointDao.hasRouteData(item.getId());
                    setText(item.getName() + (seeded ? "  ✓" : "  (Catalog Only)"));
                }
            }
        });
    }

    private void onMountainChange() {
        Mountain selected = mountainCombo.getValue();
        if (selected == null) {
            chart.getData().clear();
            renderBtn.setDisable(true);
            renderBtn.setOpacity(0.5);
            showInitialEmptyState();
            statusLabel.setText("Select a mountain from the dropdown above to view its elevation profile.");
            statusLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");
            return;
        }

        boolean isSeeded = waypointDao.hasRouteData(selected.getId());
        if (!isSeeded) {
            chart.getData().clear();
            renderBtn.setDisable(true);
            renderBtn.setOpacity(0.5);
            showUnseededProfileState(selected);
            statusLabel.setText("⚠️ Route data not seeded for " + selected.getName() + " — elevation profile unavailable.");
            statusLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDark() ? "#fcd34d" : "#b45309") + "; -fx-font-weight: bold;");
        } else {
            renderBtn.setDisable(false);
            renderBtn.setOpacity(1.0);
            statusLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");
            renderElevationProfile();
        }
    }

    private void showInitialEmptyState() {
        waypointListContainer.getChildren().clear();
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24));

        Label icon = new Label("📈");
        icon.setFont(Font.font("System", 20));

        Label title = new Label("No Mountain Selected");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Label subtitle = new Label("Choose a mountain peak from the dropdown above and click 'Plot Profile' to visualize trail topography.");
        subtitle.setFont(Font.font("System", 11));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + "; -fx-text-alignment: center;");

        box.getChildren().addAll(icon, title, subtitle);
        waypointListContainer.getChildren().add(box);
    }

    private void renderElevationProfile() {
        Mountain selected = mountainCombo.getValue();
        CostFunction costFunction = costCombo.getValue();

        if (selected == null || costFunction == null) {
            statusLabel.setText("Please select a mountain and cost function.");
            return;
        }

        try {
            waypointDao.validateMountainSeeded(selected);
        } catch (UnseededMountainException e) {
            chart.getData().clear();
            showUnseededProfileState(selected);
            AlertHelper.showUnseededMountainWarning(selected);
            return;
        }

        List<Waypoint> waypoints = waypointDao.getWaypointsForMountain(selected.getId());
        List<RouteSegment> segments = new RouteSegmentDao().getSegmentsForMountain(selected.getId());

        if (waypoints.isEmpty() || segments.isEmpty()) {
            chart.getData().clear();
            showUnseededProfileState(selected);
            statusLabel.setText("No route data seeded for " + selected.getName() + " yet.");
            return;
        }

        Integer trailheadId = waypoints.stream().filter(Waypoint::isTrailhead).findFirst().map(Waypoint::getId).orElse(null);
        Integer summitId = waypoints.stream().filter(Waypoint::isSummit).findFirst().map(Waypoint::getId).orElse(null);

        if (trailheadId == null || summitId == null) {
            chart.getData().clear();
            waypointListContainer.getChildren().clear();
            statusLabel.setText("⚠️ Mountain is missing trailhead or summit waypoint.");
            AlertHelper.showErrorAlert("Profile Error", null,
                    "Trailhead or summit waypoint is missing for " + selected.getName() + ".");
            return;
        }

        Map<Integer, Waypoint> waypointMap = new HashMap<>();
        for (Waypoint wp : waypoints) {
            waypointMap.put(wp.getId(), wp);
        }

        PathfindingAlgorithm algorithm = new Dijkstra();
        PathResult result;
        try {
            result = algorithm.findRoute(new Graph(segments, waypoints), trailheadId, summitId, costFunction);
        } catch (RouteNotFoundException e) {
            chart.getData().clear();
            showNoRouteFoundState(selected);
            statusLabel.setText("⚠️ No route found connecting base to summit.");
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            return;
        }

        // Plot data series
        chart.getData().clear();
        waypointListContainer.getChildren().clear();

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(costFunction.name());

        double cumulativeDistance = 0.0;
        Waypoint start = waypointMap.get(trailheadId);
        if (start != null) {
            series.getData().add(new XYChart.Data<>(0.0, start.getElevationM()));
            addMilestoneRow(start, 0.0);
        }

        for (RouteSegment segment : result.getSegments()) {
            cumulativeDistance += segment.getDistanceKm();
            Waypoint to = waypointMap.get(segment.getToWaypointId());
            if (to != null) {
                series.getData().add(new XYChart.Data<>(cumulativeDistance, to.getElevationM()));
                addMilestoneRow(to, cumulativeDistance);
            }
        }

        chart.getData().add(series);
        statusLabel.setText(String.format("Showing elevation profile for %s (%s): Total %.1f km, Final Elevation %,d m",
                selected.getName(), costFunction, cumulativeDistance, selected.getElevationM()));
        statusLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");
    }

    private void showUnseededProfileState(Mountain mountain) {
        waypointListContainer.getChildren().clear();
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Label icon = new Label("⚠️");
        icon.setFont(Font.font("System", 20));

        Label title = new Label("Profile Unavailable");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: " + (ThemeManager.isDark() ? "#fcd34d" : "#b45309") + ";");

        Label subtitle = new Label("Profile for " +
                (mountain != null ? mountain.getName() : "this mountain") + " is currently not available.");
        subtitle.setFont(Font.font("System", 11));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + "; -fx-text-alignment: center;");

        box.getChildren().addAll(icon, title, subtitle);
        waypointListContainer.getChildren().add(box);
    }

    private void showNoRouteFoundState(Mountain mountain) {
        waypointListContainer.getChildren().clear();
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Label icon = new Label("❌");
        icon.setFont(Font.font("System", 20));

        Label title = new Label("No Route Found");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: #ef4444;");

        Label subtitle = new Label("No route connects the trailhead to the summit for " +
                (mountain != null ? mountain.getName() : "this mountain") + ".");
        subtitle.setFont(Font.font("System", 11));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + "; -fx-text-alignment: center;");

        box.getChildren().addAll(icon, title, subtitle);
        waypointListContainer.getChildren().add(box);
    }

    private void addMilestoneRow(Waypoint waypoint, double distanceKm) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 8, 4, 8));
        row.setStyle("-fx-border-color: " + (ThemeManager.isDark() ? "#1e293b" : "#f1f5f9") + "; -fx-border-width: 0 0 1 0;");

        Label badge = new Label(waypoint.getWaypointType().toUpperCase());
        badge.setFont(Font.font("System", FontWeight.BOLD, 9));
        badge.setPadding(new Insets(2, 6, 2, 6));

        if (waypoint.isTrailhead()) {
            badge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 4;");
        } else if (waypoint.isSummit()) {
            badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-background-radius: 4;");
        } else {
            badge.setStyle(ThemeManager.isDark()
                    ? "-fx-background-color: #1e293b; -fx-text-fill: #94a3b8; -fx-background-radius: 4;"
                    : "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 4;");
        }

        Label name = new Label(waypoint.getName());
        name.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        name.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label dist = new Label(String.format("%.1f km", distanceKm));
        dist.setFont(Font.font("System", 11));
        dist.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        Label ele = new Label(String.format("%,d m", waypoint.getElevationM()));
        ele.setFont(Font.font("System", FontWeight.BOLD, 11));
        ele.setStyle("-fx-text-fill: " + ThemeManager.getIceAccent() + ";");

        row.getChildren().addAll(badge, name, dist, ele);
        waypointListContainer.getChildren().add(row);
    }
}
