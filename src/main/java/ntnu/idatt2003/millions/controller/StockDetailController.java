package ntnu.idatt2003.millions.controller;

import java.util.List;
import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.Stock;
import ntnu.idatt2003.millions.model.transaction.Transaction;
import ntnu.idatt2003.millions.service.ExchangeService;
import ntnu.idatt2003.millions.view.StockDetailView;

/**
 * Controller for the stock detail center panel.
 *
 * <p>Owns the buy interaction and keeps the {@link StockDetailView}
 * populated whenever the selected stock or game state changes.
 */
public class StockDetailController {

  private final StockDetailView view;
  private final StockPriceChartController chartController;
  private final ExchangeService exchangeService;
  private final Player player;

  private Stock currentStock;

  /**
   * Creates the controller and wires the buy button.
   *
   * @param view            the stock detail view
   * @param exchangeService the exchange service to execute trades on
   * @param player          the active player
   */
  public StockDetailController(
      StockDetailView view,
      ExchangeService exchangeService,
      Player player) {
    this.view = view;
    this.exchangeService = exchangeService;
    this.player = player;
    this.chartController = new StockPriceChartController(view.getChartView(), exchangeService);

    wireBuyButton();
    wireQuantitySpinner();
  }

  /**
   * Switches the view to display the given stock.
   *
   * <p>Called by {@link DashboardController} when the user selects a
   * stock in the market sidebar.
   *
   * @param stock the stock to display
   */
  public void showStock(Stock stock) {
    this.currentStock = stock;
    view.showBuyError(null);
    refresh();
  }

  /**
   * Refreshes all displayed data for the current stock.
   *
   * <p>Called by {@link DashboardController} on every {@code GameEvent}
   * so prices, history, and the transaction log stay current.
   */
  public void refresh() {
    if (currentStock == null) {
      return;
    }

    view.setStock(currentStock);
    chartController.refresh(currentStock);

    // Filter the full archive to only this stock's symbol
    List<Transaction> stockTx = player.getTransactionArchive()
        .getTransactions()
        .stream()
        .filter(tx -> tx.getShare().stock().getSymbol()
            .equals(currentStock.getSymbol()))
        .toList();

    view.setTransactions(stockTx);
  }

  private void wireBuyButton() {
    view.getBuyButton().setOnAction(e -> onBuy());
  }

  private void wireQuantitySpinner() {
    // Keep estimated total live as the user changes quantity
    view.getQuantitySpinner().valueProperty().addListener(
        (obs, oldVal, newVal) -> {
          if (currentStock != null) {
            view.updateBuyTotal(currentStock.getSalesPrice());
          }
        });
  }

  private void onBuy() {
    if (currentStock == null) {
      return;
    }

    int quantity = view.getQuantitySpinner().getValue();

    try {
      exchangeService.buy(
          currentStock.getSymbol(),
          java.math.BigDecimal.valueOf(quantity),
          player);
      view.showBuyError(null);
      refresh();
    } catch (IllegalArgumentException ex) {
      view.showBuyError(ex.getMessage());
    }
  }
}