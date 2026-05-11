package ntnu.idatt2003.millions.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import ntnu.idatt2003.millions.model.Stock;
import ntnu.idatt2003.millions.model.StockFileRecord;

/**
 * Static utility for reading and writing {@link StockFileRecord} CSV files.
 *
 * <p>The CSV format has optional {@code metadata} rows at the top
 * (description, tick) followed by {@code Symbol,Name,Price} data rows.
 */
public class StockFileService {

  /**
   * Writes a list of stocks to a file with optional metadata rows.
   *
   * @param stockFileRecord the record to write
   * @throws IOException if writing fails
   */
  public static void writeStocks(StockFileRecord stockFileRecord) throws IOException {
    try (CSVWriter writer = new CSVWriter(
        new FileWriter(stockFileRecord.getFileName()))) {
      if (stockFileRecord.getDescription() != null) {
        writer.writeNext(
            new String[]{"metadata", "description", stockFileRecord.getDescription()});
      }
      if (stockFileRecord.getTick() != -1L) {
        writer.writeNext(
            new String[]{"metadata", "tick", String.valueOf(stockFileRecord.getTick())});
      }

      writer.writeNext(new String[]{"Symbol", "Name", "Price"});

      for (Stock stock : stockFileRecord.getStocks()) {
        writer.writeNext(stock.toStringList());
      }
    }
  }

  /**
   * Reads a CSV file containing stocks and optional metadata.
   *
   * @param file the file to read
   * @return a {@link StockFileRecord} populated from the file
   * @throws IOException if reading or parsing fails
   */
  public static StockFileRecord readStocks(File file) throws IOException {
    List<Stock> stocks = new ArrayList<>();
    String description = null;
    long tick = -1L;

    try (CSVReader reader = new CSVReader(new FileReader(file))) {

      String[] line;
      while ((line = reader.readNext()) != null) {
        if (!line[0].equals("metadata")) {
          break;
        }

        switch (line[1]) {
          case "description" -> description = line[2];
          case "tick" -> tick = Long.parseLong(line[2]);
          default -> throw new CsvValidationException("invalid metadata");
        }
      }

      String[] fields;
      while ((fields = reader.readNext()) != null) {
        stocks.add(new Stock(fields[0], fields[1], new BigDecimal(fields[2])));
      }
    } catch (CsvValidationException e) {
      throw new IOException("Failed to parse CSV: " + e.getMessage(), e);
    }

    return new StockFileRecord(stocks, file, description, tick);
  }
}
