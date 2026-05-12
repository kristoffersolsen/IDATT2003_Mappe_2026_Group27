package ntnu.idatt2003.millions.order.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ntnu.idatt2003.millions.market.model.Exchange;
import ntnu.idatt2003.millions.player.model.Player;
import ntnu.idatt2003.millions.player.model.Share;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.shared.observer.GameEvent;
import ntnu.idatt2003.millions.transaction.model.Purchase;
import ntnu.idatt2003.millions.transaction.model.Sale;
import ntnu.idatt2003.millions.transaction.model.TransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds pending limit orders for all players and evaluates them against
 * current market prices each simulated tick.
 *
 * <p>Executions are collected before any events are fired to prevent
 * re-entrant observer notifications while iterating the order list.
 */
public class OrderBook {

  private static final Logger log = LoggerFactory.getLogger(OrderBook.class);

  private final Map<Player, List<LimitOrder>> orders = new HashMap<>();

  /**
   * Adds a pending limit order for the given player.
   *
   * @param player the player placing the order
   * @param order  the order to queue
   */
  public void add(Player player, LimitOrder order) {
    orders.computeIfAbsent(player, p -> new ArrayList<>()).add(order);
  }

  /**
   * Removes a pending limit order for the given player.
   *
   * @param player the player cancelling the order
   * @param order  the order to remove
   * @return true if the order was found and removed, false otherwise
   */
  public boolean remove(Player player, LimitOrder order) {
    List<LimitOrder> playerOrders = orders.get(player);
    if (playerOrders == null) {
      return false;
    }
    return playerOrders.remove(order);
  }

  /**
   * Returns an unmodifiable view of all pending orders for the given player.
   *
   * @param player the player to query
   * @return pending orders, or an empty list if none exist
   */
  public List<LimitOrder> getPendingOrders(Player player) {
    List<LimitOrder> playerOrders = orders.get(player);
    if (playerOrders == null) {
      return List.of();
    }
    return Collections.unmodifiableList(playerOrders);
  }

  /**
   * Evaluates all pending orders for the given player against current prices
   * and executes any whose trigger condition is met.
   *
   * <p>Triggered orders are collected first, then executed after the
   * iteration finishes, to prevent re-entrant observer calls during the loop.
   * Each executed order fires {@link GameEvent#LIMIT_ORDER_EXECUTED} on the
   * exchange after the order has been removed from the book.
   *
   * @param exchange the exchange providing current stock prices and event dispatch
   * @param player   the player whose orders should be evaluated
   * @param tick     the current simulation tick, used when recording transactions
   */
  public void evaluate(Exchange exchange, Player player, long tick) {
    List<LimitOrder> playerOrders = orders.get(player);
    if (playerOrders == null || playerOrders.isEmpty()) {
      return;
    }

    List<LimitOrder> toExecute = new ArrayList<>();
    for (LimitOrder order : playerOrders) {
      if (!exchange.hasStock(order.stockSymbol())) {
        continue;
      }
      BigDecimal currentPrice = exchange.getStock(order.stockSymbol()).getSalesPrice();
      boolean triggered = switch (order.type()) {
        case LIMIT_BUY -> currentPrice.compareTo(order.triggerPrice()) <= 0;
        case LIMIT_SELL -> currentPrice.compareTo(order.triggerPrice()) >= 0;
      };
      if (triggered) {
        toExecute.add(order);
      }
    }

    // Execute after collecting to avoid modifying the list during iteration
    for (LimitOrder order : toExecute) {
      playerOrders.remove(order);
      executeOrder(exchange, player, order, tick);
      exchange.notifyObservers(GameEvent.LIMIT_ORDER_EXECUTED);
    }
  }

  private void executeOrder(Exchange exchange, Player player, LimitOrder order, long tick) {
    Stock stock = exchange.getStock(order.stockSymbol());
    if (order.type() == OrderType.LIMIT_BUY) {
      // Cash was already reserved at placement; add the shares
      Share share = new Share(stock, order.quantity(), order.triggerPrice());
      player.getPortfolio().addShare(share);
      Purchase tx = TransactionFactory.createPurchase(share, tick);
      tx.markCommitted();
      player.getTransactionArchive().add(tx);
    } else {
      executeLimitSell(player, order, tick);
    }
  }

  private void executeLimitSell(Player player, LimitOrder order, long tick) {
    Optional<Share> heldOpt = player.getPortfolio().getShareBySymbol(order.stockSymbol());
    if (heldOpt.isEmpty()) {
      log.warn("Limit sell for {} skipped — player no longer holds the stock",
          order.stockSymbol());
      return;
    }
    Share held = heldOpt.get();
    if (held.quantity().compareTo(order.quantity()) < 0) {
      log.warn("Limit sell for {} skipped — only {} shares held, need {}",
          order.stockSymbol(), held.quantity(), order.quantity());
      return;
    }
    Share shareToSell = new Share(held.stock(), order.quantity(), held.purchasePrice());
    Sale tx = TransactionFactory.createSale(shareToSell, tick);
    player.addMoney(tx.getCalculator().calculateTotal());
    player.getPortfolio().removeShare(held, order.quantity());
    player.getTransactionArchive().add(tx);
    tx.markCommitted();
  }
}
