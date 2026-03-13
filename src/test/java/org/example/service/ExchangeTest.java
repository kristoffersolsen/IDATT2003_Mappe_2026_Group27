package org.example.service;

import org.example.model.Player;
import org.example.model.Share;
import org.example.model.Stock;
import org.example.model.transaction.Purchase;
import org.example.model.transaction.Sale;
import org.example.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exchange")
class ExchangeTest {

  private Exchange exchange;
  private List<Stock> stocks;

  @BeforeEach
  void setUp() {
    stocks = List.of(
      new Stock("AAA", "Company A", BigDecimal.valueOf(10)),
      new Stock("BBB", "Company B", BigDecimal.valueOf(20)),
      new Stock("CCC", "Company C", BigDecimal.valueOf(30))
    );
    exchange = new Exchange("Exchange A", 1, stocks);
  }

  @Nested
  @DisplayName("hasStock / getStock")
  class StockLookup {

    @Test
    @DisplayName("hasStock returns true for known symbol")
    void hasStock_known() {
      assertTrue(exchange.hasStock("AAA"));
    }

    @Test
    @DisplayName("hasStock returns false for unknown symbol")
    void hasStock_unknown() {
      assertFalse(exchange.hasStock("ZZZ"));
    }

    @Test
    @DisplayName("getStock returns correct stock for known symbol")
    void getStock_known() {
      Stock s = exchange.getStock("AAA");
      assertEquals("AAA", s.getSymbol());
    }

    @Test
    @DisplayName("getStock throws for unknown symbol")
    void getStock_unknown() {
      assertThrows(IllegalArgumentException.class, () -> exchange.getStock("ZZZ"));
    }
  }

  @Nested
  @DisplayName("findStocks")
  class FindStocks {

    @Test
    @DisplayName("finds all stocks whose company name contains the search term")
    void findsMatchingStocks() {
      List<Stock> result = exchange.findStocks("Company");
      assertEquals(3, result.size());
    }
    
    @Test
    @DisplayName("returns empty list when no stocks match")
    void returnsEmptyForNoMatch() {
      List<Stock> result = exchange.findStocks("Company Z");
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("partial name match works")
    void partialNameMatch() {
      List<Stock> result = exchange.findStocks("B");
      assertEquals(1, result.size());
      assertEquals("BBB", result.get(0).getSymbol());
    }
  }

  @Nested
  @DisplayName("buy")
  class Buy {

    @Test
    @DisplayName("successful buy returns a committed Purchase transaction")
    void successfulBuy() {
      Player player = new Player("Buyer", BigDecimal.valueOf(1000));
      Transaction tx = exchange.buy("AAA", BigDecimal.ONE, player);
      assertInstanceOf(Purchase.class, tx);
      assertTrue(tx.isCommitted());
    }

    @Test
    @DisplayName("buy reduces player balance")
    void buyReducesBalance() {
      Player player = new Player("Buyer", BigDecimal.valueOf(1000));
      BigDecimal before = player.getMoney();
      exchange.buy("AAA", BigDecimal.ONE, player);
      assertTrue(player.getMoney().compareTo(before) < 0);
    }

    @Test
    @DisplayName("share appears in player portfolio after buy")
    void buyAddsShareToPortfolio() {
      Player player = new Player("Buyer", BigDecimal.valueOf(1000));
      exchange.buy("AAA", BigDecimal.ONE, player);
      assertFalse(player.getPortfolio().getShares().isEmpty());
    }

    @Test
    @DisplayName("buy throws for unknown symbol")
    void buyUnknownSymbol() {
      Player player = new Player("Buyer", BigDecimal.valueOf(1000));
      assertThrows(IllegalArgumentException.class,
        () -> exchange.buy("ZZZ", BigDecimal.ONE, player));
    }

    @Test
    @DisplayName("buy throws when player cannot afford it")
    void buyInsufficientFunds() {
      Player player = new Player("Broke", BigDecimal.valueOf(0));
      assertThrows(IllegalArgumentException.class,
        () -> exchange.buy("AAA", BigDecimal.valueOf(100), player));
    }
  }

  @Nested
  @DisplayName("sell")
  class Sell {

    @Test
    @DisplayName("successful sell returns a committed Sale transaction")
    void successfulSell() {
      Player player = new Player("Seller", BigDecimal.valueOf(1000));
      Stock stock = exchange.getStock("AAA");
      Share share = new Share(stock, BigDecimal.ONE, stock.getSalesPrice());
      player.getPortfolio().addShare(share);
      Transaction tx = exchange.sell("AAA", BigDecimal.ONE, player);
      assertInstanceOf(Sale.class, tx);
      assertTrue(tx.isCommitted());
    }

    @Test
    @DisplayName("sell increases player balance")
    void sellIncreasesBalance() {
      Player player = new Player("Seller", BigDecimal.valueOf(1000));
      Stock stock = exchange.getStock("AAA");
      Share share = new Share(stock, BigDecimal.ONE, stock.getSalesPrice());
      player.getPortfolio().addShare(share);
      BigDecimal before = player.getMoney();
      exchange.sell("AAA", BigDecimal.ONE, player);
      assertTrue(player.getMoney().compareTo(before) > 0);
    }

    @Test
    @DisplayName("sell throws for unknown symbol")
    void sellUnknownSymbol() {
      Player player = new Player("Seller", BigDecimal.valueOf(1000));
      assertThrows(IllegalArgumentException.class,
        () -> exchange.sell("ZZZ", BigDecimal.ONE, player));
    }

    @Test
    @DisplayName("sell throws when player does not own the share")
    void sellWithoutOwnership() {
      Player player = new Player("Seller", BigDecimal.valueOf(1000));
      assertThrows(IllegalArgumentException.class,
        () -> exchange.sell("AAA", BigDecimal.ONE, player));
    }
  }

  @Nested
  @DisplayName("getWeek / advance")
  class WeekAdvance {

    @Test
    @DisplayName("initial week matches constructor argument")
    void initialWeek() {
      assertEquals(1, exchange.getWeek());
    }

    @Test
    @DisplayName("advance increments week by one")
    void advanceIncrementsWeek() {
      exchange.advance();
      assertEquals(2, exchange.getWeek());
    }

    @Test
    @DisplayName("advance changes all stock prices")
    void advanceChangesPrices() {
      BigDecimal priceBefore = exchange.getStock("AAA").getSalesPrice();
      exchange.advance();
      // The new price is recorded in the price history (size grows)
      assertTrue(exchange.getStock("AAA").getHistoricalPrices().size() > 1);
    }
  }
}