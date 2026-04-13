package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.example.controller.AppController;

/**
 * The main class.
 */
public class Main extends Application {

  @Override
  public void start(Stage stage) {

    stage.setTitle("Millions — Stock Trading Game");
    stage.setMinWidth(900);
    stage.setMinHeight(600);

    Scene scene = new Scene(new Region(), 900, 600);
    stage.setScene(scene);
    stage.show();

    new AppController(stage);
  }

  /**
   * Main function
   * @param args
   */
  public static void main(String[] args) {
    launch(args);
  }
}