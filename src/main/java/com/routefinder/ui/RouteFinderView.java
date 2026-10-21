package com.routefinder.ui;

import com.routefinder.algorithm.AStarAlgorithm;
import com.routefinder.algorithm.Dijkstra;
import com.routefinder.algorithm.Graph;
import com.routefinder.algorithm.PathfindingAlgorithm;
import com.routefinder.db.MountainDao;
import com.routefinder.db.RouteSegmentDao;
import com.routefinder.db.SavedRouteDao;
import com.routefinder.db.WaypointDao;
import com.routefinder.exception.ExportException;
import com.routefinder.exception.RouteNotFoundException;
import com.routefinder.exception.UnseededMountainException;
import com.routefinder.export.CsvExporter;
import com.routefinder.export.GpxExporter;
import com.routefinder.export.RouteExporter;
import com.routefinder.model.CostFunction;
import com.routefinder.model.Mountain;
import com.routefinder.model.PathResult;
import com.routefinder.model.RouteSegment;
import com.routefinder.model.SavedRoute;
import com.routefinder.model.UserSession;
import com.routefinder.model.Waypoint;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ascent Route Finder Screen.
 *
 * <p>Styled with an Alpine-inspired theme (snow white & glacial blue in light mode,
 * starlit midnight blues in dark mode). Features multi-objective path calculation,
 * comparison cards, and external file export.</p>
 */
public class RouteFinderView extends VBox {

    private final ComboBox<Mountain> mountainCombo = new ComboBox<>();
    private final ComboBox<PathfindingAlgorithm> algorithmCombo = new ComboBox<>();
    private final ComboBox<RouteExporter> exporterCombo = new ComboBox<>();
    private final Button findRouteButton = new Button("Find Routes");
    private final Button exportButton = new Button("Export Route");
    private final Button saveRouteBtn = new Button("⭐ Save Route");
    private final Button markSummitedBtn = new Button("✓ Mark Summited");
    private final Label statusLabel = new Label();
    private final WaypointDao waypointDao = new WaypointDao();
    private final SavedRouteDao savedRouteDao = new SavedRouteDao();

    // Inline warning banner for unseeded catalog peaks
    private final HBox unseededWarningBanner = new HBox(12);

    // Mountain detail banner
    private final HBox mountainInfoBanner = new HBox(16);
    private final ImageView mountainImageView = new ImageView();
    private final Label mountainTitleLabel = new Label();
    private final Label mountainMetaLabel = new Label();
    private final Label mountainDescLabel = new Label();

    // Route cards
    private final Map<CostFunction, VBox> cardBoxes = new EnumMap<>(CostFunction.class);
    private final Map<CostFunction, Label> distanceLabels = new EnumMap<>(CostFunction.class);
    private final Map<CostFunction, Label> elevationLabels = new EnumMap<>(CostFunction.class);
    private final Map<CostFunction, Label> safetyLabels = new EnumMap<>(CostFunction.class);

    // Itinerary panel
    private final VBox itineraryBox = new VBox(6);
    private CostFunction selectedCostFunction = CostFunction.DISTANCE;

    // Cache of latest computed results
    private final Map<CostFunction, PathResult> lastResults = new EnumMap<>(CostFunction.class);
    private List<Waypoint> lastWaypoints = Collections.emptyList();

    public RouteFinderView() {
        this(null);
    }

    public RouteFinderView(Mountain initialMountain) {
        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + ThemeManager.getBgPrimary() + ";");

        // Page Header
        Label heading = new Label("Ascent Route Finder");
        heading.setFont(Font.font("System", FontWeight.BOLD, 24));
        heading.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Label subtitle = new Label("Compute and compare optimal ascent routes across three Alpine optimization objectives.");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        VBox headerBox = new VBox(4, heading, subtitle);

