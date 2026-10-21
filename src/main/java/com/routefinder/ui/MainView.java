package com.routefinder.ui;

import com.routefinder.model.Mountain;
import com.routefinder.model.UserSession;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Root Application Container and Shell.
 *
 * <p>Implements {@link AlertHelper.OverlayHost} to display modal sheets, warnings,
 * unseeded peak dialogs, and climber profile switchers directly over the active viewport
 * without opening detached OS-level window popups.</p>
 *
 * <p>Configures a dark alpine rock sidebar with:
 * <ul>
 *   <li><b>Default Screen:</b> {@link BrowseView} (Mountain Catalog)</li>
 *   <li><b>Navigation Items:</b> Browse Peaks, Find Routes, Elevation Chart, and My Logbook.</li>
 *   <li><b>Alpine Theme Toggle:</b> Seamlessly toggles between ☀️ Alpine Snow (Light)
 *       and 🌙 Midnight Sky (Dark) modes.</li>
 * </ul>
 */
public class MainView extends StackPane implements AlertHelper.OverlayHost {

    private final BorderPane appShell;
    private final StackPane overlayContainer;
    private Runnable activeOnDismiss;

    private final Button browseBtn;
    private final Button routeFinderBtn;
    private final Button mapBtn;
    private final Button logbookBtn;
    private final Button themeToggleBtn;
    private final Label activeUserPill;
    private final VBox sidebar;

    private Button activeNavButton;
    private Mountain activeMountainForRouteFinder = null;

