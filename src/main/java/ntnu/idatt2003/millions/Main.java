package ntnu.idatt2003.millions;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import ntnu.idatt2003.millions.app.controller.AppController;
import ntnu.idatt2003.millions.shared.view.ErrorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class.
 */
public class Main extends Application {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  @Override
  public void start(Stage stage) {
    Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
      log.error("Uncaught exception on FX thread", throwable);
      ErrorDialog.show("Unexpected error", throwable.getMessage());
    });

    stage.setTitle("Millions — Stock Trading Game");
    stage.setMinWidth(900);
    stage.setMinHeight(600);

    Scene scene = new Scene(new Region(), 1300, 800);
    stage.setScene(scene);
    stage.show();
    String[] cssFiles = {
        "/style.css",
        "/css/theme.css",
        "/css/dashboard.css",
        "/css/portfolio.css",
        "/css/transactions.css",
        "/css/orders.css",
        "/css/start-end.css"
    };
    for (String cssFile : cssFiles) {
      try {
        scene.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
      } catch (NullPointerException e) {
        log.error("CSS stylesheet could not be loaded: {}", cssFile, e);
      }
    }
    new AppController(stage);
  }

  /**
   * Main entry point.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
}
