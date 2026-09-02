package com.routefinder.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Mountain browser screen. Placeholder until MountainDao + TableView/card-list
 * are built.
 */
public class BrowseView extends VBox {
    public BrowseView() {
        setPadding(new Insets(24));
        getChildren().add(new Label("Mountain Browser — coming soon"));
    }
}
