package com.routefinder.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Root BorderPane: left-side nav (Browse / Find Route / Map) with
 * swappable center content. This is the single App.java attaches to.
 */
public class MainView extends BorderPane {

    public MainView() {
        Button browseBtn = new Button("Browse");
        Button routeFinderBtn = new Button("Find Route");
        Button mapBtn = new Button("Map");

        browseBtn.setOnAction(e -> setCenter(new BrowseView()));
        routeFinderBtn.setOnAction(e -> setCenter(new RouteFinderView()));
        mapBtn.setOnAction(e -> setCenter(new MapView()));

        VBox nav = new VBox(10, browseBtn, routeFinderBtn, mapBtn);
        nav.setPadding(new Insets(16));
        nav.setAlignment(Pos.TOP_CENTER);
        nav.setPrefWidth(140);
        nav.setStyle("-fx-background-color: #f0f0f0;");

        setLeft(nav);
        setCenter(new RouteFinderView()); // default screen while we're testing it — easy to change later
    }
}