        // Control Toolbar
        loadMountains();
        mountainCombo.setPromptText("Select a Mountain...");
        if (initialMountain != null) {
            for (Mountain m : mountainCombo.getItems()) {
                if (m.getId() == initialMountain.getId()) {
                    mountainCombo.getSelectionModel().select(m);
                    break;
                }
            }
        }
        mountainCombo.setOnAction(e -> updateMountainBanner());

        algorithmCombo.getItems().addAll(new Dijkstra(), new AStarAlgorithm());
        algorithmCombo.getSelectionModel().selectFirst();

        findRouteButton.setStyle(
                "-fx-background-color: #0284c7;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 7 16;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        findRouteButton.setOnAction(e -> onFindRoute());

        exporterCombo.getItems().addAll(new GpxExporter(), new CsvExporter());
        exporterCombo.getSelectionModel().selectFirst();

        exportButton.setStyle(
                "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#334155") + ";" +
                "-fx-text-fill: " + (ThemeManager.isDark() ? "#f8fafc" : "white") + ";" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 7 14;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        exportButton.setOnAction(e -> onExportRoute());

        saveRouteBtn.setStyle(
                "-fx-background-color: #0284c7;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 7 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        saveRouteBtn.setOnAction(e -> onSaveRoute(SavedRoute.Status.SAVED));

        markSummitedBtn.setStyle(
                "-fx-background-color: #16a34a;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 7 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        markSummitedBtn.setOnAction(e -> onSaveRoute(SavedRoute.Status.COMPLETED));

        Label lblMountain = new Label("Mountain:");
        lblMountain.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + "; -fx-font-weight: bold;");

        Label lblAlgo = new Label("Algorithm:");
        lblAlgo.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + "; -fx-font-weight: bold;");

        Label lblFormat = new Label("Format:");
        lblFormat.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + "; -fx-font-weight: bold;");

