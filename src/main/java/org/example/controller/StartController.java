package org.example.controller;

import java.io.File;
import java.math.BigDecimal;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.model.Player;
import org.example.model.StockFileRecord;
import org.example.service.Exchange;
import org.example.service.StockFileService;
import org.example.view.StartView;

public class StartController {

  private final StartView view;
  private final Stage stage;
  private final AppController appController;

  private File file;

  /**
   * Constructor.
   * @param view
   * @param stage
   * @param appController
   */
  public StartController(StartView view, Stage stage, AppController appController) {
    this.view = view;
    this.stage = stage;
    this.appController = appController;

    wireFileBrowseButton();
    wireStartButton();
  }

  /**
   * Wires the button to select the file. Controller holds file since it it directly used here.
   */
  private void wireFileBrowseButton() {
    view.getBrowseButton().setOnAction(e  -> {
      File file = view.showFileChooser(stage);
      if (file != null) {
        this.file = file;
        view.setFileName(file.getName());
      }
        }
    );
  }

  /**
   * Wires the start button. Calls the onStart method.
   */
  private void wireStartButton() {
    view.getStartButton().setOnAction(e  -> onStart());
  }

  /**
   * Validates user input, displays correct error messages and starts the game.
   */
  private void onStart() {
    String name = view.getPlayerName();
    String startingCapital = view.getStartingCapital();

    // Checking name
    if (name.isEmpty()) {
      view.showError("Player name is empty");
      return;
    }

    // Validating starting capital
    if (startingCapital.isEmpty()) {
      view.showError("Start capital is empty");
      return;
    }
    BigDecimal capital;
    try {
      capital = new BigDecimal(startingCapital);
    } catch (NumberFormatException e) {
      view.showError("Start capital is not a valid number");
      return;
    }

    // Validating file input
    if (file == null) {
      view.showError("Select a file");
      return;
    }

    StockFileRecord stockFile;
    try {
      stockFile = StockFileService.readStocks(file);
    } catch (Exception e) {
      view.showError("Error reading stock file: " + e.getMessage());
      return;
    }

    // Start game
    Player player = new Player(name, capital);
    Exchange exchange = new Exchange("Main Exchange", stockFile);

    appController.startGame(player, exchange);
  }
}
