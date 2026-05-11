package ntnu.idatt2003.millions.model.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LimitOrder")
class LimitOrderTest {

  @Nested
  @DisplayName("Record fields")
  class Fields {

    @Test
    @DisplayName("stockSymbol_returnsConstructorValue")
    void stockSymbol_returnsConstructorValue() {
      LimitOrder order = new LimitOrder("AAPL", OrderType.LIMIT_BUY,
          BigDecimal.TEN, new BigDecimal("150.00"), 42L);
      assertEquals("AAPL", order.stockSymbol());
    }

    @Test
    @DisplayName("type_returnsConstructorValue")
    void type_returnsConstructorValue() {
      LimitOrder order = new LimitOrder("TSLA", OrderType.LIMIT_SELL,
          BigDecimal.ONE, new BigDecimal("200.00"), 0L);
      assertEquals(OrderType.LIMIT_SELL, order.type());
    }

    @Test
    @DisplayName("quantity_returnsConstructorValue")
    void quantity_returnsConstructorValue() {
      BigDecimal qty = new BigDecimal("5");
      LimitOrder order = new LimitOrder("MSFT", OrderType.LIMIT_BUY,
          qty, new BigDecimal("300.00"), 10L);
      assertEquals(qty, order.quantity());
    }

    @Test
    @DisplayName("triggerPrice_returnsConstructorValue")
    void triggerPrice_returnsConstructorValue() {
      BigDecimal price = new BigDecimal("99.50");
      LimitOrder order = new LimitOrder("GOOG", OrderType.LIMIT_BUY,
          BigDecimal.ONE, price, 5L);
      assertEquals(price, order.triggerPrice());
    }

    @Test
    @DisplayName("placedAtTick_returnsConstructorValue")
    void placedAtTick_returnsConstructorValue() {
      LimitOrder order = new LimitOrder("AMZN", OrderType.LIMIT_SELL,
          BigDecimal.TEN, new BigDecimal("100.00"), 77L);
      assertEquals(77L, order.placedAtTick());
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("equals_sameValues_returnsTrue")
    void equals_sameValues_returnsTrue() {
      LimitOrder a = new LimitOrder("AAPL", OrderType.LIMIT_BUY,
          BigDecimal.TEN, new BigDecimal("150.00"), 1L);
      LimitOrder b = new LimitOrder("AAPL", OrderType.LIMIT_BUY,
          BigDecimal.TEN, new BigDecimal("150.00"), 1L);
      assertEquals(a, b);
    }

    @Test
    @DisplayName("equals_differentSymbol_returnsFalse")
    void equals_differentSymbol_returnsFalse() {
      LimitOrder a = new LimitOrder("AAPL", OrderType.LIMIT_BUY,
          BigDecimal.TEN, new BigDecimal("150.00"), 1L);
      LimitOrder b = new LimitOrder("MSFT", OrderType.LIMIT_BUY,
          BigDecimal.TEN, new BigDecimal("150.00"), 1L);
      assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals_differentType_returnsFalse")
    void equals_differentType_returnsFalse() {
      LimitOrder a = new LimitOrder("AAPL", OrderType.LIMIT_BUY,
          BigDecimal.TEN, new BigDecimal("150.00"), 1L);
      LimitOrder b = new LimitOrder("AAPL", OrderType.LIMIT_SELL,
          BigDecimal.TEN, new BigDecimal("150.00"), 1L);
      assertNotEquals(a, b);
    }
  }
}