        HBox controls = new HBox(10,
                lblMountain, mountainCombo,
                lblAlgo, algorithmCombo,
                findRouteButton,
                lblFormat, exporterCombo,
                exportButton,
                saveRouteBtn,
                markSummitedBtn
        );
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));
        controls.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        // Setup unseeded peak warning banner
        unseededWarningBanner.setAlignment(Pos.CENTER_LEFT);
        unseededWarningBanner.setPadding(new Insets(10, 14, 10, 14));
        unseededWarningBanner.setStyle(
                ThemeManager.isDark()
                        ? "-fx-background-color: #451a03; -fx-border-color: #b45309; -fx-border-radius: 6; -fx-background-radius: 6;"
                        : "-fx-background-color: #fef3c7; -fx-border-color: #f59e0b; -fx-border-radius: 6; -fx-background-radius: 6;"
        );
        Label warnIcon = new Label("⚠️");
        warnIcon.setFont(Font.font("System", 16));
        Label warnMsg = new Label("Route Data Pending: This mountain is catalog-only. GPS waypoints and ascent segments have not been seeded into SQLite yet. Calculations and file exports are disabled.");
        warnMsg.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        warnMsg.setStyle("-fx-text-fill: " + (ThemeManager.isDark() ? "#fde68a" : "#92400e") + ";");
        warnMsg.setWrapText(true);
        HBox.setHgrow(warnMsg, Priority.ALWAYS);
        unseededWarningBanner.getChildren().addAll(warnIcon, warnMsg);
        unseededWarningBanner.setVisible(false);
        unseededWarningBanner.setManaged(false);

        // Mountain Banner
        setupMountainBanner();

        // 3 Alpine Route Comparison Cards
        HBox routeCards = new HBox(16,
                buildRouteCard("Shortest Distance", CostFunction.DISTANCE,
                        ThemeManager.isDark() ? "#38bdf8" : "#0284c7", "Minimizes trail distance"),
                buildRouteCard("Least Elevation Gain", CostFunction.ELEVATION_GAIN,
                        ThemeManager.isDark() ? "#4ade80" : "#16a34a", "Minimizes vertical climbing strain"),
                buildRouteCard("Safest Terrain", CostFunction.SAFETY,
                        ThemeManager.isDark() ? "#fb923c" : "#ea580c", "Prefers highest-rated safety segments")
        );
        routeCards.setAlignment(Pos.CENTER_LEFT);

        // Itinerary Panel
        Label itineraryTitle = new Label("Route Waypoint Itinerary & Milestones");
        itineraryTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        itineraryTitle.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        ScrollPane scrollItinerary = new ScrollPane(itineraryBox);
        scrollItinerary.setFitToWidth(true);
        scrollItinerary.setPrefHeight(130);
        scrollItinerary.setMaxHeight(150);
        scrollItinerary.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 6;"
        );

        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        getChildren().addAll(headerBox, controls, unseededWarningBanner, mountainInfoBanner, routeCards, itineraryTitle, scrollItinerary, statusLabel);

        updateMountainBanner();
        if (initialMountain != null && mountainCombo.getValue() != null) {
            onFindRoute();
        }
    }

    private void setupMountainBanner() {
        mountainInfoBanner.setAlignment(Pos.CENTER_LEFT);
        mountainInfoBanner.setPadding(new Insets(12));
        mountainInfoBanner.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        mountainImageView.setFitWidth(140);
        mountainImageView.setFitHeight(90);
        mountainImageView.setPreserveRatio(true);

        mountainTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        mountainTitleLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        mountainMetaLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        mountainMetaLabel.setStyle("-fx-text-fill: " + ThemeManager.getIceAccent() + ";");

        mountainDescLabel.setFont(Font.font("System", 11));
        mountainDescLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + ";");
        mountainDescLabel.setWrapText(true);

        VBox textInfo = new VBox(4, mountainTitleLabel, mountainMetaLabel, mountainDescLabel);
        HBox.setHgrow(textInfo, Priority.ALWAYS);

        mountainInfoBanner.getChildren().addAll(mountainImageView, textInfo);
    }

    private void updateMountainBanner() {
        Mountain selected = mountainCombo.getValue();
        if (selected == null) {
            mountainInfoBanner.setVisible(false);
            mountainInfoBanner.setManaged(false);
            unseededWarningBanner.setVisible(false);
            unseededWarningBanner.setManaged(false);
            findRouteButton.setDisable(true);
            findRouteButton.setOpacity(0.5);
            exportButton.setDisable(true);
            exportButton.setOpacity(0.5);
            saveRouteBtn.setDisable(true);
            saveRouteBtn.setOpacity(0.5);
            markSummitedBtn.setDisable(true);
            markSummitedBtn.setOpacity(0.5);

            lastResults.clear();
            lastWaypoints = Collections.emptyList();
            for (CostFunction cf : CostFunction.values()) {
                updateCard(cf, null);
            }
            showInitialEmptyState();
            statusLabel.setText("Select a mountain from the dropdown above to begin ascent route planning.");
            statusLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");
            return;
        }

        boolean isSeeded = waypointDao.hasRouteData(selected.getId());

        mountainInfoBanner.setVisible(true);
        mountainInfoBanner.setManaged(true);
        mountainImageView.setImage(MountainImages.load(selected, 140, 90));
        mountainTitleLabel.setText(selected.getName() + (isSeeded ? "  [Routes Available]" : "  [Catalog Only]"));
        mountainMetaLabel.setText(String.format("🏔️ Elevation: %,d m  |  Region: %s  |  Difficulty: %s  |  Season: %s",
                selected.getElevationM(),
                selected.getRegion() != null ? selected.getRegion() : "Global",
                selected.getDifficulty() != null ? selected.getDifficulty().toUpperCase() : "MODERATE",
                selected.getSeason() != null ? selected.getSeason() : "Year-round"));
        mountainDescLabel.setText(selected.getDescription() != null ? selected.getDescription() : "");

        if (!isSeeded) {
            unseededWarningBanner.setVisible(true);
            unseededWarningBanner.setManaged(true);
            findRouteButton.setDisable(true);
            findRouteButton.setOpacity(0.5);
            exportButton.setDisable(true);
            exportButton.setOpacity(0.5);
            saveRouteBtn.setDisable(true);
            saveRouteBtn.setOpacity(0.5);
            markSummitedBtn.setDisable(true);
            markSummitedBtn.setOpacity(0.5);

            lastResults.clear();
            lastWaypoints = Collections.emptyList();
            for (CostFunction cf : CostFunction.values()) {
                updateCard(cf, null);
            }
            showUnseededEmptyState(selected);
            statusLabel.setText("Route data not seeded for " + selected.getName() + " — route finding disabled.");
            statusLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDark() ? "#fcd34d" : "#b45309") + "; -fx-font-weight: bold;");
        } else {
            unseededWarningBanner.setVisible(false);
            unseededWarningBanner.setManaged(false);
            findRouteButton.setDisable(false);
            findRouteButton.setOpacity(1.0);
            exportButton.setDisable(true);
            exportButton.setOpacity(0.5);
            saveRouteBtn.setDisable(true);
            saveRouteBtn.setOpacity(0.5);
            markSummitedBtn.setDisable(true);
            markSummitedBtn.setOpacity(0.5);
            statusLabel.setText("Click 'Find Routes' to calculate optimal ascent paths for " + selected.getName() + ".");
            statusLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");
        }
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

    private void showInitialEmptyState() {
        itineraryBox.getChildren().clear();
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Label icon = new Label("🏔️");
        icon.setFont(Font.font("System", 20));

        Label title = new Label("No Mountain Selected");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Label subtitle = new Label("Choose a mountain peak from the dropdown above to begin ascent planning.");
        subtitle.setFont(Font.font("System", 11));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + "; -fx-text-alignment: center;");

        box.getChildren().addAll(icon, title, subtitle);
        itineraryBox.getChildren().add(box);
    }

    private void onFindRoute() {
        Mountain selected = mountainCombo.getValue();
        if (selected == null) {
            statusLabel.setText("Please select a mountain first.");
            return;
        }

        try {
            waypointDao.validateMountainSeeded(selected);
        } catch (UnseededMountainException e) {
            showUnseededEmptyState(selected);
            AlertHelper.showUnseededMountainWarning(selected);
            return;
        }

        List<Waypoint> waypoints = waypointDao.getWaypointsForMountain(selected.getId());
        List<RouteSegment> segments = new RouteSegmentDao().getSegmentsForMountain(selected.getId());

        if (waypoints.isEmpty() || segments.isEmpty()) {
            lastResults.clear();
            itineraryBox.getChildren().clear();
            for (CostFunction cf : CostFunction.values()) {
                updateCard(cf, null);
            }
            showUnseededEmptyState(selected);
            statusLabel.setText("No route data seeded for " + selected.getName() + " yet.");
            return;
        }

        Integer trailheadId = waypoints.stream().filter(Waypoint::isTrailhead).findFirst().map(Waypoint::getId).orElse(null);
        Integer summitId = waypoints.stream().filter(Waypoint::isSummit).findFirst().map(Waypoint::getId).orElse(null);

        if (trailheadId == null || summitId == null) {
            statusLabel.setText("⚠️ Mountain is missing a trailhead or summit waypoint.");
            AlertHelper.showErrorAlert("Route Error", null,
                    "Trailhead or summit waypoint is missing for " + selected.getName() + ".");
            return;
        }

        Graph graph = new Graph(segments, waypoints);
        PathfindingAlgorithm algorithm = algorithmCombo.getValue() != null
                ? algorithmCombo.getValue()
                : new Dijkstra();

        lastResults.clear();
        lastWaypoints = waypoints;

        boolean anyRouteFound = false;
        for (CostFunction costFunction : CostFunction.values()) {
            try {
                PathResult result = algorithm.findRoute(graph, trailheadId, summitId, costFunction);
                lastResults.put(costFunction, result);
                updateCard(costFunction, result);
                anyRouteFound = true;
            } catch (RouteNotFoundException e) {
                updateCard(costFunction, null);
            }
        }

        if (!anyRouteFound) {
            showNoRouteFoundState(selected);
            statusLabel.setText("⚠️ No valid route could be computed connecting base to summit.");
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            exportButton.setDisable(true);
            exportButton.setOpacity(0.5);
            saveRouteBtn.setDisable(true);
            saveRouteBtn.setOpacity(0.5);
            markSummitedBtn.setDisable(true);
            markSummitedBtn.setOpacity(0.5);
            return;
        }

        exportButton.setDisable(false);
        exportButton.setOpacity(1.0);
        saveRouteBtn.setDisable(false);
        saveRouteBtn.setOpacity(1.0);
        markSummitedBtn.setDisable(false);
        markSummitedBtn.setOpacity(1.0);

        highlightCard(selectedCostFunction);
        displayItinerary(selectedCostFunction);
        statusLabel.setText("Showing optimal routes for " + selected.getName() + " using " + algorithm.getName() + ".");
        statusLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");
    }

    private void showUnseededEmptyState(Mountain mountain) {
        itineraryBox.getChildren().clear();
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Label icon = new Label("⚠️");
        icon.setFont(Font.font("System", 20));

        Label title = new Label("Route Unavailable");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: " + (ThemeManager.isDark() ? "#fcd34d" : "#b45309") + ";");

        Label subtitle = new Label("Route for " + (mountain != null ? mountain.getName() : "this mountain") + " is currently not available.");
        subtitle.setFont(Font.font("System", 11));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + "; -fx-text-alignment: center;");

        box.getChildren().addAll(icon, title, subtitle);
        itineraryBox.getChildren().add(box);
    }

    private void showNoRouteFoundState(Mountain mountain) {
        itineraryBox.getChildren().clear();
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Label icon = new Label("❌");
        icon.setFont(Font.font("System", 20));

        Label title = new Label("No Route Found");
        title.setFont(Font.font("System", FontWeight.BOLD, 13));
        title.setStyle("-fx-text-fill: #ef4444;");

        Label subtitle = new Label("No route found between trailhead and summit for " +
                (mountain != null ? mountain.getName() : "this mountain") + ".");
        subtitle.setFont(Font.font("System", 11));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + "; -fx-text-alignment: center;");

        box.getChildren().addAll(icon, title, subtitle);
        itineraryBox.getChildren().add(box);
    }

    private void updateCard(CostFunction costFunction, PathResult result) {
        Label distanceLabel = distanceLabels.get(costFunction);
        Label elevationLabel = elevationLabels.get(costFunction);
        Label safetyLabel = safetyLabels.get(costFunction);

        if (result == null) {
            distanceLabel.setText("Distance: --");
            elevationLabel.setText("Elevation gain: --");
            safetyLabel.setText("Safety score: --");
            return;
        }

        double totalDistance = result.getSegments().stream().mapToDouble(RouteSegment::getDistanceKm).sum();
        int totalElevation = result.getSegments().stream().mapToInt(RouteSegment::getElevationGainM).sum();
        int totalSafety = result.getSegments().stream().mapToInt(RouteSegment::getSafetyScore).sum();

        distanceLabel.setText(String.format("Distance: %.1f km", totalDistance));
        elevationLabel.setText(String.format("Elevation gain: %,d m", totalElevation));
        safetyLabel.setText(String.format("Safety score: %d (cumulative)", totalSafety));
    }

    private void displayItinerary(CostFunction costFunction) {
        itineraryBox.getChildren().clear();
        PathResult result = lastResults.get(costFunction);

        if (result == null || result.getSegments().isEmpty() || lastWaypoints.isEmpty()) {
            Label noRoute = new Label("No route available for the selected criteria.");
            noRoute.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + "; -fx-padding: 8;");
            itineraryBox.getChildren().add(noRoute);
            return;
        }

        Map<Integer, Waypoint> waypointMap = new HashMap<>();
        for (Waypoint wp : lastWaypoints) {
            waypointMap.put(wp.getId(), wp);
        }

        double runningDist = 0.0;
        int step = 1;
        for (RouteSegment segment : result.getSegments()) {
            runningDist += segment.getDistanceKm();
            Waypoint from = waypointMap.get(segment.getFromWaypointId());
            Waypoint to = waypointMap.get(segment.getToWaypointId());

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 8, 4, 8));
            row.setStyle("-fx-border-color: " + (ThemeManager.isDark() ? "#1e293b" : "#f1f5f9") + "; -fx-border-width: 0 0 1 0;");

            Label stepLbl = new Label("Leg " + step++);
            stepLbl.setFont(Font.font("System", FontWeight.BOLD, 11));
            stepLbl.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + "; -fx-min-width: 45px;");

            String fromStr = from != null ? from.getName() + " (" + from.getElevationM() + "m)" : "Waypoint #" + segment.getFromWaypointId();
            String toStr = to != null ? to.getName() + " (" + to.getElevationM() + "m)" : "Waypoint #" + segment.getToWaypointId();

            Label pathLbl = new Label(fromStr + "  ➔  " + toStr);
            pathLbl.setFont(Font.font("System", FontWeight.MEDIUM, 12));
            pathLbl.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");
            HBox.setHgrow(pathLbl, Priority.ALWAYS);

            Label statsLbl = new Label(String.format("%.1f km  ·  +%,d m  ·  Safety: %d",
                    segment.getDistanceKm(), segment.getElevationGainM(), segment.getSafetyScore()));
            statsLbl.setFont(Font.font("System", 11));
            statsLbl.setStyle("-fx-text-fill: " + ThemeManager.getIceAccent() + ";");

            row.getChildren().addAll(stepLbl, pathLbl, statsLbl);
            itineraryBox.getChildren().add(row);
        }
    }

    private void highlightCard(CostFunction costFunction) {
        selectedCostFunction = costFunction;
        String accent = ThemeManager.getIceAccent();
        for (Map.Entry<CostFunction, VBox> entry : cardBoxes.entrySet()) {
            VBox box = entry.getValue();
            if (entry.getKey() == costFunction) {
                box.setStyle(
                        "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                        "-fx-border-color: " + accent + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        (ThemeManager.isDark()
                                ? "-fx-effect: dropshadow(three-pass-box, rgba(56,189,248,0.25), 10, 0, 0, 3);"
                                : "-fx-effect: dropshadow(three-pass-box, rgba(2,132,199,0.18), 10, 0, 0, 3);")
                );
            } else {
                box.setStyle(
                        "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                        "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 6, 0, 0, 2);"
                );
            }
        }
    }

    private VBox buildRouteCard(String title, CostFunction costFunction, String accentColor, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("System", 10));
        subtitleLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        Label badge = new Label(costFunction.name());
        badge.setFont(Font.font("System", FontWeight.BOLD, 9));
        badge.setStyle("-fx-background-color: " + accentColor + "; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 4;");

        HBox topRow = new HBox(8, titleLabel, badge);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label distance = new Label("Distance: --");
        Label elevation = new Label("Elevation gain: --");
        Label safety = new Label("Safety score: --");

        distance.setFont(Font.font("System", 12));
        elevation.setFont(Font.font("System", 12));
        safety.setFont(Font.font("System", 12));

        distance.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + ";");
        elevation.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + ";");
        safety.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + ";");

        distanceLabels.put(costFunction, distance);
        elevationLabels.put(costFunction, elevation);
        safetyLabels.put(costFunction, safety);

        VBox card = new VBox(8, topRow, subtitleLabel, distance, elevation, safety);
        card.setPadding(new Insets(14));
        card.setPrefWidth(260);
        card.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );
        card.setOnMouseClicked(e -> {
            highlightCard(costFunction);
            displayItinerary(costFunction);
        });

        cardBoxes.put(costFunction, card);
        return card;
    }

    private void onExportRoute() {
        Mountain selected = mountainCombo.getValue();
        if (selected == null) {
            statusLabel.setText("Please select a mountain first.");
            return;
        }

        if (!waypointDao.hasRouteData(selected.getId())) {
            AlertHelper.showUnseededMountainWarning(selected);
            return;
        }

        if (lastResults.isEmpty() || lastWaypoints.isEmpty()) {
            statusLabel.setText("Please find a route first before exporting.");
            AlertHelper.showWarningAlert("Export Unavailable", null,
                    "Please compute a route for " + selected.getName() + " first.");
            return;
        }

        RouteExporter exporter = exporterCombo.getValue();
        if (exporter == null) {
            statusLabel.setText("Please select an export format.");
            return;
        }

        PathResult routeToExport = lastResults.get(selectedCostFunction);
        if (routeToExport == null) {
            routeToExport = lastResults.values().stream().findFirst().orElse(null);
        }

        if (routeToExport == null) {
            statusLabel.setText("No valid computed route available to export.");
            AlertHelper.showWarningAlert("Export Unavailable", null, "No valid route could be exported.");
            return;
        }

        try {
            String safeMountainName = selected.getName().replaceAll("[^a-zA-Z0-9_-]", "_");
            Path exportDir = Path.of("exports");
            Path targetFile = exportDir.resolve(safeMountainName + "_" + selectedCostFunction.name() + "_route" + exporter.getFileExtension());

            exporter.export(routeToExport, lastWaypoints, targetFile);
            statusLabel.setText("✓ Exported " + exporter.getFormatName() + " to: " + targetFile);
            statusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
            AlertHelper.showInfoAlert("Export Succeeded", null,
                    "Saved " + exporter.getFormatName() + " to " + targetFile.getFileName() + ".");
        } catch (ExportException e) {
            statusLabel.setText("❌ Export failed: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            AlertHelper.showErrorAlert("Export Failed", null, "Export encountered an error: " + e.getMessage());
        }
    }

    private void onSaveRoute(SavedRoute.Status status) {
        Mountain selected = mountainCombo.getValue();
        if (selected == null) {
            statusLabel.setText("Please select a mountain first.");
            return;
        }

        if (!waypointDao.hasRouteData(selected.getId())) {
            AlertHelper.showUnseededMountainWarning(selected);
            return;
        }

        PathResult route = lastResults.get(selectedCostFunction);
        if (route == null && !lastResults.isEmpty()) {
            route = lastResults.values().stream().findFirst().orElse(null);
        }

        if (route == null) {
            statusLabel.setText("Please calculate a route before saving to your logbook.");
            AlertHelper.showWarningAlert("No Route Found", null,
                    "Please compute an optimal route before saving to your logbook.");
            return;
        }

        try {
            int userId = UserSession.getActiveUser().getId();
            String defaultNotes = status == SavedRoute.Status.COMPLETED
                    ? "Summited via " + selectedCostFunction.name() + " route."
                    : "Planned ascent via " + selectedCostFunction.name() + " route.";

            savedRouteDao.saveRoute(userId, selected.getId(), selectedCostFunction, status, false, defaultNotes);

            if (status == SavedRoute.Status.COMPLETED) {
                statusLabel.setText("🏔️ Summit recorded! " + selected.getName() + " marked as completed in your Logbook.");
                statusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                AlertHelper.showInfoAlert("Summit Recorded", null,
                        selected.getName() + " marked as completed in your logbook.");
            } else {
                statusLabel.setText("⭐ Saved " + selected.getName() + " (" + selectedCostFunction.name() + ") to your Logbook wishlist!");
                statusLabel.setStyle("-fx-text-fill: #0284c7; -fx-font-weight: bold;");
                AlertHelper.showInfoAlert("Route Saved", null,
                        selected.getName() + " added to your logbook wishlist.");
            }
        } catch (Exception e) {
            statusLabel.setText("Failed to save route: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            AlertHelper.showErrorAlert("Logbook Error", null, "Failed to save route: " + e.getMessage());
        }
    }
}
