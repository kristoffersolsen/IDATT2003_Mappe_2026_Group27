package org.example.controller;

import javafx.stage.Stage;
import org.example.model.Player;
import org.example.service.ExchangeService;
import org.example.util.Format;
import org.example.view.DashboardView;
import org.example.view.EndView;
import org.example.view.StartView;

/**
 * Top-level controller that manages screen transitions.
 */
public class AppController {

  private final Stage stage;

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
   */
  public void startGame(Player player, ExchangeService exchangeService) {
    DashboardView dashboardView = new DashboardView();
    new DashboardController(dashboardView, player, exchangeService, this);
    stage.getScene().setRoot(dashboardView.getRoot());
  }

  /**
   * Transitions to the end screen.
   *
   * @param player          the player whose session ended
   * @param exchangeService the exchange service at game end
   */
  public void showEndScreen(Player player, ExchangeService exchangeService) {
    EndView endView = new EndView();
    new EndController(endView, this);

    endView.populate(
        player.getName(),
        Format.formatMoney(player.getNetWorth()),
        Format.formatMoney(player.getNetWorth()),
        player.getStatus(exchangeService.getExchange().getWeek()).getStatus());

    stage.getScene().setRoot(endView.getRoot());
    stage.setTitle("Millions — Game Over");
  }
}