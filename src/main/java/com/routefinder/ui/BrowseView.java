package com.routefinder.ui;

import com.routefinder.db.MountainDao;
import com.routefinder.db.WaypointDao;
import com.routefinder.model.Mountain;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Mountain Catalog & Explorer Screen (Default Entry Screen).
 *
 * <p>Styled with an Alpine-inspired theme (crystalline snow-white surfaces in Light mode,
 * deep midnight blues in Dark mode). Displays a prominent Featured Mountain of the Day
 * hero section with landscape photography and rich description, followed by a responsive
 * catalog card grid.</p>
 */
public class BrowseView extends VBox {

    /**
     * Supported sorting criteria for the mountain catalog.
     */
    public enum SortOption {
        DIFFICULTY_EASY_TO_EXTREME("⚡ Difficulty (Easy → Extreme)"),
        DIFFICULTY_EXTREME_TO_EASY("🔥 Difficulty (Extreme → Easy)"),
        ELEVATION_HIGH_TO_LOW("🏔️ Elevation (High → Low)"),
        ELEVATION_LOW_TO_HIGH("📉 Elevation (Low → High)"),
        NAME_A_TO_Z("🔤 Name (A → Z)"),
        NAME_Z_TO_A("🔡 Name (Z → A)"),
        SEEDED_FIRST("✓ Routes Available First");

        private final String label;

        SortOption(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final Consumer<Mountain> onSelectMountain;
    private final Set<Integer> seededMountainIds;
    private final List<Mountain> allMountains;

    private final VBox featuredHeroContainer = new VBox();
    private Mountain currentFeaturedMountain;

    private final FlowPane cardGrid = new FlowPane();
    private final ComboBox<SortOption> sortCombo = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final Label peakCountLabel = new Label();

    public BrowseView() {
        this(null);
    }

    public BrowseView(Consumer<Mountain> onSelectMountain) {
        this.onSelectMountain = onSelectMountain;
        this.seededMountainIds = new WaypointDao().getSeededMountainIds();
        this.allMountains = new MountainDao().getAllMountains();

        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + ThemeManager.getBgPrimary() + ";");

        // Header Title
        Label title = new Label("Alpine Mountain Catalog");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Label subtitle = new Label("Explore peaks, elevations, and trails across world-renowned mountain ranges.");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        VBox header = new VBox(4, title, subtitle);

        // Search & Sorting Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 14, 10, 14));
        toolbar.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        searchField.setPromptText("🔍  Search mountains, regions, difficulty...");
        searchField.setPrefWidth(280);
        searchField.setStyle(
                "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#f8fafc") + ";" +
                "-fx-text-fill: " + ThemeManager.getTextTitle() + ";" +
                "-fx-prompt-text-fill: " + ThemeManager.getTextMuted() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 7 10;"
        );
        searchField.textProperty().addListener((obs, oldV, newV) -> refreshGrid());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sortLabel = new Label("Sort By:");
        sortLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        sortLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + ";");

