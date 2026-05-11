package ntnu.idatt2003.millions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.millions.model.Exchange;
import ntnu.idatt2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExchangeService")
class ExchangeServiceTest {

  private Exchange exchange;
  private ExchangeService exchangeService;
  private Stock stockA;
  private Stock stockB;
  private Stock stockC;

  @BeforeEach
  void setUp() {
    stockA = new Stock("AAA", "Company A", BigDecimal.valueOf(100));
    stockB = new Stock("BBB", "Company B", BigDecimal.valueOf(100));
    stockC = new Stock("CCC", "Company C", BigDecimal.valueOf(100));

    // Simulate a skip: mark skip start, then add new prices
    stockA.markSkipStart();
    stockB.markSkipStart();
    stockC.markSkipStart();

    stockA.addNewSalesPrice(BigDecimal.valueOf(120)); // skip change = +20
    stockB.addNewSalesPrice(BigDecimal.valueOf(80));  // skip change = -20
    // stockC: skip change = 0

    exchange = new Exchange("Test Exchange", 0L, List.of(stockA, stockB, stockC));
    exchangeService = ExchangeService.forTesting(exchange);
  }

  @Nested
  @DisplayName("getGainers")
  class Gainers {

    @Test
    @DisplayName("returns only stocks with a positive skip price change")
    void returnsOnlyPositiveChanges() {
      List<Stock> gainers = exchangeService.getGainers(10);
      assertTrue(gainers.stream().allMatch(s -> s.getSkipPriceChange().signum() > 0));
    }

    @Test
    @DisplayName("respects the limit parameter")
    void respectsLimit() {
      Stock stockD = new Stock("DDD", "Company D", BigDecimal.valueOf(100));
      stockD.markSkipStart();
      stockD.addNewSalesPrice(BigDecimal.valueOf(150));
      Exchange bigExchange = new Exchange("Big", 0L,
          List.of(stockA, stockB, stockC, stockD));
      ExchangeService bigService = ExchangeService.forTesting(bigExchange);

      List<Stock> gainers = bigService.getGainers(1);
      assertEquals(1, gainers.size());
    }

    @Test
    @DisplayName("returns empty list when no stocks have gained")
    void emptyWhenNoGainers() {
      Exchange loserExchange = new Exchange("Losers", 0L, List.of(stockB, stockC));
      ExchangeService loserService = ExchangeService.forTesting(loserExchange);
      assertTrue(loserService.getGainers(10).isEmpty());
    }

    @Test
    @DisplayName("gainers are sorted with largest gain first")
    void sortedDescending() {
      Stock bigGainer = new Stock("BIG", "Big Gainer", BigDecimal.valueOf(100));
      bigGainer.markSkipStart();
      bigGainer.addNewSalesPrice(BigDecimal.valueOf(200)); // skip change = +100
      Exchange sortExchange = new Exchange("Sort", 0L, List.of(stockA, bigGainer));
      ExchangeService sortService = ExchangeService.forTesting(sortExchange);

      List<Stock> gainers = sortService.getGainers(10);
      assertTrue(gainers.get(0).getSkipPriceChange()
          .compareTo(gainers.get(1).getSkipPriceChange()) > 0);
    }
  }

  @Nested
  @DisplayName("getLosers")
  class Losers {

    @Test
    @DisplayName("returns only stocks with a negative skip price change")
    void returnsOnlyNegativeChanges() {
      List<Stock> losers = exchangeService.getLosers(10);
      assertTrue(losers.stream().allMatch(s -> s.getSkipPriceChange().signum() < 0));
    }

    @Test
    @DisplayName("respects the limit parameter")
    void respectsLimit() {
      Stock stockE = new Stock("EEE", "Company E", BigDecimal.valueOf(100));
      stockE.markSkipStart();
      stockE.addNewSalesPrice(BigDecimal.valueOf(50));
      Exchange bigExchange = new Exchange("Big", 0L,
          List.of(stockA, stockB, stockC, stockE));
      ExchangeService bigService = ExchangeService.forTesting(bigExchange);

      List<Stock> losers = bigService.getLosers(1);
      assertEquals(1, losers.size());
    }

    @Test
    @DisplayName("returns empty list when no stocks have lost")
    void emptyWhenNoLosers() {
      Exchange gainerExchange = new Exchange("Gainers", 0L, List.of(stockA, stockC));
      ExchangeService gainerService = ExchangeService.forTesting(gainerExchange);
      assertTrue(gainerService.getLosers(10).isEmpty());
    }

    @Test
    @DisplayName("losers are sorted with largest loss first")
    void sortedAscending() {
      Stock bigLoser = new Stock("BIG", "Big Loser", BigDecimal.valueOf(100));
      bigLoser.markSkipStart();
      bigLoser.addNewSalesPrice(BigDecimal.valueOf(10)); // skip change = -90
      Exchange sortExchange = new Exchange("Sort", 0L, List.of(stockB, bigLoser));
      ExchangeService sortService = ExchangeService.forTesting(sortExchange);

      List<Stock> losers = sortService.getLosers(10);
      assertTrue(losers.get(0).getSkipPriceChange()
          .compareTo(losers.get(1).getSkipPriceChange()) < 0);
    }
  }

  @Nested
  @DisplayName("findStocks")
  class FindStocks {

    @Test
    @DisplayName("matches by company name")
    void matchesByCompanyName() {
      List<Stock> result = exchangeService.findStocks("Company A");
      assertEquals(1, result.size());
      assertEquals("AAA", result.get(0).getSymbol());
    }

    @Test
    @DisplayName("matches by symbol")
    void matchesBySymbol() {
      List<Stock> result = exchangeService.findStocks("BBB");
      assertEquals(1, result.size());
      assertEquals("BBB", result.get(0).getSymbol());
    }

    @Test
    @DisplayName("search is case-insensitive")
    void caseInsensitive() {
      List<Stock> result = exchangeService.findStocks("company a");
      assertEquals(1, result.size());
    }

    @Test
    @DisplayName("returns empty list for no match")
    void noMatch() {
      assertTrue(exchangeService.findStocks("ZZZZ").isEmpty());
    }
  }
}
