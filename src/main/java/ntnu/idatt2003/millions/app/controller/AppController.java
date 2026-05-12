package ntnu.idatt2003.millions.app.controller;

import javafx.stage.Stage;
import ntnu.idatt2003.millions.app.config.GameContext;
import ntnu.idatt2003.millions.player.model.Player;
import ntnu.idatt2003.millions.shared.time.GameTime;
import ntnu.idatt2003.millions.market.service.ExchangeService;
import ntnu.idatt2003.millions.shared.util.Format;
import ntnu.idatt2003.millions.dashboard.view.DashboardScreen;
import ntnu.idatt2003.millions.end.view.EndScreen;
import ntnu.idatt2003.millions.start.view.StartScreen;
import ntnu.idatt2003.millions.dashboard.controller.DashboardController;
import ntnu.idatt2003.millions.start.controller.StartController;
import ntnu.idatt2003.millions.end.controller.EndController;

/**
 * Top-level controller that manages screen transitions.
 */
public class AppController {

  private final Stage stage;
  private DashboardController dashboardController;

  /**
   * Constructor. Shows the start screen immediately.
   *
   * @param stage the primary stage
   */
  public AppController(Stage stage) {
    this.stage = stage;
    showStartScreen();
  }

  /**
   * Shows the start screen.
   */
  public void showStartScreen() {
    disposeDashboard();
    StartScreen startView = new StartScreen();
    new StartController(startView, stage, this);
    stage.getScene().setRoot(startView.getRoot());
    stage.setTitle("Millions");
  }

  /**
   * Transitions to the dashboard and begins the game.
   *
   * @param player          the active player
   * @param exchangeService the active exchange service
   * @param context         the game context for this session
   */
  public void startGame(Player player, ExchangeService exchangeService, GameContext context) {
    disposeDashboard();
    DashboardScreen dashboardView = new DashboardScreen();
    dashboardController = new DashboardController(
        dashboardView, player, exchangeService, this, context);
    stage.getScene().setRoot(dashboardView.getRoot());
  }

  /**
   * Transitions to the end screen.
   *
   * @param player          the player whose session ended
   * @param exchangeService the exchange service at game end
   * @param context         the game context for this session
   */
  public void showEndScreen(Player player, ExchangeService exchangeService, GameContext context) {
    disposeDashboard();
    EndScreen endView = new EndScreen();
    new EndController(endView, this);

    GameTime finalTime = context.gameClock().currentTime();
    endView.populate(
        player.getName(),
        Format.formatMoney(player.getStartingMoney()),
        Format.formatMoney(player.getNetWorth()),
        player.getStatus(finalTime).getStatus());

    stage.getScene().setRoot(endView.getRoot());
    stage.setTitle("Millions — Game Over");
  }

  private void disposeDashboard() {
    if (dashboardController != null) {
      dashboardController.dispose();
      dashboardController = null;
    }
  }
}
