package ntnu.idatt2003.millions.controller;

import java.io.File;
import java.math.BigDecimal;
import java.util.Random;
import javafx.stage.Stage;
import ntnu.idatt2003.millions.config.Difficulty;
import ntnu.idatt2003.millions.config.GameContext;
import ntnu.idatt2003.millions.config.GameDefaults;
import ntnu.idatt2003.millions.config.GameSettings;
import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.StockFileRecord;
import ntnu.idatt2003.millions.model.order.OrderBook;
import ntnu.idatt2003.millions.model.time.GameClock;
import ntnu.idatt2003.millions.service.DividendService;
import ntnu.idatt2003.millions.service.ExchangeService;
import ntnu.idatt2003.millions.service.OrderService;
import ntnu.idatt2003.millions.service.StockFileService;
import ntnu.idatt2003.millions.view.StartView;

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

    Difficulty difficulty = view.getDifficulty();
    GameSettings settings = GameDefaults.forDifficulty(difficulty);

    Player player = new Player(name, capital);
    ExchangeService exchangeService = new ExchangeService("Main Exchange", stockFile, settings);

    OrderBook orderBook = new OrderBook();
    OrderService orderService = new OrderService(orderBook, exchangeService.getExchange());
    orderService.registerPlayer(player);
    exchangeService.setOrderService(orderService);

    DividendService dividendService = new DividendService(exchangeService.getExchange());
    dividendService.registerPlayer(player);
    exchangeService.setDividendService(dividendService);

    GameClock clock = new GameClock(exchangeService, settings);
    GameContext context = new GameContext(
        settings,
        new Random(settings.randomSeed()),
        clock,
        clock.currentTime(),
        orderService,
        dividendService);

    appController.startGame(player, exchangeService, context);
  }
}