    public MainView() {
        appShell = new BorderPane();

        // Register this view as the AlertHelper modal host
        AlertHelper.setOverlayHost(this);

        // App Brand Header
        Label brandIcon = new Label("🏔️");
        brandIcon.setFont(Font.font("System", 28));

        Label brandTitle = new Label("Route-Finder");
        brandTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        brandTitle.setStyle("-fx-text-fill: white;");

        Label brandSub = new Label("Alpine Ascent Planner");
        brandSub.setFont(Font.font("System", 10));
        brandSub.setStyle("-fx-text-fill: #94a3b8;");

        VBox brandBox = new VBox(2, brandIcon, brandTitle, brandSub);
        brandBox.setAlignment(Pos.CENTER);
        brandBox.setPadding(new Insets(12, 0, 20, 0));

        // Navigation Buttons
        browseBtn = createNavButton("🏔️  Browse Peaks");
        routeFinderBtn = createNavButton("🧭  Find Routes");
        mapBtn = createNavButton("📈  Elevation Chart");
        logbookBtn = createNavButton("🎒  My Logbook");

        browseBtn.setOnAction(e -> navigateTo(new BrowseView(this::showRouteFinder), browseBtn));
        routeFinderBtn.setOnAction(e -> navigateTo(new RouteFinderView(activeMountainForRouteFinder), routeFinderBtn));
        mapBtn.setOnAction(e -> navigateTo(new MapView(), mapBtn));
        logbookBtn.setOnAction(e -> navigateTo(new LogbookView(this::showRouteFinder), logbookBtn));

        VBox navButtons = new VBox(8, browseBtn, routeFinderBtn, mapBtn, logbookBtn);
        navButtons.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(navButtons, Priority.ALWAYS);

        // Active Climber Profile Pill
        activeUserPill = new Label("👤  " + UserSession.getActiveUser().getDisplayName());
        activeUserPill.setFont(Font.font("System", FontWeight.BOLD, 11));
        activeUserPill.setStyle(
                "-fx-background-color: #111928;" +
                "-fx-text-fill: #38bdf8;" +
                "-fx-border-color: #1e293b;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 6 10;" +
                "-fx-alignment: CENTER;"
        );
        activeUserPill.setMaxWidth(Double.MAX_VALUE);
        UserSession.addListener(u -> activeUserPill.setText("👤  " + u.getDisplayName()));

        // Alpine Theme Toggle Button
        themeToggleBtn = new Button(ThemeManager.isDark() ? "☀️  Alpine Snow" : "🌙  Midnight Sky");
        themeToggleBtn.setMaxWidth(Double.MAX_VALUE);
        themeToggleBtn.setStyle(
                "-fx-background-color: #1e293b;" +
                "-fx-text-fill: #38bdf8;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                "-fx-padding: 8 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        themeToggleBtn.setOnAction(e -> {
            ThemeManager.toggleTheme(getScene());
            themeToggleBtn.setText(ThemeManager.isDark() ? "☀️  Alpine Snow" : "🌙  Midnight Sky");
            refreshCurrentView();
        });

        // Sidebar Footer
        Label footer = new Label("Java 21 · JavaFX · SQLite");
        footer.setFont(Font.font("System", 10));
        footer.setStyle("-fx-text-fill: #64748b;");

        VBox footerBox = new VBox(8, activeUserPill, themeToggleBtn, footer);
        footerBox.setAlignment(Pos.CENTER);

        sidebar = new VBox(14, brandBox, navButtons, footerBox);
        sidebar.setPadding(new Insets(16, 14, 16, 14));
        sidebar.setAlignment(Pos.CENTER);
        sidebar.setPrefWidth(195);
        sidebar.setStyle("-fx-background-color: #070b14;");

        appShell.setLeft(sidebar);

        // Overlay Container Layer (Layer 1 above appShell Layer 0)
        overlayContainer = new StackPane();
        overlayContainer.setVisible(false);
        overlayContainer.setManaged(false);
        overlayContainer.setAlignment(Pos.CENTER);
        overlayContainer.setOnMouseClicked(e -> {
            // Dismiss when user clicks backdrop outside modal card
            if (e.getTarget() == overlayContainer) {
                hideOverlay();
            }
        });

        // Keyboard navigation: Escape key closes active overlay
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE && isOverlayActive()) {
                hideOverlay();
            }
        });

        // Add layers to StackPane root
        getChildren().addAll(appShell, overlayContainer);

        // Default screen: BROWSER PAGE as requested
        navigateTo(new BrowseView(this::showRouteFinder), browseBtn);
    }

    /**
     * Swaps the active center view inside appShell and updates navigation highlights.
     */
    private void navigateTo(Node view, Button activeBtn) {
        appShell.setCenter(view);
        this.activeNavButton = activeBtn;

        for (Button btn : new Button[]{browseBtn, routeFinderBtn, mapBtn, logbookBtn}) {
            if (btn == activeBtn) {
                btn.setStyle(
                        "-fx-background-color: #0284c7;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;" +
                        "-fx-alignment: BASELINE_LEFT;" +
                        "-fx-padding: 10 14;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
                );
            } else {
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-text-fill: #cbd5e1;" +
                        "-fx-font-weight: normal;" +
                        "-fx-font-size: 13px;" +
                        "-fx-alignment: BASELINE_LEFT;" +
                        "-fx-padding: 10 14;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
                );
            }
        }
    }

    /**
     * Displays a modal card over the viewport with dimmed backdrop and smooth entrance animation.
     */
    @Override
    public void showOverlay(Node modalNode, Runnable onDismiss) {
        this.activeOnDismiss = onDismiss;
        overlayContainer.getChildren().clear();

        String backdropColor = ThemeManager.isDark()
                ? "rgba(7, 11, 20, 0.75)"
                : "rgba(15, 23, 42, 0.45)";
        overlayContainer.setStyle("-fx-background-color: " + backdropColor + ";");
        overlayContainer.getChildren().add(modalNode);
        overlayContainer.setVisible(true);
        overlayContainer.setManaged(true);
        overlayContainer.toFront();

        // Subtle fade-in animation
        FadeTransition ft = new FadeTransition(Duration.millis(160), overlayContainer);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        // Slight scale-in for a refined native feel
        modalNode.setScaleX(0.95);
        modalNode.setScaleY(0.95);
        ScaleTransition st = new ScaleTransition(Duration.millis(160), modalNode);
        st.setFromX(0.95);
        st.setFromY(0.95);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    /**
     * Dismisses the currently active overlay with a smooth fade-out animation.
     */
    @Override
    public void hideOverlay() {
        if (!overlayContainer.isVisible()) {
            return;
        }

        FadeTransition ft = new FadeTransition(Duration.millis(120), overlayContainer);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            overlayContainer.setVisible(false);
            overlayContainer.setManaged(false);
            overlayContainer.getChildren().clear();
            if (activeOnDismiss != null) {
                Runnable r = activeOnDismiss;
                activeOnDismiss = null;
                r.run();
            }
        });
        ft.play();
    }

    @Override
    public boolean isOverlayActive() {
        return overlayContainer.isVisible();
    }

    /**
     * Re-renders the current screen to apply theme colors dynamically.
     */
    private void refreshCurrentView() {
        if (activeNavButton == browseBtn) {
            navigateTo(new BrowseView(this::showRouteFinder), browseBtn);
        } else if (activeNavButton == routeFinderBtn) {
            navigateTo(new RouteFinderView(activeMountainForRouteFinder), routeFinderBtn);
        } else if (activeNavButton == mapBtn) {
            navigateTo(new MapView(), mapBtn);
        } else if (activeNavButton == logbookBtn) {
            navigateTo(new LogbookView(this::showRouteFinder), logbookBtn);
        }
    }

    /**
     * Programmatically navigates to RouteFinderView with the selected mountain active.
     */
    public void showRouteFinder(Mountain mountain) {
        this.activeMountainForRouteFinder = mountain;
        navigateTo(new RouteFinderView(mountain), routeFinderBtn);
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }
}
