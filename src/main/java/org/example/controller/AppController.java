package org.example.controller;

import javafx.stage.Stage;
import org.example.model.Player;
import org.example.service.Exchange;
import org.example.view.DashboardView;
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
}
