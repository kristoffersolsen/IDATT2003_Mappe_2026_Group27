package ntnu.idatt2003.millions.controller;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.millions.config.GameContext;
import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.Stock;
import ntnu.idatt2003.millions.model.order.LimitOrder;
import ntnu.idatt2003.millions.model.order.OrderType;
import ntnu.idatt2003.millions.model.transaction.Transaction;
import ntnu.idatt2003.millions.service.ExchangeService;
import ntnu.idatt2003.millions.view.StockDetailView;

/**
 * Controller for the stock detail center panel.
 *
 * <p>Owns the buy and limit-buy interactions and keeps the
 * {@link StockDetailView} populated whenever the selected stock or game
 * state changes.
 */
public class StockDetailController {

  private final StockDetailView view;
  private final StockPriceChartController chartController;
  private final ExchangeService exchangeService;
  private final Player player;
  private final GameContext context;

  private Stock currentStock;
  private boolean limitBuyMode = false;

  /**
   * Creates the controller and wires the buy and limit-buy buttons.
   *
   * @param view            the stock detail view
   * @param exchangeService the exchange service to execute trades on
   * @param player          the active player
   * @param context         the game context providing settings and order service
   */
  public StockDetailController(
      StockDetailView view,
      ExchangeService exchangeService,
      Player player,
      GameContext context) {
    this.view = view;
    this.exchangeService = exchangeService;
    this.player = player;
    this.context = context;
    this.chartController = new StockPriceChartController(
        view.getChartView(), exchangeService, context.settings());

    wireBuyButton();
    wireLimitBuyToggle();
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
    chartController.showStock(stock);
    // Reset limit buy mode when switching stocks
    limitBuyMode = false;
    view.setLimitBuyMode(false);
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

    List<Transaction> stockTx = player.getTransactionArchive()
        .getTransactions()
        .stream()
        .filter(tx -> tx.getShare().stock().getSymbol()
            .equals(currentStock.getSymbol()))
        .toList();

    view.setTransactions(stockTx);
  }

  private void wireBuyButton() {
    view.getBuyButton().setOnAction(e -> {
      if (limitBuyMode) {
        onPlaceLimitBuy();
      } else {
        onBuy();
      }
    });
  }

  private void wireLimitBuyToggle() {
    view.getLimitBuyToggle().setOnAction(e -> {
      limitBuyMode = !limitBuyMode;
      view.setLimitBuyMode(limitBuyMode);
      view.showBuyError(null);
    });
  }

  private void wireQuantitySpinner() {
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
          BigDecimal.valueOf(quantity),
          player);
      view.showBuyError(null);
      refresh();
    } catch (IllegalArgumentException ex) {
      view.showBuyError(ex.getMessage());
    }
  }

  private void onPlaceLimitBuy() {
    if (currentStock == null || context.orderService() == null) {
      return;
    }

    String priceText = view.getTriggerPriceField().getText().trim();
    if (priceText.isEmpty()) {
      view.showBuyError("Enter a trigger price");
      return;
    }

    BigDecimal triggerPrice;
    try {
      triggerPrice = new BigDecimal(priceText);
    } catch (NumberFormatException ex) {
      view.showBuyError("Trigger price must be a number");
      return;
    }

    if (triggerPrice.signum() <= 0) {
      view.showBuyError("Trigger price must be positive");
      return;
    }

    int quantity = view.getQuantitySpinner().getValue();
    long tick = exchangeService.getExchange().getTickCount();

    LimitOrder order = new LimitOrder(
        currentStock.getSymbol(),
        OrderType.LIMIT_BUY,
        BigDecimal.valueOf(quantity),
        triggerPrice,
        tick);

    try {
      context.orderService().placeLimitOrder(player, order);
      view.showBuyError(null);
      view.getTriggerPriceField().clear();
    } catch (IllegalArgumentException ex) {
      view.showBuyError(ex.getMessage());
    }
  }
}
