package ntnu.idatt2003.millions.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.millions.market.model.Exchange;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.market.service.ExchangeService;
import ntnu.idatt2003.millions.market.service.GameClock;
import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.shared.config.GameDefaults;
import ntnu.idatt2003.millions.shared.config.Difficulty;
import ntnu.idatt2003.millions.shared.time.GameTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GameClock")
class GameClockTest {

  private Exchange exchange;
  private ExchangeService exchangeService;
  private GameClock clock;

  @BeforeEach
  void setUp() {
    List<Stock> stocks = List.of(
        new Stock("AAA", "Company A", BigDecimal.valueOf(100))
    );
    exchange = new Exchange("Test", 0L, stocks);
    exchangeService = ExchangeService.forTesting(exchange);
    clock = new GameClock(exchangeService, ntnu.idatt2003.millions.shared.config.GameDefaults
        .forDifficulty(ntnu.idatt2003.millions.shared.config.Difficulty.NORMAL));
  }

  @Test
  @DisplayName("initial tick count is zero")
  void initialTickCountIsZero() {
    assertEquals(0L, clock.getTickCount());
  }

  @Nested
  @DisplayName("advanceBy")
  class AdvanceBy {

    @Test
    @DisplayName("advanceBy(1) calls tick() once and increments tickCount by 1")
    void advanceBy1() {
      clock.advanceBy(1);
      assertEquals(1L, clock.getTickCount());
    }

    @Test
    @DisplayName("advanceBy(5) calls tick() 5 times and increments tickCount by 5")
    void advanceBy5() {
      clock.advanceBy(5);
      assertEquals(5L, clock.getTickCount());
      // 5 new prices should have been added to each stock
      assertTrue(exchange.getStock("AAA").getHistoricalPrices().size() > 1);
    }

    @Test
    @DisplayName("advanceBy(168) increments tickCount by 168")
    void advanceBy168() {
      clock.advanceBy(168);
      assertEquals(168L, clock.getTickCount());
    }

    @Test
    @DisplayName("multiple advances accumulate correctly")
    void multipleAdvances() {
      clock.advanceBy(3);
      clock.advanceBy(7);
      assertEquals(10L, clock.getTickCount());
    }

    @Test
    @DisplayName("advanceBy marks skip start on all stocks before ticking")
    void markSkipStartBeforeLoop() {
      Stock stock = exchange.getStock("AAA");
      BigDecimal priceBefore = stock.getSalesPrice();
      clock.advanceBy(1);
      // priceAtSkipStart = priceBefore; getSkipPriceChange = currentPrice - priceBefore
      // so currentPrice - skipChange = priceBefore
      BigDecimal reconstructed = stock.getSalesPrice().subtract(stock.getSkipPriceChange());
      assertEquals(0, priceBefore.compareTo(reconstructed));
    }
  }

  @Nested
  @DisplayName("currentTime")
  class CurrentTime {

    @Test
    @DisplayName("currentTime reflects current tick count")
    void currentTimeReflectsTickCount() {
      clock.advanceBy(10);
      GameTime time = clock.currentTime();
      // At tick 10, week = 10/40 + 1 = 1
      assertEquals(1, time.getWeek());
    }
  }
}
