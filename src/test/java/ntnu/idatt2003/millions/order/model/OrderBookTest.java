package ntnu.idatt2003.millions.order.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.millions.market.model.Exchange;
import ntnu.idatt2003.millions.player.model.Player;
import ntnu.idatt2003.millions.market.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OrderBook")
class OrderBookTest {

  private static final BigDecimal STARTING_MONEY = new BigDecimal("100000");
  private static final String SYMBOL = "AAPL";

  private OrderBook book;
  private Player player;
  private Stock stock;
  private Exchange exchange;

  @BeforeEach
  void setUp() {
    book = new OrderBook();
    stock = new Stock(SYMBOL, "Apple", new BigDecimal("150.00"));
    player = new Player("Alice", STARTING_MONEY);
    exchange = new Exchange("Test", 0L, List.of(stock));
  }

  @Nested
  @DisplayName("getPendingOrders")
  class GetPendingOrders {

    @Test
    @DisplayName("getPendingOrders_noOrders_returnsEmpty")
    void getPendingOrders_noOrders_returnsEmpty() {
      assertTrue(book.getPendingOrders(player).isEmpty());
    }

    @Test
    @DisplayName("getPendingOrders_afterAdd_containsOrder")
    void getPendingOrders_afterAdd_containsOrder() {
      LimitOrder order = limitBuy("130.00");
      book.add(player, order);
      assertEquals(List.of(order), book.getPendingOrders(player));
    }
  }

  @Nested
  @DisplayName("remove")
  class Remove {

    @Test
    @DisplayName("remove_existingOrder_returnsTrue")
    void remove_existingOrder_returnsTrue() {
      LimitOrder order = limitBuy("130.00");
      book.add(player, order);
      assertTrue(book.remove(player, order));
      assertTrue(book.getPendingOrders(player).isEmpty());
    }

    @Test
    @DisplayName("remove_nonExistentOrder_returnsFalse")
    void remove_nonExistentOrder_returnsFalse() {
      assertFalse(book.remove(player, limitBuy("100.00")));
    }
  }

  @Nested
  @DisplayName("evaluate — LIMIT_BUY")
  class EvaluateLimitBuy {

    @Test
    @DisplayName("evaluate_priceAboveTrigger_orderNotExecuted")
    void evaluate_priceAboveTrigger_orderNotExecuted() {
      // current price 150, trigger 130 → not triggered
      LimitOrder order = limitBuy("130.00");
      book.add(player, order);

      book.evaluate(exchange, player, 1L);

      assertFalse(book.getPendingOrders(player).isEmpty());
      assertTrue(player.getPortfolio().getShares().isEmpty());
    }

    @Test
    @DisplayName("evaluate_priceAtTrigger_orderExecuted")
    void evaluate_priceAtTrigger_orderExecuted() {
      // trigger = current price exactly → should fire
      LimitOrder order = limitBuy("150.00");
      book.add(player, order);

      book.evaluate(exchange, player, 1L);

      assertTrue(book.getPendingOrders(player).isEmpty());
      assertFalse(player.getPortfolio().getShares().isEmpty());
    }

    @Test
    @DisplayName("evaluate_priceBelowTrigger_orderExecuted")
    void evaluate_priceBelowTrigger_orderExecuted() {
      // drop price to 140, trigger 150 → should fire
      stock.addNewSalesPrice(new BigDecimal("140.00"));
      LimitOrder order = limitBuy("150.00");
      book.add(player, order);

      book.evaluate(exchange, player, 1L);

      assertTrue(book.getPendingOrders(player).isEmpty());
      assertEquals(new BigDecimal("5"), player.getPortfolio().getShares().get(0).quantity());
    }

    @Test
    @DisplayName("evaluate_executedOrder_addedToArchive")
    void evaluate_executedOrder_addedToArchive() {
      stock.addNewSalesPrice(new BigDecimal("140.00"));
      LimitOrder order = limitBuy("150.00");
      book.add(player, order);

      book.evaluate(exchange, player, 5L);

      assertFalse(player.getTransactionArchive().getTransactions().isEmpty());
    }

    @Test
    @DisplayName("evaluate_executedOrder_removedFromBook")
    void evaluate_executedOrder_removedFromBook() {
      stock.addNewSalesPrice(new BigDecimal("140.00"));
      LimitOrder order1 = limitBuy("150.00");
      LimitOrder order2 = limitBuy("130.00"); // not triggered at 140
      book.add(player, order1);
      book.add(player, order2);

      book.evaluate(exchange, player, 1L);

      List<LimitOrder> remaining = book.getPendingOrders(player);
      assertEquals(1, remaining.size());
      assertEquals(order2, remaining.get(0));
    }
  }

  @Nested
  @DisplayName("evaluate — LIMIT_SELL")
  class EvaluateLimitSell {

    @BeforeEach
    void buyShares() {
      // Give the player 5 shares of AAPL at purchase price 140
      player.getPortfolio().addShare(
          new ntnu.idatt2003.millions.player.model.Share(stock, new BigDecimal("5"),
              new BigDecimal("140.00")));
    }

    @Test
    @DisplayName("evaluate_priceBelowTrigger_orderNotExecuted")
    void evaluate_priceBelowTrigger_orderNotExecuted() {
      // current price 150, trigger 160 → not triggered
      LimitOrder order = limitSell("160.00");
      book.add(player, order);

      book.evaluate(exchange, player, 1L);

      assertFalse(book.getPendingOrders(player).isEmpty());
      assertFalse(player.getPortfolio().getShares().isEmpty());
    }

    @Test
    @DisplayName("evaluate_priceAtTrigger_orderExecuted")
    void evaluate_priceAtTrigger_orderExecuted() {
      // trigger = current price exactly → should fire
      LimitOrder order = limitSell("150.00");
      book.add(player, order);

      book.evaluate(exchange, player, 1L);

      assertTrue(book.getPendingOrders(player).isEmpty());
      assertTrue(player.getPortfolio().getShares().isEmpty());
    }

    @Test
    @DisplayName("evaluate_priceAboveTrigger_orderExecuted")
    void evaluate_priceAboveTrigger_orderExecuted() {
      // raise price to 160, trigger 150 → should fire
      stock.addNewSalesPrice(new BigDecimal("160.00"));
      LimitOrder order = limitSell("150.00");
      book.add(player, order);

      book.evaluate(exchange, player, 1L);

      assertTrue(book.getPendingOrders(player).isEmpty());
      assertTrue(player.getPortfolio().getShares().isEmpty());
    }

    @Test
    @DisplayName("evaluate_sellExecution_creditsPlayer")
    void evaluate_sellExecution_creditsPlayer() {
      stock.addNewSalesPrice(new BigDecimal("160.00"));
      LimitOrder order = limitSell("150.00");
      book.add(player, order);

      BigDecimal cashBefore = player.getMoney();
      book.evaluate(exchange, player, 1L);

      assertTrue(player.getMoney().compareTo(cashBefore) > 0);
    }
  }

  // ------------- Helpers -------------

  private LimitOrder limitBuy(String trigger) {
    return new LimitOrder(SYMBOL, OrderType.LIMIT_BUY, new BigDecimal("5"),
        new BigDecimal(trigger), 0L);
  }

  private LimitOrder limitSell(String trigger) {
    return new LimitOrder(SYMBOL, OrderType.LIMIT_SELL, new BigDecimal("5"),
        new BigDecimal(trigger), 0L);
  }
}
