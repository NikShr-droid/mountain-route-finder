package com.routefinder;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Entry point for Mountain Route-Finder.
 *
 * For now this just proves the JavaFX pipeline works end to end
 * (Stage -> Scene -> Scene Graph). MainView will replace the
 * placeholder center content once it exists.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setCenter(new Label("Mountain Route-Finder — setup OK"));

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("Mountain Route-Finder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
