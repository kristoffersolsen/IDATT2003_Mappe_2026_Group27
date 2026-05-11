package ntnu.idatt2003.millions.controller;

import javafx.stage.Stage;
import ntnu.idatt2003.millions.config.GameContext;
import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.time.GameTime;
import ntnu.idatt2003.millions.service.ExchangeService;
import ntnu.idatt2003.millions.util.Format;
import ntnu.idatt2003.millions.view.DashboardView;
import ntnu.idatt2003.millions.view.EndView;
import ntnu.idatt2003.millions.view.StartView;

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
    StartView startView = new StartView();
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
    DashboardView dashboardView = new DashboardView();
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
    EndView endView = new EndView();
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
