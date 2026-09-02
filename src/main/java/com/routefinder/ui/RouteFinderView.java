package com.routefinder.ui;

import com.routefinder.model.CostFunction;
import com.routefinder.model.Mountain;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Route Finder screen: pick a mountain, hit "Find Route", see all three
 * cost-function results side by side (Google-Maps-style multi-route selector).
 *
 * Currently wired end to end EXCEPT the actual Dijkstra call — the button
 * correctly reads the ComboBox selection and reacts to it, but the three
 * cards show placeholder values until Graph/Dijkstra exist.
 */
public class RouteFinderView extends VBox {

    private final ComboBox<Mountain> mountainCombo = new ComboBox<>();

    public RouteFinderView() {
        setSpacing(20);
        setPadding(new Insets(24));

        Label heading = new Label("Route Finder");
        heading.setFont(Font.font("System", FontWeight.BOLD, 22));

        // Placeholder mountain list until MountainDao pulls these from SQLite
        mountainCombo.getItems().addAll(
                new Mountain(1, "Guru Shikhar"),
                new Mountain(2, "Kilimanjaro"),
                new Mountain(3, "Mont Blanc"),
                new Mountain(4, "Everest"),
                new Mountain(5, "Annapurna")
        );
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

        getChildren().addAll(heading, controls, routeCards);
    }

    /**
     * Proves the click -> read ComboBox value -> react pipeline works.
     * This is exactly where the Dijkstra call goes once Graph/Dijkstra
     * are implemented: for each CostFunction, build the Graph for the
     * selected mountain, run Dijkstra, and push results into the
     * matching card.
     */
    private void onFindRoute() {
        Mountain selected = mountainCombo.getValue();
        if (selected == null) {
            System.out.println("No mountain selected.");
            return;
        }
        System.out.println("Find Route clicked for: " + selected.getName());
        // TODO: run Dijkstra for DISTANCE / ELEVATION_GAIN / SAFETY on `selected`
        // and update each card's labels with the real numbers.
    }

    /**
     * Factory method for a single route-option card. Placeholder metrics
     * for now; costFunction is stashed as userData so we know which card
     * to update once real results come back.
     */
    private VBox buildRouteCard(String title, CostFunction costFunction) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label distance = new Label("Distance: --");
        Label elevation = new Label("Elevation gain: --");
        Label safety = new Label("Safety score: --");

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
