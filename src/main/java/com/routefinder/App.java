package com.routefinder;

import com.routefinder.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for Mountain Route-Finder.
 * Boots the Stage -> Scene -> Scene Graph pipeline with MainView as root.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView();

        Scene scene = new Scene(mainView, 900, 600);
        primaryStage.setTitle("Mountain Route-Find" +
                "er");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
