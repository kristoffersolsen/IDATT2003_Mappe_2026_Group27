package org.example.service;

import org.example.model.Stock;
import org.example.model.StockFileRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockFileService")
class StockFileServiceTest {

  private List<Stock> sampleStocks() {
    List<Stock> stocks = new ArrayList<>();
    stocks.add(new Stock("SYM1", "Company One", BigDecimal.valueOf(1.11)));
    stocks.add(new Stock("SYM2", "Company Two", BigDecimal.valueOf(2.22)));
    stocks.add(new Stock("SYM3", "Company Three", BigDecimal.valueOf(3.33)));
    return stocks;
  }

  private static final String DATA_PATH =
    System.getProperty("user.dir") + "/src/main/resources/data/stocks/";

  private static final List<String> TEST_FILES = List.of(
    "rt_count.csv", "rt_sym.csv", "rt_company.csv", "rt_prices.csv",
    "meta_desc.csv", "meta_week.csv", "meta_nodesc.csv", "meta_noweek.csv",
    "meta_full.csv", "edge_empty.csv", "edge_single.csv"
  );

  @AfterAll
  static void cleanUpTestFiles() {
    for (String fileName : TEST_FILES) {
      try {
        Files.deleteIfExists(Paths.get(DATA_PATH + fileName));
      } catch (IOException e) {
        System.err.println("Could not delete test file: " + fileName + " — " + e.getMessage());
      }
    }
  }


  @Nested
  @DisplayName("readStocks / writeStocks round-trip")
  class RoundTrip {

    @Test
    @DisplayName("stock count is preserved after write-read cycle")
    void stockCountPreserved() throws IOException {
      List<Stock> original = sampleStocks();
      StockFileService.writeStocks(new StockFileRecord(original, "rt_count.csv"));
      StockFileRecord record = StockFileService.readStocks("rt_count.csv");
      assertEquals(original.size(), record.getStocks().size());
    }

    @Test
    @DisplayName("symbols are preserved after write-read cycle")
    void symbolsPreserved() throws IOException {
      List<Stock> original = sampleStocks();
      StockFileService.writeStocks(new StockFileRecord(original, "rt_sym.csv"));
      List<Stock> read = StockFileService.readStocks("rt_sym.csv").getStocks();
      for (int i = 0; i < original.size(); i++) {
        assertEquals(original.get(i).getSymbol(), read.get(i).getSymbol());
      }
    }

    @Test
    @DisplayName("company names are preserved after write-read cycle")
    void companyNamesPreserved() throws IOException {
      List<Stock> original = sampleStocks();
      StockFileService.writeStocks(new StockFileRecord(original, "rt_company.csv"));
      List<Stock> read = StockFileService.readStocks("rt_company.csv").getStocks();
      for (int i = 0; i < original.size(); i++) {
        assertEquals(original.get(i).getCompany(), read.get(i).getCompany());
      }
    }

    @Test
    @DisplayName("prices are preserved after write-read cycle")
    void pricesPreserved() throws IOException {
      List<Stock> original = sampleStocks();
      StockFileService.writeStocks(new StockFileRecord(original, "rt_prices.csv"));
      List<Stock> read = StockFileService.readStocks("rt_prices.csv").getStocks();
      for (int i = 0; i < original.size(); i++) {
        assertEquals(0,
          original.get(i).getSalesPrice().compareTo(read.get(i).getSalesPrice()));
      }
    }
  }

  @Nested
  @DisplayName("metadata")
  class Metadata {

    @Test
    @DisplayName("description is stored and retrieved correctly")
    void descriptionRoundTrip() throws IOException {
      StockFileService.writeStocks(
        new StockFileRecord(sampleStocks(), "meta_desc.csv", "Test description"));
      StockFileRecord record = StockFileService.readStocks("meta_desc.csv");
      assertEquals("Test description", record.getDescription());
    }

    @Test
    @DisplayName("week number is stored and retrieved correctly")
    void weekRoundTrip() throws IOException {
      StockFileService.writeStocks(
        new StockFileRecord(sampleStocks(), "meta_week.csv", "Desc", 42));
      StockFileRecord record = StockFileService.readStocks("meta_week.csv");
      assertEquals(42, record.getWeek());
    }

    @Test
    @DisplayName("file without description has null description")
    void noDescriptionIsNull() throws IOException {
      StockFileService.writeStocks(
        new StockFileRecord(sampleStocks(), "meta_nodesc.csv"));
      StockFileRecord record = StockFileService.readStocks("meta_nodesc.csv");
      assertNull(record.getDescription());
    }

    @Test
    @DisplayName("file without week has week == -1")
    void noWeekIsMinusOne() throws IOException {
      StockFileService.writeStocks(
        new StockFileRecord(sampleStocks(), "meta_noweek.csv"));
      StockFileRecord record = StockFileService.readStocks("meta_noweek.csv");
      assertEquals(-1, record.getWeek());
    }

    @Test
    @DisplayName("both description and week survive a round-trip together")
    void fullMetadataRoundTrip() throws IOException {
      StockFileService.writeStocks(
        new StockFileRecord(sampleStocks(), "meta_full.csv", "Full meta", 7));
      StockFileRecord record = StockFileService.readStocks("meta_full.csv");
      assertEquals("Full meta", record.getDescription());
      assertEquals(7, record.getWeek());
    }
  }

  @Nested
  @DisplayName("edge cases")
  class EdgeCases {

    @Test
    @DisplayName("empty stock list can be written and read back")
    void emptyStockList() throws IOException {
      StockFileService.writeStocks(
        new StockFileRecord(new ArrayList<>(), "edge_empty.csv"));
      StockFileRecord record = StockFileService.readStocks("edge_empty.csv");
      assertTrue(record.getStocks().isEmpty());
    }

    @Test
    @DisplayName("single stock survives round-trip")
    void singleStock() throws IOException {
      List<Stock> single = List.of(new Stock("ONE", "One Corp", BigDecimal.valueOf(99.99)));
      StockFileService.writeStocks(new StockFileRecord(single, "edge_single.csv"));
      List<Stock> read = StockFileService.readStocks("edge_single.csv").getStocks();
      assertEquals(1, read.size());
      assertEquals("ONE", read.get(0).getSymbol());
    }
  }
}