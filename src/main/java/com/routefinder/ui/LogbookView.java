package com.routefinder.ui;

import com.routefinder.db.MountainDao;
import com.routefinder.db.SavedRouteDao;
import com.routefinder.db.UserDao;
import com.routefinder.model.Mountain;
import com.routefinder.model.SavedRoute;
import com.routefinder.model.User;
import com.routefinder.model.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Consumer;

/**
 * Climber Logbook & Expedition Tracker Screen.
 *
 * <p>Allows climbers to record conquered peaks, track total vertical gain,
 * bookmark future ascent wishlists, and write expedition notes.</p>
 */
public class LogbookView extends VBox {

    private enum FilterMode {
        ALL("All Entries"),
        COMPLETED("🏔️ Completed Summits"),
        SAVED("📌 Planned Wishlist"),
        LIKED("❤️ Favorites");

        private final String label;

        FilterMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final Consumer<Mountain> onNavigateToRouteFinder;
    private final SavedRouteDao savedRouteDao = new SavedRouteDao();
    private final MountainDao mountainDao = new MountainDao();
    private final UserDao userDao = new UserDao();

    private final VBox routeListContainer = new VBox(12);
    private final HBox statsRow = new HBox(16);
    private final Label userBadgeLabel = new Label();
    private FilterMode currentFilter = FilterMode.ALL;

    public LogbookView() {
        this(null);
    }

    public LogbookView(Consumer<Mountain> onNavigateToRouteFinder) {
        this.onNavigateToRouteFinder = onNavigateToRouteFinder;

        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: " + ThemeManager.getBgPrimary() + ";");

        // Header Title
        Label title = new Label("Climber Logbook & Expeditions");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Label subtitle = new Label("Track completed summits, record vertical elevation milestones, and plan future alpine expeditions.");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        VBox header = new VBox(4, title, subtitle);

        // Stats Dashboard
        buildStatsDashboard();

        // Filter & Profile Switcher Toolbar
        HBox toolbar = buildToolbar();

        // Scrollable Logged Routes List
        ScrollPane scrollPane = new ScrollPane(routeListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, statsRow, toolbar, scrollPane);

        // Initial render
        refreshLogbook();
    }

    private void buildStatsDashboard() {
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().clear();

        User user = UserSession.getActiveUser();
        List<SavedRoute> allRoutes = savedRouteDao.getRoutesForUser(user.getId());

        long completedCount = allRoutes.stream().filter(SavedRoute::isCompleted).count();
        long savedCount = allRoutes.stream().filter(SavedRoute::isSaved).count();
        long likedCount = allRoutes.stream().filter(SavedRoute::isLiked).count();
        int totalElevationM = allRoutes.stream()
                .filter(SavedRoute::isCompleted)
                .mapToInt(SavedRoute::getMountainElevationM)
                .sum();

        statsRow.getChildren().addAll(
                createStatCard("🏔️ Completed", completedCount + " Summits", ThemeManager.isDark() ? "#38bdf8" : "#0284c7"),
                createStatCard("🧗 Conquered Elevation", String.format("%,d m", totalElevationM), ThemeManager.isDark() ? "#4ade80" : "#16a34a"),
                createStatCard("📌 Planned Wishlist", savedCount + " Expeditions", ThemeManager.isDark() ? "#fb923c" : "#ea580c"),
                createStatCard("❤️ Favorites", likedCount + " Saved", ThemeManager.isDark() ? "#f472b6" : "#db2777")
        );
    }

