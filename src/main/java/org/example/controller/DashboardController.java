package org.example.controller;

import org.example.model.Stock;
import org.example.model.observer.GameEvent;
import org.example.model.observer.GameObserver;
import org.example.model.Player;
import org.example.service.ExchangeService;
import org.example.util.Format;
import org.example.view.DashboardView;

/**
 * Controller for the main dashboard.
 *
 * <p>Holds an {@link ExchangeService} for operations and observes the
 * underlying {@link org.example.model.Exchange} model for state changes.
 */
public class DashboardController implements GameObserver {

  private final DashboardView view;
  private final Player player;
  private final ExchangeService exchangeService;
  private final AppController appController;
  private final MarketController marketController;

  private boolean rightPanelVisible = false;
  private String currentRightPanel = "";

  /**
   * Creates the controller, registers as observer, and performs initial refresh.
   *
   * @param view            the dashboard view
   * @param player          the active player
   * @param exchangeService the active exchange service
   * @param appController   the app-level controller
   */
  public DashboardController(
      DashboardView view,
      Player player,
      ExchangeService exchangeService,
      AppController appController) {
    this.view = view;
    this.player = player;
    this.exchangeService = exchangeService;
    this.appController = appController;

    // Observe the model objects, not the service
    exchangeService.getExchange().addObserver(this);
    player.addObserver(this);

    this.marketController = new MarketController(
        view.getMarketView(),
        exchangeService,
        this::onStockSelected
    );

    wireAdvanceButton();
    wireEndGameButton();
    wirePortfolioButton();
    wireTransactionsButton();

    refresh();
  }

  @Override
  public void onEvent(Object source, GameEvent event) {
    switch (event) {
      case WEEK_ADVANCED -> {
        marketController.refresh();
        refresh();
      }
      case STOCK_PURCHASED, STOCK_SOLD, BALANCE_CHANGED, PORTFOLIO_CHANGED -> refresh();
      default -> { }
    }
  }

  private void onStockSelected(Stock stock) {
    // TODO: pass to StockDetailController once implemented
    System.out.println("Selected: " + stock.getSymbol());
  }

  private void wireAdvanceButton() {
    view.getAdvanceButton().setOnAction(e -> exchangeService.advance());
  }

  private void wireEndGameButton() {
    view.getEndGameButton().setOnAction(
        e -> appController.showEndScreen(player, exchangeService));
  }

  private void wirePortfolioButton() {
    view.getPortfolioButton().setOnAction(e -> toggleRightPanel("portfolio"));
  }

  private void wireTransactionsButton() {
    view.getTransactionsButton().setOnAction(e -> toggleRightPanel("transactions"));
  }

  private void toggleRightPanel(String panelName) {
    if (rightPanelVisible && currentRightPanel.equals(panelName)) {
      view.setRightPanel(null);
      rightPanelVisible = false;
      currentRightPanel = "";
    } else {
      view.setRightPanel(buildPlaceholderPanel(panelName));
      rightPanelVisible = true;
      currentRightPanel = panelName;
    }
  }

  private javafx.scene.layout.VBox buildPlaceholderPanel(String panelName) {
    javafx.scene.control.Label title = new javafx.scene.control.Label(
        panelName.equals("portfolio") ? "Portfolio" : "Recent Transactions");
    title.getStyleClass().add("font-title");

    javafx.scene.control.Label body = new javafx.scene.control.Label("Content coming soon.");
    body.getStyleClass().add("font-content");

    javafx.scene.layout.VBox panel = new javafx.scene.layout.VBox(16, title, body);
    panel.setPadding(new javafx.geometry.Insets(20));
    panel.setPrefWidth(300);
    panel.getStyleClass().add("content-white");
    return panel;
  }

  private void refresh() {
    view.setWeek(exchangeService.getExchange().getWeek());
    view.updatePlayerInfo(
        player.getName(),
        player.getStatus(exchangeService.getExchange().getWeek()).getStatus(),
        Format.formatMoney(player.getNetWorth()),
        Format.formatMoney(player.getMoney()),
        Format.formatMoney(player.getPortfolio().getNetWorth()));
  }
}