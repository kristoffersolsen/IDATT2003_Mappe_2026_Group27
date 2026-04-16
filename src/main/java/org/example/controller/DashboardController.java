package org.example.controller;

import org.example.model.Player;
import org.example.service.Exchange;
import org.example.util.Format;
import org.example.view.DashboardView;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Controller for the main dashboard.
 *
 * <p>Wires the {@link DashboardView} buttons to their actions and keeps
 * the view in sync with the model after each state change.
 * Child controllers (MarketController, StockDetailController, etc.)
 * will be added here as their views are implemented.
 */
public class DashboardController {

  private final DashboardView view;
  private final Player player;
  private final Exchange exchange;
  private final AppController appController;

  private boolean rightPanelVisible = false;
  private String currentRightPanel = "";

  /**
   * Creates the controller, wires buttons, and performs the initial UI refresh.
   *
   * @param view          the dashboard view
   * @param player        the active player
   * @param exchange      the active exchange
   * @param appController the app-level controller that handles screen transitions
   */
  public DashboardController(
      DashboardView view,
      Player player,
      Exchange exchange,
      AppController appController) {
    this.view = view;
    this.player = player;
    this.exchange = exchange;
    this.appController = appController;

    wireAdvanceButton();
    wireEndGameButton();
    wirePortfolioButton();
    wireTransactionsButton();

    refresh();
  }

  /**
   * Wires the advance button to move to the next week.
   */
  private void wireAdvanceButton() {
    view.getAdvanceButton().setOnAction(e -> onAdvance());
  }

  /**
   * Wires the end game button to sell all holdings and transition to the end screen.
   */
  private void wireEndGameButton() {
    view.getEndGameButton().setOnAction(e -> onEndGame());
  }

  /**
   * Wires the portfolio button to toggle the portfolio panel.
   */
  private void wirePortfolioButton() {
    view.getPortfolioButton().setOnAction(e -> toggleRightPanel("portfolio"));
  }

  /**
   * Wires the transactions button to toggle the transactions panel.
   */
  private void wireTransactionsButton() {
    view.getTransactionsButton().setOnAction(e -> toggleRightPanel("transactions"));
  }

  /**
   * Advances the exchange by one week and refreshes the view.
   */
  private void onAdvance() {
    exchange.advance();
    refresh();
  }

  /**
   * Ends the game by transitioning to the end screen.
   *
   * <p>Sells all holdings first so the net worth shown on the end screen
   * reflects only cash. Full sell-all logic will be added once
   * the portfolio and transaction infrastructure is complete.
   */
  private void onEndGame() {
    appController.showEndScreen(player, exchange);
  }

  /**
   * Toggles the right panel open or closed.
   *
   * <p>If the same panel is requested while already open, it closes.
   * If a different panel is requested, it replaces the current one.
   *
   * @param panelName the panel to show — "portfolio" or "transactions"
   */
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

  /**
   * Temporary placeholder panel builder.
   *
   * <p>Will be replaced by real PortfolioView and TransactionView nodes
   * once those classes are implemented.
   *
   * @param panelName the panel name to display as a title
   * @return a minimal VBox placeholder
   */
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

  /**
   * Refreshes all live values in the view to reflect the current model state.
   *
   * <p>Called after every state-changing action (advance, buy, sell).
   */
  private void refresh() {
    view.setWeek(exchange.getWeek());
    view.updatePlayerInfo(
        player.getName(),
        player.getStatus(exchange.getWeek()).getStatus(),
        Format.formatMoney(player.getNetWorth()),
        Format.formatMoney(player.getMoney()),
        Format.formatMoney(player.getPortfolio().getNetWorth()));
  }
}
