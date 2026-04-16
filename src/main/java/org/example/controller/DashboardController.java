package org.example.controller;

import org.example.model.Player;
import org.example.service.Exchange;
import org.example.view.DashboardView;

public class DashboardController {

  private DashboardView view;
  private Player player;
  private Exchange exchange;
  private AppController appController;

  public DashboardController(DashboardView view, Player player, Exchange exchange,  AppController appController) {
    this.view = view;
    this.player = player;
    this.exchange = exchange;
    this.appController = appController;
  }

}
