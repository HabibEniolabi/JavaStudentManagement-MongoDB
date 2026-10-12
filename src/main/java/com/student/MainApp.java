package com.student;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/student-view.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root, 950, 650);

        scene.getStylesheets().add(
            getClass().getResource("/styles.css").toExternalForm()
        );

        stage.setTitle("Student Management System");
        stage.setScene(scene);
        stage.setMinWidth(750);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}