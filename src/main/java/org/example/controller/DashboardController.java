package org.example.controller;

import java.math.BigDecimal;
import org.example.model.Share;
import org.example.model.Stock;
import org.example.model.observer.GameEvent;
import org.example.model.observer.GameObserver;
import org.example.model.Player;
import org.example.service.ExchangeService;
import org.example.util.Format;
import org.example.view.DashboardView;
import org.example.view.PortfolioPanel;
import org.example.view.StockDetailView;
import org.example.view.TransactionsPanel;

/**
 * Controller for the main dashboard.
 *
 * <p>Holds an {@link ExchangeService} for operations and observes the
 * underlying {@link org.example.model.Exchange} model for state changes.
 * Delegates center-panel logic to {@link StockDetailController} and
 * side-panel logic to {@link MarketController}.
 */
public class DashboardController implements GameObserver {

  private final DashboardView view;
  private final Player player;
  private final ExchangeService exchangeService;
  private final AppController appController;

  private final MarketController marketController;
  private final StockDetailController stockDetailController;

  private final PortfolioPanel portfolioPanel;
  private final TransactionsPanel transactionsPanel;

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

    this.portfolioPanel = new PortfolioPanel(this::onSellShare);
    this.transactionsPanel = new TransactionsPanel();

    StockDetailView stockDetailView = new StockDetailView();
    view.setCenterPanel(stockDetailView);
    this.stockDetailController = new StockDetailController(
        stockDetailView, exchangeService, player);

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

    exchangeService.getExchange().getStocks().stream()
        .findFirst()
        .ifPresent(this::onStockSelected);

    refresh();
  }

  @Override
  public void onEvent(Object source, GameEvent event) {
    switch (event) {
      case WEEK_ADVANCED -> {
        marketController.refresh();
        stockDetailController.refresh();
        refresh();
      }
      case STOCK_PURCHASED, STOCK_SOLD, BALANCE_CHANGED, PORTFOLIO_CHANGED -> {
        stockDetailController.refresh();
        refresh();
      }
      default -> {
      }
    }
  }

  private void onStockSelected(Stock stock) {
    stockDetailController.showStock(stock);
  }

  /**
   * Forwards a partial or full sell from the portfolio modal to the service.
   *
   * @param share          the position being sold
   * @param quantityToSell the quantity chosen in the modal
   */
  private void onSellShare(Share share, BigDecimal quantityToSell) {
    try {
      exchangeService.sell(share.stock().getSymbol(), quantityToSell, player);
    } catch (IllegalArgumentException e) {
      System.err.println("Sell failed: " + e.getMessage());
    }
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
      view.setRightPanel(
          panelName.equals("portfolio") ? portfolioPanel : transactionsPanel);
      rightPanelVisible = true;
      currentRightPanel = panelName;
    }
  }

  private void refresh() {
    view.setWeek(exchangeService.getExchange().getWeek());
    view.updatePlayerInfo(
        player.getName(),
        player.getStatus(exchangeService.getExchange().getWeek()).getStatus(),
        Format.formatMoney(player.getNetWorth()),
        Format.formatMoney(player.getMoney()),
        Format.formatMoney(player.getPortfolio().getNetWorth()));

    portfolioPanel.setShares(player.getPortfolio().getShares());
    transactionsPanel.setTransactions(
        player.getTransactionArchive().getTransactions());
  }
}