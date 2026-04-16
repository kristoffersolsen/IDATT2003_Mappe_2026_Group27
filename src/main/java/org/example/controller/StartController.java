package org.example.controller;

import java.io.File;
import java.math.BigDecimal;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.model.Player;
import org.example.model.StockFileRecord;
import org.example.service.ExchangeService;
import org.example.service.StockFileService;
import org.example.view.StartView;

/**
 * Controller for the start screen.
 *
 * <p>Validates user input and hands off to {@link AppController} to begin
 * the game once a valid configuration is provided.
 */
public class StartController {

  private final StartView view;
  private final Stage stage;
  private final AppController appController;

  private File file;

  /**
   * Constructor.
   *
   * @param view          the start view
   * @param stage         the primary stage
   * @param appController the app-level controller
   */
  public StartController(StartView view, Stage stage, AppController appController) {
    this.view = view;
    this.stage = stage;
    this.appController = appController;

    wireFileBrowseButton();
    wireStartButton();
  }

  private void wireFileBrowseButton() {
    view.getBrowseButton().setOnAction(e -> {
      File chosen = view.showFileChooser(stage);
      if (chosen != null) {
        this.file = chosen;
        view.setFileName(chosen.getName());
      }
    });
  }

  private void wireStartButton() {
    view.getStartButton().setOnAction(e -> onStart());
  }

  private void onStart() {
    String name = view.getPlayerName();
    String startingCapital = view.getStartingCapital();

    if (name.isEmpty()) {
      view.showError("Player name is empty");
      return;
    }
    if (startingCapital.isEmpty()) {
      view.showError("Starting capital is empty");
      return;
    }

    BigDecimal capital;
    try {
      capital = new BigDecimal(startingCapital);
    } catch (NumberFormatException e) {
      view.showError("Starting capital is not a valid number");
      return;
    }

    if (file == null) {
      view.showError("Please select a stock data file");
      return;
    }

    StockFileRecord stockFile;
    try {
      stockFile = StockFileService.readStocks(file);
    } catch (Exception e) {
      view.showError("Error reading stock file: " + e.getMessage());
      return;
    }

    Player player = new Player(name, capital);
    ExchangeService exchangeService = new ExchangeService("Main Exchange", stockFile);

    appController.startGame(player, exchangeService);
  }
}