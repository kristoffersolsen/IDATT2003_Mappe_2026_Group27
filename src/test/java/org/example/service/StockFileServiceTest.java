package org.example.service;

import org.example.model.Stock;
import org.example.model.StockFileRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockFileService")
class StockFileServiceTest {

  @TempDir
  static Path tempDir;

  private List<Stock> sampleStocks() {
    List<Stock> stocks = new ArrayList<>();
    stocks.add(new Stock("SYM1", "Company One", BigDecimal.valueOf(1.11)));
    stocks.add(new Stock("SYM2", "Company Two", BigDecimal.valueOf(2.22)));
    stocks.add(new Stock("SYM3", "Company Three", BigDecimal.valueOf(3.33)));
    return stocks;
  }

  private File tempFile(String name) {
    return tempDir.resolve(name).toFile();
  }

  @Nested
  @DisplayName("readStocks / writeStocks round-trip")
  class RoundTrip {

    @Test
    @DisplayName("stock count is preserved after write-read cycle")
    void stockCountPreserved() throws IOException {
      List<Stock> original = sampleStocks();
      File file = tempFile("rt_count.csv");
      StockFileService.writeStocks(new StockFileRecord(original, file));
      StockFileRecord record = StockFileService.readStocks(file);
      assertEquals(original.size(), record.getStocks().size());
    }

    @Test
    @DisplayName("symbols are preserved after write-read cycle")
    void symbolsPreserved() throws IOException {
      List<Stock> original = sampleStocks();
      File file = tempFile("rt_sym.csv");
      StockFileService.writeStocks(new StockFileRecord(original, file));
      List<Stock> read = StockFileService.readStocks(file).getStocks();
      for (int i = 0; i < original.size(); i++) {
        assertEquals(original.get(i).getSymbol(), read.get(i).getSymbol());
      }
    }

    @Test
    @DisplayName("company names are preserved after write-read cycle")
    void companyNamesPreserved() throws IOException {
      List<Stock> original = sampleStocks();
      File file = tempFile("rt_company.csv");
      StockFileService.writeStocks(new StockFileRecord(original, file));
      List<Stock> read = StockFileService.readStocks(file).getStocks();
      for (int i = 0; i < original.size(); i++) {
        assertEquals(original.get(i).getCompany(), read.get(i).getCompany());
      }
    }

    @Test
    @DisplayName("prices are preserved after write-read cycle")
    void pricesPreserved() throws IOException {
      List<Stock> original = sampleStocks();
      File file = tempFile("rt_prices.csv");
      StockFileService.writeStocks(new StockFileRecord(original, file));
      List<Stock> read = StockFileService.readStocks(file).getStocks();
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
      File file = tempFile("meta_desc.csv");
      StockFileService.writeStocks(
          new StockFileRecord(sampleStocks(), file, "Test description"));
      StockFileRecord record = StockFileService.readStocks(file);
      assertEquals("Test description", record.getDescription());
    }

    @Test
    @DisplayName("week number is stored and retrieved correctly")
    void weekRoundTrip() throws IOException {
      File file = tempFile("meta_week.csv");
      StockFileService.writeStocks(
          new StockFileRecord(sampleStocks(), file, "Desc", 42));
      StockFileRecord record = StockFileService.readStocks(file);
      assertEquals(42, record.getWeek());
    }

    @Test
    @DisplayName("file without description has null description")
    void noDescriptionIsNull() throws IOException {
      File file = tempFile("meta_nodesc.csv");
      StockFileService.writeStocks(
          new StockFileRecord(sampleStocks(), file));
      StockFileRecord record = StockFileService.readStocks(file);
      assertNull(record.getDescription());
    }

    @Test
    @DisplayName("file without week has week == -1")
    void noWeekIsMinusOne() throws IOException {
      File file = tempFile("meta_noweek.csv");
      StockFileService.writeStocks(
          new StockFileRecord(sampleStocks(), file));
      StockFileRecord record = StockFileService.readStocks(file);
      assertEquals(-1, record.getWeek());
    }

    @Test
    @DisplayName("both description and week survive a round-trip together")
    void fullMetadataRoundTrip() throws IOException {
      File file = tempFile("meta_full.csv");
      StockFileService.writeStocks(
          new StockFileRecord(sampleStocks(), file, "Full meta", 7));
      StockFileRecord record = StockFileService.readStocks(file);
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
      File file = tempFile("edge_empty.csv");
      StockFileService.writeStocks(
          new StockFileRecord(new ArrayList<>(), file));
      StockFileRecord record = StockFileService.readStocks(file);
      assertTrue(record.getStocks().isEmpty());
    }

    @Test
    @DisplayName("single stock survives round-trip")
    void singleStock() throws IOException {
      File file = tempFile("edge_single.csv");
      List<Stock> single = List.of(
          new Stock("ONE", "One Corp", BigDecimal.valueOf(99.99)));
      StockFileService.writeStocks(new StockFileRecord(single, file));
      List<Stock> read = StockFileService.readStocks(file).getStocks();
      assertEquals(1, read.size());
      assertEquals("ONE", read.get(0).getSymbol());
    }

    @Test
    @DisplayName("reading a non-existent file throws IOException")
    void readNonExistentFileThrows() {
      File missing = tempDir.resolve("does_not_exist.csv").toFile();
      assertThrows(IOException.class, () -> StockFileService.readStocks(missing));
    }
  }
}