        sortCombo.getItems().setAll(SortOption.values());
        // Default sort: DIFFICULTY (as requested by user)
        sortCombo.getSelectionModel().select(SortOption.DIFFICULTY_EASY_TO_EXTREME);
        sortCombo.setStyle(
                "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#ffffff") + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;"
        );
        sortCombo.setOnAction(e -> refreshGrid());

        toolbar.getChildren().addAll(searchField, spacer, sortLabel, sortCombo);

        // Pick a random initial mountain (prefer seeded peaks so Plan Route works right away)
        if (!allMountains.isEmpty()) {
            List<Mountain> seededList = allMountains.stream()
                    .filter(m -> seededMountainIds.contains(m.getId()))
                    .toList();
            List<Mountain> pool = seededList.isEmpty() ? allMountains : seededList;
            int randomIndex = (int) (Math.random() * pool.size());
            currentFeaturedMountain = pool.get(randomIndex);
        }
        renderFeaturedHero();

        // Mountain Cards Grid
        cardGrid.setHgap(20);
        cardGrid.setVgap(20);
        cardGrid.setPadding(new Insets(8, 0, 16, 0));

        // Status summary row (e.g. "Showing 11 peaks · Sorted by Difficulty")
        peakCountLabel.setFont(Font.font("System", FontWeight.MEDIUM, 11));
        peakCountLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        // Initial card render based on default difficulty sort
        refreshGrid();

        VBox scrollContent = new VBox(16, featuredHeroContainer, peakCountLabel, cardGrid);
        scrollContent.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, toolbar, scrollPane);
    }

    /**
     * Cycles to the next mountain in the catalog to showcase on the hero card.
     */
    private void cycleFeaturedMountain() {
        if (allMountains.isEmpty()) return;
        int currentIndex = allMountains.indexOf(currentFeaturedMountain);
        int nextIndex = (currentIndex + 1) % allMountains.size();
        currentFeaturedMountain = allMountains.get(nextIndex);
        renderFeaturedHero();
    }

    private void renderFeaturedHero() {
        featuredHeroContainer.getChildren().clear();
        if (currentFeaturedMountain != null) {
            featuredHeroContainer.getChildren().add(buildFeaturedHeroCard(currentFeaturedMountain));
        }
    }

    /**
     * Re-filters and sorts mountain cards in real time based on active search text and sort criterion.
     */
    private void refreshGrid() {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        SortOption sortOption = sortCombo.getValue() != null ? sortCombo.getValue() : SortOption.DIFFICULTY_EASY_TO_EXTREME;

        // Hide featured hero banner when an active search query is filtering cards
        if (query.isEmpty()) {
            featuredHeroContainer.setVisible(true);
            featuredHeroContainer.setManaged(true);
        } else {
            featuredHeroContainer.setVisible(false);
            featuredHeroContainer.setManaged(false);
        }

        List<Mountain> filtered = allMountains.stream()
                .filter(m -> {
                    if (query.isEmpty()) return true;
                    String name = m.getName() != null ? m.getName().toLowerCase() : "";
                    String region = m.getRegion() != null ? m.getRegion().toLowerCase() : "";
                    String diff = m.getDifficulty() != null ? m.getDifficulty().toLowerCase() : "";
                    return name.contains(query) || region.contains(query) || diff.contains(query);
                })
                .sorted(getComparator(sortOption, seededMountainIds))
                .toList();

        cardGrid.getChildren().clear();

        if (filtered.isEmpty()) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30, 20, 30, 20));
            emptyBox.setPrefWidth(800);

            Label emptyIcon = new Label("🔍");
            emptyIcon.setFont(Font.font("System", 24));

            Label emptyTitle = new Label("No Mountains Found");
            emptyTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            emptyTitle.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

            Label emptySub = new Label("No peaks match '" + query + "'. Try another keyword or clear the search.");
            emptySub.setFont(Font.font("System", 11));
            emptySub.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

            Button clearBtn = new Button("Clear Search");
            clearBtn.setStyle(
                    "-fx-background-color: #0284c7;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 6 14;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;"
            );
            clearBtn.setOnAction(e -> searchField.clear());

            emptyBox.getChildren().addAll(emptyIcon, emptyTitle, emptySub, clearBtn);
            cardGrid.getChildren().add(emptyBox);
            peakCountLabel.setText("No peaks matching '" + query + "'");
        } else {
            for (Mountain mountain : filtered) {
                cardGrid.getChildren().add(buildMountainCard(mountain));
            }
            if (query.isEmpty()) {
                peakCountLabel.setText(String.format("Showing %d peaks  ·  Sorted by %s",
                        filtered.size(), sortOption.getLabel()));
            } else {
                peakCountLabel.setText(String.format("Found %d matching peaks  ·  Sorted by %s",
                        filtered.size(), sortOption.getLabel()));
            }
        }
    }

    /**
     * Maps textual difficulty to an ordinal rank for consistent sorting.
     */
    public static int difficultyRank(String difficulty) {
        if (difficulty == null) return 5;
        return switch (difficulty.trim().toLowerCase()) {
            case "easy" -> 1;
            case "moderate" -> 2;
            case "hard" -> 3;
            case "extreme" -> 4;
            default -> 5;
        };
    }

    /**
     * Builds a Mountain comparator for the requested sort option.
     */
    public static Comparator<Mountain> getComparator(SortOption option, Set<Integer> seededIds) {
        return switch (option) {
            case DIFFICULTY_EASY_TO_EXTREME -> Comparator
                    .comparingInt((Mountain m) -> difficultyRank(m.getDifficulty()))
                    .thenComparing(Comparator.comparingInt(Mountain::getElevationM).reversed())
                    .thenComparing(Mountain::getName);

            case DIFFICULTY_EXTREME_TO_EASY -> Comparator
                    .comparingInt((Mountain m) -> difficultyRank(m.getDifficulty())).reversed()
                    .thenComparing(Comparator.comparingInt(Mountain::getElevationM).reversed())
                    .thenComparing(Mountain::getName);

            case ELEVATION_HIGH_TO_LOW -> Comparator
                    .comparingInt(Mountain::getElevationM).reversed()
                    .thenComparing(Mountain::getName);

            case ELEVATION_LOW_TO_HIGH -> Comparator
                    .comparingInt(Mountain::getElevationM)
                    .thenComparing(Mountain::getName);

            case NAME_A_TO_Z -> Comparator
                    .comparing(Mountain::getName, String.CASE_INSENSITIVE_ORDER);

            case NAME_Z_TO_A -> Comparator
                    .comparing(Mountain::getName, String.CASE_INSENSITIVE_ORDER).reversed();

            case SEEDED_FIRST -> Comparator
                    .comparing((Mountain m) -> (seededIds != null && seededIds.contains(m.getId())) ? 0 : 1)
                    .thenComparingInt((Mountain m) -> difficultyRank(m.getDifficulty()))
                    .thenComparing(Mountain::getName);
        };
    }

    /**
     * Builds the prominent Featured Mountain Hero Card taking up a full row.
     * Contains widescreen landscape photography and the mountain's rich narrative description.
     */
    private VBox buildFeaturedHeroCard(Mountain mountain) {
        boolean isSeeded = seededMountainIds.contains(mountain.getId());

        VBox heroWrapper = new VBox(8);
        heroWrapper.setMaxWidth(Double.MAX_VALUE);

        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + (isSeeded ? ThemeManager.getIceAccent() : (ThemeManager.isDark() ? "#334155" : "#cbd5e1")) + ";" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                (ThemeManager.isDark()
                        ? "-fx-effect: dropshadow(three-pass-box, rgba(2,132,199,0.22), 16, 0, 0, 4);"
                        : "-fx-effect: dropshadow(three-pass-box, rgba(2,132,199,0.12), 16, 0, 0, 4);")
        );

        // Wide Landscape Photo (340 x 185)
        ImageView photoView = MountainImages.viewLandscape(mountain, 340, 185);

        // Right side: Mountain details, badges, full description, actions
        VBox contentCol = new VBox(8);
        HBox.setHgrow(contentCol, Priority.ALWAYS);

        // Header Row: Featured Badge + Cycle Button
        Label featuredBadge = new Label("⭐  FEATURED MOUNTAIN OF THE DAY");
        featuredBadge.setFont(Font.font("System", FontWeight.BOLD, 10));
        featuredBadge.setPadding(new Insets(3, 8, 3, 8));
        featuredBadge.setStyle(
                ThemeManager.isDark()
                        ? "-fx-background-color: #0c4a6e; -fx-text-fill: #38bdf8; -fx-background-radius: 6;"
                        : "-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-background-radius: 6;"
        );

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button cycleBtn = new Button("🎲  Cycle Peak");
        cycleBtn.setFont(Font.font("System", FontWeight.BOLD, 10));
        cycleBtn.setStyle(
                "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#f1f5f9") + ";" +
                "-fx-text-fill: " + ThemeManager.getTextBody() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 4 10;" +
                "-fx-cursor: hand;"
        );
        cycleBtn.setOnAction(e -> cycleFeaturedMountain());

        HBox topRow = new HBox(8, featuredBadge, topSpacer, cycleBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Mountain Name
        Label nameLabel = new Label(mountain.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        nameLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        // Badges Row
        Label metaLabel = new Label(String.format("🏔️ %,d m · %s",
                mountain.getElevationM(),
                mountain.getRegion() != null ? mountain.getRegion() : "Global"));
        metaLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        metaLabel.setStyle("-fx-text-fill: " + ThemeManager.getIceAccent() + ";");

        Label diffBadge = createDifficultyBadge(mountain.getDifficulty());
        Label routeStatusBadge = createRouteStatusBadge(isSeeded);
        Label seasonLabel = new Label(mountain.getSeason() != null ? "☀️ " + mountain.getSeason() : "☀️ All Year");
        seasonLabel.setFont(Font.font("System", 11));
        seasonLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        HBox badgeRow = new HBox(8, metaLabel, diffBadge, routeStatusBadge, seasonLabel);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        // Rich Description (ONLY ON THIS HERO CARD)
        Label descLabel = new Label(mountain.getDescription() != null ? mountain.getDescription() : "");
        descLabel.setFont(Font.font("System", 12));
        descLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextBody() + "; -fx-line-spacing: 2px;");
        descLabel.setWrapText(true);
        VBox.setVgrow(descLabel, Priority.ALWAYS);

        // Action Row
        Button planBtn = new Button(isSeeded ? "🧭  Plan Route for " + mountain.getName() + " →" : "⚠️  Route Pending for " + mountain.getName());
        planBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        if (isSeeded) {
            planBtn.setStyle(
                    "-fx-background-color: #0284c7;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 8 16;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;"
            );
            planBtn.setOnAction(e -> {
                if (onSelectMountain != null) {
                    onSelectMountain.accept(mountain);
                }
            });
        } else {
            planBtn.setStyle(
                    "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#f1f5f9") + ";" +
                    "-fx-text-fill: " + (ThemeManager.isDark() ? "#94a3b8" : "#64748b") + ";" +
                    "-fx-border-color: " + (ThemeManager.isDark() ? "#334155" : "#cbd5e1") + ";" +
                    "-fx-border-radius: 6;" +
                    "-fx-background-radius: 6;" +
                    "-fx-padding: 8 16;" +
                    "-fx-cursor: hand;"
            );
            planBtn.setOnAction(e -> AlertHelper.showUnseededMountainWarning(mountain));
        }

        HBox actionRow = new HBox(planBtn);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        contentCol.getChildren().addAll(topRow, nameLabel, badgeRow, descLabel, actionRow);

        card.getChildren().addAll(photoView, contentCol);
        heroWrapper.getChildren().add(card);
        return heroWrapper;
    }

    /**
     * Builds a compact, uniform mountain card for the responsive catalog grid.
     * Note: Full narrative description is reserved for the Featured Hero Card above.
     */
    private VBox buildMountainCard(Mountain mountain) {
        boolean isSeeded = seededMountainIds.contains(mountain.getId());

        VBox card = new VBox(8);
        card.setPrefWidth(260);
        card.setMaxWidth(260);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + (isSeeded ? ThemeManager.getBorderSubtle() : (ThemeManager.isDark() ? "#334155" : "#e2e8f0")) + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                (ThemeManager.isDark()
                        ? "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 2);"
                        : "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.06), 8, 0, 0, 2);")
        );

        // Thumbnail Photo
        ImageView photoView = MountainImages.view(mountain, 236, 135);

        // Mountain Name
        Label nameLabel = new Label(mountain.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        // Region & Elevation
        Label metaLabel = new Label(String.format("🏔️ %,d m · %s", mountain.getElevationM(),
                mountain.getRegion() != null ? mountain.getRegion() : "Global"));
        metaLabel.setFont(Font.font("System", FontWeight.MEDIUM, 11));
        metaLabel.setStyle("-fx-text-fill: " + ThemeManager.getIceAccent() + ";");

        // Badges Row
        Label diffBadge = createDifficultyBadge(mountain.getDifficulty());
        Label routeStatusBadge = createRouteStatusBadge(isSeeded);

        Label seasonLabel = new Label(mountain.getSeason() != null ? "☀️ " + mountain.getSeason() : "☀️ All Year");
        seasonLabel.setFont(Font.font("System", 10));
        seasonLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        HBox badgeRow = new HBox(6, diffBadge, routeStatusBadge, seasonLabel);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        // Action Button: Active blue for seeded peaks; muted warning state for unseeded
        Button planBtn = new Button(isSeeded ? "Plan Route →" : "Route Pending ⚠️");
        planBtn.setMaxWidth(Double.MAX_VALUE);

        if (isSeeded) {
            planBtn.setStyle(
                    "-fx-background-color: #0284c7;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 11px;" +
                    "-fx-padding: 7 12;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;"
            );
            planBtn.setOnAction(e -> {
                if (onSelectMountain != null) {
                    onSelectMountain.accept(mountain);
                }
            });
        } else {
            planBtn.setStyle(
                    "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#f1f5f9") + ";" +
                    "-fx-text-fill: " + (ThemeManager.isDark() ? "#94a3b8" : "#64748b") + ";" +
                    "-fx-border-color: " + (ThemeManager.isDark() ? "#334155" : "#cbd5e1") + ";" +
                    "-fx-border-radius: 6;" +
                    "-fx-background-radius: 6;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 11px;" +
                    "-fx-padding: 7 12;" +
                    "-fx-cursor: hand;"
            );
            planBtn.setTooltip(new Tooltip("Route data not yet seeded for " + mountain.getName() + ". Click for details."));
            planBtn.setOnAction(e -> AlertHelper.showUnseededMountainWarning(mountain));
        }

        // Card click behavior: navigate if seeded, prompt error state if unseeded
        card.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button) {
                return;
            }
            if (isSeeded) {
                if (onSelectMountain != null) {
                    onSelectMountain.accept(mountain);
                }
            } else {
                AlertHelper.showUnseededMountainWarning(mountain);
            }
        });
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");

        if (photoView != null) {
            card.getChildren().add(photoView);
        }
        card.getChildren().addAll(nameLabel, metaLabel, badgeRow, planBtn);

        return card;
    }

    private Label createRouteStatusBadge(boolean isSeeded) {
        Label badge = new Label(isSeeded ? "✓ SEEDED" : "⚠️ CATALOG");
        badge.setFont(Font.font("System", FontWeight.BOLD, 9));
        badge.setPadding(new Insets(2, 5, 2, 5));

        if (isSeeded) {
            badge.setStyle(ThemeManager.isDark()
                    ? "-fx-background-color: #064e3b; -fx-text-fill: #6ee7b7; -fx-background-radius: 4;"
                    : "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 4;");
        } else {
            badge.setStyle(ThemeManager.isDark()
                    ? "-fx-background-color: #451a03; -fx-text-fill: #fcd34d; -fx-background-radius: 4;"
                    : "-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-background-radius: 4;");
        }
        return badge;
    }

    private Label createDifficultyBadge(String difficulty) {
        String text = difficulty != null ? difficulty.toUpperCase() : "MODERATE";
        Label badge = new Label(text);
        badge.setFont(Font.font("System", FontWeight.BOLD, 10));
        badge.setPadding(new Insets(2, 6, 2, 6));

        String style = switch (text.toLowerCase()) {
            case "easy" -> ThemeManager.isDark()
                    ? "-fx-background-color: #064e3b; -fx-text-fill: #6ee7b7; -fx-background-radius: 4;"
                    : "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 4;";
            case "hard" -> ThemeManager.isDark()
                    ? "-fx-background-color: #7c2d12; -fx-text-fill: #fdba74; -fx-background-radius: 4;"
                    : "-fx-background-color: #ffedd5; -fx-text-fill: #c2410c; -fx-background-radius: 4;";
            case "extreme" -> ThemeManager.isDark()
                    ? "-fx-background-color: #7f1d1d; -fx-text-fill: #fca5a5; -fx-background-radius: 4;"
                    : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-background-radius: 4;";
            default -> ThemeManager.isDark()
                    ? "-fx-background-color: #0c4a6e; -fx-text-fill: #7dd3fc; -fx-background-radius: 4;"
                    : "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-background-radius: 4;";
        };
        badge.setStyle(style);
        return badge;
    }
}
