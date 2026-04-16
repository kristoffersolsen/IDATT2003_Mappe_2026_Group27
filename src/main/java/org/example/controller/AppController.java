package org.example.controller;

import javafx.stage.Stage;
import org.example.model.Player;
import org.example.service.Exchange;
import org.example.util.Format;
import org.example.view.DashboardView;
import org.example.view.EndView;
import org.example.view.StartView;

public class AppController {

  private final Stage stage;

  public AppController(Stage stage) {
    this.stage = stage;
    showStart();
  }

  public void showStart() {
    StartView startView = new StartView();
    new StartController(startView, stage, this);

    stage.getScene().setRoot(startView.getRoot());
  }

  public void startGame(Player player, Exchange exchange) {
    DashboardView dashboardView = new DashboardView();
    new DashboardController(dashboardView, player, exchange, this);

    stage.getScene().setRoot(dashboardView.getRoot());
  }

  /**
   * Shows the start screen.
   *
   * <p>Creates a fresh {@link StartView} and {@link StartController} each time,
   * so restarting a game always presents a clean form.
   */
  public void showStartScreen() {
    StartView startView = new StartView();
    new StartController(startView, stage, this);

    stage.getScene().setRoot(startView.getRoot());
    stage.setTitle("Millions");
  }

  /**
   * Transitions to the end screen after the player ends the game.
   *
   * <p>Formats the final summary values here so neither the view
   * nor the end controller needs to touch {@code BigDecimal}.
   *
   * @param player   the player whose session just ended
   * @param exchange the exchange at the time the game ended
   */
  public void showEndScreen(Player player, Exchange exchange) {
    EndView endView = new EndView();
    new EndController(endView, this);

    endView.populate(
        player.getName(),
        Format.formatMoney(player.getNetWorth()),
        Format.formatMoney(player.getNetWorth()),
        player.getStatus(exchange.getWeek()).getStatus());

    stage.getScene().setRoot(endView.getRoot());
    stage.setTitle("Millions — Game Over");
  }
}
