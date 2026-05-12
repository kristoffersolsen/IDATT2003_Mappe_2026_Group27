package ntnu.idatt2003.millions.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.millions.market.model.Exchange;
import ntnu.idatt2003.millions.player.model.Player;
import ntnu.idatt2003.millions.player.model.Share;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.order.model.LimitOrder;
import ntnu.idatt2003.millions.order.model.OrderBook;
import ntnu.idatt2003.millions.order.model.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OrderService")
class OrderServiceTest {

  private static final BigDecimal STARTING_MONEY = new BigDecimal("10000");
  private static final String SYMBOL = "TSLA";

  private OrderService service;
  private Player player;
  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock(SYMBOL, "Tesla", new BigDecimal("200.00"));
    Exchange exchange = new Exchange("Test", 0L, List.of(stock));
    service = new OrderService(new OrderBook(), exchange);
    player = new Player("Bob", STARTING_MONEY);
    service.registerPlayer(player);
  }

  @Nested
  @DisplayName("placeLimitOrder — LIMIT_BUY")
  class PlaceLimitBuy {

    @Test
    @DisplayName("placeLimitBuy_sufficientCash_reservesMoney")
    void placeLimitBuy_sufficientCash_reservesMoney() {
      BigDecimal trigger = new BigDecimal("180.00");
      BigDecimal qty = new BigDecimal("10");
      BigDecimal gross = trigger.multiply(qty);             // 1800
      BigDecimal commission = gross.multiply(new BigDecimal("0.005")); // 9
      BigDecimal reservation = gross.add(commission);       // 1809

      BigDecimal cashBefore = player.getMoney();
      service.placeLimitOrder(player, limitBuy(trigger, qty));

      assertEquals(cashBefore.subtract(reservation), player.getMoney());
    }

    @Test
    @DisplayName("placeLimitBuy_sufficientCash_orderQueued")
    void placeLimitBuy_sufficientCash_orderQueued() {
      LimitOrder order = limitBuy(new BigDecimal("180.00"), BigDecimal.ONE);
      service.placeLimitOrder(player, order);
      assertEquals(List.of(order), service.getPendingOrders(player));
    }

    @Test
    @DisplayName("placeLimitBuy_insufficientCash_throws")
    void placeLimitBuy_insufficientCash_throws() {
      // Trigger price × qty far exceeds starting money
      LimitOrder order = limitBuy(new BigDecimal("200.00"), new BigDecimal("1000"));
      assertThrows(IllegalArgumentException.class,
          () -> service.placeLimitOrder(player, order));
    }

    @Test
    @DisplayName("placeLimitBuy_insufficientCash_noOrderQueued")
    void placeLimitBuy_insufficientCash_noOrderQueued() {
      LimitOrder order = limitBuy(new BigDecimal("200.00"), new BigDecimal("1000"));
      try {
        service.placeLimitOrder(player, order);
      } catch (IllegalArgumentException ignored) {
        // expected
      }
      assertTrue(service.getPendingOrders(player).isEmpty());
    }
  }

  @Nested
  @DisplayName("placeLimitOrder — LIMIT_SELL")
  class PlaceLimitSell {

    @BeforeEach
    void holdShares() {
      player.getPortfolio().addShare(
          new Share(stock, new BigDecimal("5"), new BigDecimal("180.00")));
    }

    @Test
    @DisplayName("placeLimitSell_sufficientShares_orderQueued")
    void placeLimitSell_sufficientShares_orderQueued() {
      LimitOrder order = limitSell(new BigDecimal("220.00"), new BigDecimal("5"));
      service.placeLimitOrder(player, order);
      assertEquals(List.of(order), service.getPendingOrders(player));
    }

    @Test
    @DisplayName("placeLimitSell_noShares_throws")
    void placeLimitSell_noShares_throws() {
      // Player holds 5, try to sell 10
      LimitOrder order = limitSell(new BigDecimal("220.00"), new BigDecimal("10"));
      assertThrows(IllegalArgumentException.class,
          () -> service.placeLimitOrder(player, order));
    }

    @Test
    @DisplayName("placeLimitSell_doesNotChangeCash")
    void placeLimitSell_doesNotChangeCash() {
      BigDecimal cashBefore = player.getMoney();
      service.placeLimitOrder(player,
          limitSell(new BigDecimal("220.00"), new BigDecimal("5")));
      assertEquals(cashBefore, player.getMoney());
    }
  }

  @Nested
  @DisplayName("cancelLimitOrder")
  class CancelLimitOrder {

    @Test
    @DisplayName("cancelLimitBuy_refundsReservation")
    void cancelLimitBuy_refundsReservation() {
      BigDecimal cashBefore = player.getMoney();
      LimitOrder order = limitBuy(new BigDecimal("180.00"), BigDecimal.ONE);
      service.placeLimitOrder(player, order);

      service.cancelLimitOrder(player, order);

      assertEquals(0, cashBefore.compareTo(player.getMoney()));
    }

    @Test
    @DisplayName("cancelLimitBuy_removesOrderFromBook")
    void cancelLimitBuy_removesOrderFromBook() {
      LimitOrder order = limitBuy(new BigDecimal("180.00"), BigDecimal.ONE);
      service.placeLimitOrder(player, order);
      service.cancelLimitOrder(player, order);
      assertTrue(service.getPendingOrders(player).isEmpty());
    }

    @Test
    @DisplayName("cancelOrder_notFound_throws")
    void cancelOrder_notFound_throws() {
      LimitOrder order = limitBuy(new BigDecimal("180.00"), BigDecimal.ONE);
      assertThrows(IllegalArgumentException.class,
          () -> service.cancelLimitOrder(player, order));
    }

    @Test
    @DisplayName("cancelLimitSell_doesNotChangeCash")
    void cancelLimitSell_doesNotChangeCash() {
      player.getPortfolio().addShare(
          new Share(stock, new BigDecimal("5"), new BigDecimal("180.00")));
      LimitOrder order = limitSell(new BigDecimal("220.00"), new BigDecimal("5"));
      service.placeLimitOrder(player, order);

      BigDecimal cashBefore = player.getMoney();
      service.cancelLimitOrder(player, order);

      assertEquals(cashBefore, player.getMoney());
    }
  }

  @Nested
  @DisplayName("evaluateOrders — integration")
  class EvaluateOrders {

    @Test
    @DisplayName("evaluateOrders_buyTriggerHit_sharesAdded")
    void evaluateOrders_buyTriggerHit_sharesAdded() {
      // Place limit buy at 210 (above current price 200), then drop price to 195
      LimitOrder order = limitBuy(new BigDecimal("210.00"), new BigDecimal("3"));
      service.placeLimitOrder(player, order);

      stock.addNewSalesPrice(new BigDecimal("195.00")); // now <= 210
      service.evaluateOrders(1L);

      assertTrue(service.getPendingOrders(player).isEmpty());
      assertFalse(player.getPortfolio().getShares().isEmpty());
      assertEquals(new BigDecimal("3"), player.getPortfolio().getShares().get(0).quantity());
    }

    @Test
    @DisplayName("evaluateOrders_buyTriggerNotHit_orderRetained")
    void evaluateOrders_buyTriggerNotHit_orderRetained() {
      // Current price 200, trigger 180 → not hit
      LimitOrder order = limitBuy(new BigDecimal("180.00"), BigDecimal.ONE);
      service.placeLimitOrder(player, order);

      service.evaluateOrders(1L);

      assertEquals(1, service.getPendingOrders(player).size());
      assertTrue(player.getPortfolio().getShares().isEmpty());
    }
  }

  // ------------- Helpers -------------

  private LimitOrder limitBuy(BigDecimal trigger, BigDecimal qty) {
    return new LimitOrder(SYMBOL, OrderType.LIMIT_BUY, qty, trigger, 0L);
  }

  private LimitOrder limitSell(BigDecimal trigger, BigDecimal qty) {
    return new LimitOrder(SYMBOL, OrderType.LIMIT_SELL, qty, trigger, 0L);
  }
}
