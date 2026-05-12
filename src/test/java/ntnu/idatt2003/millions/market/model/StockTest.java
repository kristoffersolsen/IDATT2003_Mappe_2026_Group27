package ntnu.idatt2003.millions.market.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Stock")
class StockTest {

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAA", "Company A", BigDecimal.valueOf(150));
  }


  @Nested
  @DisplayName("Price history")
  class PriceHistory {

    @Test
    @DisplayName("initial historical list has exactly one entry")
    void historicalPrices_initiallyOneEntry() {
      assertEquals(1, stock.getHistoricalPrices().size());
    }

    @Test
    @DisplayName("addNewSalesPrice appends and getSalesPrice returns the latest")
    void addNewSalesPrice_updatesLatest() {
      stock.addNewSalesPrice(BigDecimal.valueOf(200));
      assertEquals(0, BigDecimal.valueOf(200).compareTo(stock.getSalesPrice()));
      assertEquals(2, stock.getHistoricalPrices().size());
    }

    @Test
    @DisplayName("multiple price additions preserve full history")
    void addNewSalesPrice_multipleAdditions() {
      stock.addNewSalesPrice(BigDecimal.valueOf(160));
      stock.addNewSalesPrice(BigDecimal.valueOf(170));
      stock.addNewSalesPrice(BigDecimal.valueOf(180));
      assertEquals(4, stock.getHistoricalPrices().size());
      assertEquals(0, BigDecimal.valueOf(180).compareTo(stock.getSalesPrice()));
    }
  }

  @Nested
  @DisplayName("getHighestPrice / getLowestPrice")
  class HighLow {

    @Test
    @DisplayName("highest price with single entry equals that entry")
    void highestPrice_singleEntry() {
      assertEquals(0, BigDecimal.valueOf(150).compareTo(stock.getHighestPrice()));
    }

    @Test
    @DisplayName("lowest price with single entry equals that entry")
    void lowestPrice_singleEntry() {
      assertEquals(0, BigDecimal.valueOf(150).compareTo(stock.getLowestPrice()));
    }

    @Test
    @DisplayName("highest price across multiple values is correct")
    void highestPrice_multipleEntries() {
      stock.addNewSalesPrice(BigDecimal.valueOf(300));
      stock.addNewSalesPrice(BigDecimal.valueOf(50));
      assertEquals(0, BigDecimal.valueOf(300).compareTo(stock.getHighestPrice()));
    }

    @Test
    @DisplayName("lowest price across multiple values is correct")
    void lowestPrice_multipleEntries() {
      stock.addNewSalesPrice(BigDecimal.valueOf(300));
      stock.addNewSalesPrice(BigDecimal.valueOf(50));
      assertEquals(0, BigDecimal.valueOf(50).compareTo(stock.getLowestPrice()));
    }
  }

  @Nested
  @DisplayName("getLatestPriceChange")
  class LatestPriceChange {

    @Test
    @DisplayName("returns null when only one price exists")
    void latestPriceChange_singleEntry_returnsNull() {
      assertEquals(BigDecimal.ZERO, stock.getLatestPriceChange());
    }

    @Test
    @DisplayName("positive change is calculated correctly")
    void latestPriceChange_positive() {
      stock.addNewSalesPrice(BigDecimal.valueOf(200));
      assertEquals(0, BigDecimal.valueOf(50).compareTo(stock.getLatestPriceChange()));
    }

    @Test
    @DisplayName("negative change is calculated correctly")
    void latestPriceChange_negative() {
      stock.addNewSalesPrice(BigDecimal.valueOf(100));
      assertEquals(0, BigDecimal.valueOf(-50).compareTo(stock.getLatestPriceChange()));
    }

    @Test
    @DisplayName("zero change when price stays the same")
    void latestPriceChange_zero() {
      stock.addNewSalesPrice(BigDecimal.valueOf(150));
      assertEquals(0, BigDecimal.ZERO.compareTo(stock.getLatestPriceChange()));
    }
  }


  @Test
  @DisplayName("toStringList returns symbol, company, price, dividend, and interval as strings")
  void toStringList() {
    String[] result = stock.toStringList();
    assertEquals(5, result.length);
    assertEquals("AAA", result[0]);
    assertEquals("Company A", result[1]);
    assertEquals("150", result[2]);
    assertEquals("0", result[3]);  // non-paying stock
    assertEquals("0", result[4]);
  }

  @Nested
  @DisplayName("sectorWeight")
  class SectorWeight {

    @Test
    @DisplayName("single-sector stock has weight 1.0 for its sector")
    void sectorWeight_singleSector() {
      Stock s = new Stock("A", "A Corp", BigDecimal.ONE, BigDecimal.ZERO, 0, Set.of(Sector.TECH));
      assertEquals(1.0, s.sectorWeight(Sector.TECH));
    }

    @Test
    @DisplayName("two-sector stock has weight 0.5 for each member sector")
    void sectorWeight_twoSectors() {
      Stock s = new Stock("A", "A Corp", BigDecimal.ONE, BigDecimal.ZERO, 0,
          Set.of(Sector.TECH, Sector.ENERGY));
      assertEquals(0.5, s.sectorWeight(Sector.TECH), 1e-9);
      assertEquals(0.5, s.sectorWeight(Sector.ENERGY), 1e-9);
    }

    @Test
    @DisplayName("returns 0.0 for a sector the stock does not belong to")
    void sectorWeight_nonMember_returnsZero() {
      Stock s = new Stock("A", "A Corp", BigDecimal.ONE, BigDecimal.ZERO, 0, Set.of(Sector.TECH));
      assertEquals(0.0, s.sectorWeight(Sector.FINANCE));
    }
  }

}
