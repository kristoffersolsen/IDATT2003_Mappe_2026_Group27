package ntnu.idatt2003.millions.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import ntnu.idatt2003.millions.market.model.Exchange;
import ntnu.idatt2003.millions.player.model.Player;
import ntnu.idatt2003.millions.order.model.LimitOrder;
import ntnu.idatt2003.millions.order.model.OrderBook;
import ntnu.idatt2003.millions.order.model.OrderType;
import ntnu.idatt2003.millions.shared.observer.GameEvent;
import ntnu.idatt2003.millions.transaction.model.PurchaseCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages pending limit orders for all players.
 *
 * <p>Placement validates that the player has sufficient resources (cash for
 * limit buys, shares for limit sells) and reserves cash for limit buys.
 * Cancellation refunds reserved cash. Order evaluation is delegated to the
 * underlying {@link OrderBook} and is called once per simulated hour from
 * {@link ExchangeService#tick()}.
 */
public class OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderService.class);

  private final OrderBook orderBook;
  private final Exchange exchange;
  private final List<Player> players = new ArrayList<>();

  /**
   * Constructs the service.
   *
   * @param orderBook the order book that stores and evaluates pending orders
   * @param exchange  the exchange used to notify observers of order events
   */
  public OrderService(OrderBook orderBook, Exchange exchange) {
    this.orderBook = orderBook;
    this.exchange = exchange;
  }

  /**
   * Registers a player so their orders are evaluated on each tick.
   *
   * @param player the player to register
   */
  public void registerPlayer(Player player) {
    players.add(player);
  }

  /**
   * Places a limit order for the given player.
   *
   * <p>For {@link OrderType#LIMIT_BUY} orders the full reservation amount
   * ({@code quantity × triggerPrice + commission}) is debited immediately.
   * For {@link OrderType#LIMIT_SELL} orders the player must currently hold at
   * least the specified quantity.
   *
   * @param player the player placing the order
   * @param order  the limit order to place
   * @throws IllegalArgumentException if the player lacks sufficient cash
   *                                  (buy) or shares (sell)
   */
  public void placeLimitOrder(Player player, LimitOrder order) {
    if (order.type() == OrderType.LIMIT_BUY) {
      BigDecimal gross = order.quantity().multiply(order.triggerPrice());
      BigDecimal commission = gross.multiply(PurchaseCalculator.COMMISSION_RATE);
      BigDecimal reservation = gross.add(commission);

      if (player.getMoney().compareTo(reservation) < 0) {
        throw new IllegalArgumentException(
            "Insufficient cash to place limit buy: need $" + reservation
                + " but have $" + player.getMoney());
      }
      player.withdrawMoney(reservation);
    } else {
      var heldOpt = player.getPortfolio().getShareBySymbol(order.stockSymbol());
      if (heldOpt.isEmpty() || heldOpt.get().quantity().compareTo(order.quantity()) < 0) {
        throw new IllegalArgumentException(
            "Insufficient shares to place limit sell for " + order.stockSymbol());
      }
    }

    orderBook.add(player, order);
    log.info("Limit order placed: {} {} x {} @ {}",
        order.type(), order.stockSymbol(), order.quantity(), order.triggerPrice());
    exchange.notifyObservers(GameEvent.LIMIT_ORDER_PLACED);
  }

  /**
   * Cancels a previously placed limit order for the given player.
   *
   * <p>For {@link OrderType#LIMIT_BUY} orders the reserved cash is refunded
   * in full.
   *
   * @param player the player cancelling the order
   * @param order  the order to cancel
   * @throws IllegalArgumentException if the order is not found in the book
   */
  public void cancelLimitOrder(Player player, LimitOrder order) {
    boolean removed = orderBook.remove(player, order);
    if (!removed) {
      throw new IllegalArgumentException("Order not found in order book");
    }

    if (order.type() == OrderType.LIMIT_BUY) {
      BigDecimal gross = order.quantity().multiply(order.triggerPrice());
      BigDecimal commission = gross.multiply(PurchaseCalculator.COMMISSION_RATE);
      player.addMoney(gross.add(commission));
    }

    exchange.notifyObservers(GameEvent.LIMIT_ORDER_CANCELLED);
  }

  /**
   * Returns an unmodifiable view of all pending orders for the given player.
   *
   * @param player the player to query
   * @return pending orders, or an empty list if none
   */
  public List<LimitOrder> getPendingOrders(Player player) {
    return orderBook.getPendingOrders(player);
  }

  /**
   * Evaluates all pending orders for every registered player against current
   * market prices and executes any whose trigger condition is met.
   *
   * <p>Called from {@link ExchangeService#tick()} once per simulated hour
   * after stock prices have been updated.
   *
   * @param tick the current simulation tick
   */
  public void evaluateOrders(long tick) {
    for (Player player : players) {
      orderBook.evaluate(exchange, player, tick);
    }
  }
}
