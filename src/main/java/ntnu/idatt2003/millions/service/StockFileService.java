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
 * (description, tick) followed by data rows with five columns:
 * {@code Symbol, Name, Price, Dividend, DividendInterval}.
 *
 * <p>The {@code Dividend} and {@code DividendInterval} columns are optional
 * when reading; files with only three columns are parsed with dividend values
 * of zero, preserving backward compatibility with existing stock files.
 */
public class StockFileService {

  private static final int DIVIDEND_COLUMN = 3;
  private static final int DIVIDEND_INTERVAL_COLUMN = 4;

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

      writer.writeNext(new String[]{"Symbol", "Name", "Price", "Dividend", "DividendInterval"});

      for (Stock stock : stockFileRecord.getStocks()) {
        writer.writeNext(stock.toStringList());
      }
    }
  }

  /**
   * Reads a CSV file containing stocks and optional metadata.
   *
   * <p>Data rows must have at least three columns (Symbol, Name, Price).
   * If {@code Dividend} and {@code DividendInterval} columns are present they
   * are parsed; missing or {@code "0"} values are treated as non-paying.
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

      // The previous loop consumed the header row — now read data rows
      String[] fields;
      while ((fields = reader.readNext()) != null) {
        BigDecimal dividendPerShare = BigDecimal.ZERO;
        int dividendIntervalHours = 0;

        if (fields.length > DIVIDEND_COLUMN
            && !fields[DIVIDEND_COLUMN].isEmpty()
            && !fields[DIVIDEND_COLUMN].equals("0")) {
          dividendPerShare = new BigDecimal(fields[DIVIDEND_COLUMN]);
        }
        if (fields.length > DIVIDEND_INTERVAL_COLUMN
            && !fields[DIVIDEND_INTERVAL_COLUMN].isEmpty()
            && !fields[DIVIDEND_INTERVAL_COLUMN].equals("0")) {
          dividendIntervalHours = Integer.parseInt(fields[DIVIDEND_INTERVAL_COLUMN]);
        }

        stocks.add(new Stock(fields[0], fields[1], new BigDecimal(fields[2]),
            dividendPerShare, dividendIntervalHours));
      }
    } catch (CsvValidationException e) {
      throw new IOException("Failed to parse CSV: " + e.getMessage(), e);
    }

    return new StockFileRecord(stocks, file, description, tick);
  }
}