    private VBox createStatCard(String title, String value, String accentColor) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setPrefWidth(210);
        card.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                (ThemeManager.isDark()
                        ? "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 6, 0, 0, 2);"
                        : "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.05), 6, 0, 0, 2);")
        );

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("System", FontWeight.MEDIUM, 11));
        lblTitle.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        Label lblValue = new Label(value);
        lblValue.setFont(Font.font("System", FontWeight.BOLD, 17));
        lblValue.setStyle("-fx-text-fill: " + accentColor + ";");

        card.getChildren().addAll(lblTitle, lblValue);
        return card;
    }

    private HBox buildToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 14, 10, 14));
        toolbar.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        // Filter Buttons
        for (FilterMode mode : FilterMode.values()) {
            Button btn = new Button(mode.getLabel());
            btn.setFont(Font.font("System", FontWeight.MEDIUM, 11));
            btn.setStyle(mode == currentFilter ? getActiveFilterStyle() : getInactiveFilterStyle());
            btn.setOnAction(e -> {
                currentFilter = mode;
                refreshLogbook();
                // Update button styles
                for (var node : toolbar.getChildren()) {
                    if (node instanceof Button b && b.getUserData() instanceof FilterMode m) {
                        b.setStyle(m == currentFilter ? getActiveFilterStyle() : getInactiveFilterStyle());
                    }
                }
            });
            btn.setUserData(mode);
            toolbar.getChildren().add(btn);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Active Profile Pill
        userBadgeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        userBadgeLabel.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");
        updateUserBadge();

        Button switchProfileBtn = new Button("Switch Climber");
        switchProfileBtn.setFont(Font.font("System", FontWeight.BOLD, 11));
        switchProfileBtn.setStyle(
                "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#f1f5f9") + ";" +
                "-fx-text-fill: " + ThemeManager.getIceAccent() + ";" +
                "-fx-border-color: " + ThemeManager.getBorderSubtle() + ";" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 5 10;" +
                "-fx-cursor: hand;"
        );
        switchProfileBtn.setOnAction(e -> promptSwitchProfile());

        toolbar.getChildren().addAll(spacer, userBadgeLabel, switchProfileBtn);
        return toolbar;
    }

    private void updateUserBadge() {
        userBadgeLabel.setText("👤 Climber: " + UserSession.getActiveUser().getDisplayName());
    }

    private void refreshLogbook() {
        buildStatsDashboard();
        updateUserBadge();

        routeListContainer.getChildren().clear();
        User activeUser = UserSession.getActiveUser();

        List<SavedRoute> routes = switch (currentFilter) {
            case ALL -> savedRouteDao.getRoutesForUser(activeUser.getId());
            case COMPLETED -> savedRouteDao.getRoutesByStatus(activeUser.getId(), SavedRoute.Status.COMPLETED);
            case SAVED -> savedRouteDao.getRoutesByStatus(activeUser.getId(), SavedRoute.Status.SAVED);
            case LIKED -> savedRouteDao.getLikedRoutes(activeUser.getId());
        };

        if (routes.isEmpty()) {
            routeListContainer.getChildren().add(buildEmptyState());
            return;
        }

        for (SavedRoute route : routes) {
            routeListContainer.getChildren().add(buildRouteCard(route));
        }
    }

    private VBox buildRouteCard(SavedRoute route) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-color: " + ThemeManager.getBgSurface() + ";" +
                "-fx-border-color: " + (route.isCompleted()
                        ? (ThemeManager.isDark() ? "#064e3b" : "#bbf7d0")
                        : ThemeManager.getBorderSubtle()) + ";" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        // Header Row: Mountain name + elevation + status badge
        Label nameLbl = new Label("🏔️ " + route.getMountainName());
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLbl.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Label eleLbl = new Label(String.format("%,d m", route.getMountainElevationM()));
        eleLbl.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        eleLbl.setStyle("-fx-text-fill: " + ThemeManager.getIceAccent() + ";");

        // Objective badge
        Label objectiveBadge = new Label(route.getCostFunction().name());
        objectiveBadge.setFont(Font.font("System", FontWeight.BOLD, 9));
        objectiveBadge.setPadding(new Insets(2, 6, 2, 6));
        objectiveBadge.setStyle(
                "-fx-background-color: " + (ThemeManager.isDark() ? "#0c4a6e" : "#e0f2fe") + ";" +
                "-fx-text-fill: " + (ThemeManager.isDark() ? "#7dd3fc" : "#0369a1") + ";" +
                "-fx-background-radius: 4;"
        );

        // Status badge
        Label statusBadge = new Label(route.getStatus().getDisplayName());
        statusBadge.setFont(Font.font("System", FontWeight.BOLD, 10));
        statusBadge.setPadding(new Insets(2, 6, 2, 6));
        if (route.isCompleted()) {
            statusBadge.setStyle(
                    "-fx-background-color: " + (ThemeManager.isDark() ? "#064e3b" : "#dcfce7") + ";" +
                    "-fx-text-fill: " + (ThemeManager.isDark() ? "#6ee7b7" : "#15803d") + ";" +
                    "-fx-background-radius: 4;"
            );
        } else {
            statusBadge.setStyle(
                    "-fx-background-color: " + (ThemeManager.isDark() ? "#451a03" : "#fef3c7") + ";" +
                    "-fx-text-fill: " + (ThemeManager.isDark() ? "#fcd34d" : "#b45309") + ";" +
                    "-fx-background-radius: 4;"
            );
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Favorite Button
        Button likeBtn = new Button(route.isLiked() ? "❤️ Liked" : "🤍 Like");
        likeBtn.setFont(Font.font("System", 11));
        likeBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + (route.isLiked() ? "#f43f5e" : ThemeManager.getTextMuted()) + ";" +
                "-fx-cursor: hand;"
        );
        likeBtn.setOnAction(e -> {
            savedRouteDao.toggleLiked(route.getId(), !route.isLiked());
            refreshLogbook();
        });

        HBox topRow = new HBox(8, nameLbl, eleLbl, objectiveBadge, statusBadge, spacer, likeBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Notes row
        Label notesLabel = new Label(route.getNotes().isEmpty() ? "No personal notes added yet." : "📝 \"" + route.getNotes() + "\"");
        notesLabel.setFont(Font.font("System", 12));
        notesLabel.setStyle("-fx-text-fill: " + (route.getNotes().isEmpty() ? ThemeManager.getTextMuted() : ThemeManager.getTextBody()) + ";");
        notesLabel.setWrapText(true);

        // Bottom Actions Row
        Label loggedTime = new Label("Logged: " + route.getLoggedAt());
        loggedTime.setFont(Font.font("System", 10));
        loggedTime.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + ";");

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        Button editNotesBtn = new Button("Edit Notes");
        editNotesBtn.setFont(Font.font("System", 10));
        editNotesBtn.setStyle(
                "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#f1f5f9") + ";" +
                "-fx-text-fill: " + ThemeManager.getTextBody() + ";" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 4 8;" +
                "-fx-cursor: hand;"
        );
        editNotesBtn.setOnAction(e -> promptEditNotes(route));

        Button actionBtn = null;
        if (route.isSaved()) {
            actionBtn = new Button("✓ Mark Summited");
            actionBtn.setFont(Font.font("System", FontWeight.BOLD, 10));
            actionBtn.setStyle(
                    "-fx-background-color: #16a34a;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 4;" +
                    "-fx-padding: 4 10;" +
                    "-fx-cursor: hand;"
            );
            actionBtn.setOnAction(e -> {
                savedRouteDao.updateStatus(route.getId(), SavedRoute.Status.COMPLETED);
                refreshLogbook();
            });
        }

        Button viewInPlannerBtn = new Button("🧭 View Route");
        viewInPlannerBtn.setFont(Font.font("System", FontWeight.BOLD, 10));
        viewInPlannerBtn.setStyle(
                "-fx-background-color: #0284c7;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 4 10;" +
                "-fx-cursor: hand;"
        );
        viewInPlannerBtn.setOnAction(e -> {
            Mountain mountain = mountainDao.getById(route.getMountainId());
            if (mountain != null && onNavigateToRouteFinder != null) {
                onNavigateToRouteFinder.accept(mountain);
            }
        });

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setFont(Font.font("System", 10));
        deleteBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #ef4444;" +
                "-fx-cursor: hand;"
        );
        deleteBtn.setOnAction(e -> {
            savedRouteDao.deleteRoute(route.getId());
            refreshLogbook();
        });

        HBox actionsRow = new HBox(8, loggedTime, bottomSpacer, editNotesBtn);
        if (actionBtn != null) {
            actionsRow.getChildren().add(actionBtn);
        }
        actionsRow.getChildren().addAll(viewInPlannerBtn, deleteBtn);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(topRow, notesLabel, actionsRow);
        return card;
    }

    private VBox buildEmptyState() {
        VBox emptyBox = new VBox(6);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(30, 20, 30, 20));

        Label icon = new Label("🎒");
        icon.setFont(Font.font("System", 24));

        Label title = new Label("No Logged Routes");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        title.setStyle("-fx-text-fill: " + ThemeManager.getTextTitle() + ";");

        Label desc = new Label("No entries found for this filter. Save routes or mark summits to build your logbook.");
        desc.setFont(Font.font("System", 11));
        desc.setStyle("-fx-text-fill: " + ThemeManager.getTextMuted() + "; -fx-text-alignment: center;");

        emptyBox.getChildren().addAll(icon, title, desc);
        return emptyBox;
    }

    private void promptEditNotes(SavedRoute route) {
        AlertHelper.showTextInputOverlay(
                "Expedition Notes",
                "Add Notes for " + route.getMountainName(),
                route.getNotes(),
                notes -> {
                    savedRouteDao.updateNotes(route.getId(), notes);
                    refreshLogbook();
                }
        );
    }

    private void promptSwitchProfile() {
        List<User> users = userDao.getAllUsers();
        AlertHelper.showProfilePickerOverlay(
                users,
                UserSession.getActiveUser(),
                selectedUser -> {
                    UserSession.setActiveUser(selectedUser);
                    refreshLogbook();
                },
                newUserName -> {
                    String baseUsername = newUserName.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");
                    if (baseUsername.isEmpty()) {
                        baseUsername = "climber";
                    }
                    String username = baseUsername;
                    if (userDao.getByUsername(username) != null) {
                        username = baseUsername + "_" + (System.currentTimeMillis() % 10000);
                    }
                    User created = userDao.createUser(username, newUserName.trim());
                    if (created != null) {
                        UserSession.setActiveUser(created);
                        refreshLogbook();
                    }
                }
        );
    }

    private String getActiveFilterStyle() {
        return "-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;";
    }

    private String getInactiveFilterStyle() {
        return "-fx-background-color: " + (ThemeManager.isDark() ? "#1e293b" : "#f1f5f9") + ";" +
                "-fx-text-fill: " + ThemeManager.getTextBody() + ";" +
                "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;";
    }
}
