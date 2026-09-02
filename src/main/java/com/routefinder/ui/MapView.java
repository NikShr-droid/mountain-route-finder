package com.routefinder.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Elevation profile + route map screen. Placeholder until the
 * LineChart/WebView visualization is built.
 */
public class MapView extends VBox {
    public MapView() {
        setPadding(new Insets(24));
        getChildren().add(new Label("Map / Elevation Profile — coming soon"));
    }
}
