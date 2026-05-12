package ntnu.idatt2003.millions.market.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import ntnu.idatt2003.millions.market.model.Sector;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.market.model.StockFileRecord;

/**
 * Static utility for reading and writing {@link StockFileRecord} CSV files.
 *
 * <p>The CSV format has optional {@code metadata} rows at the top
 * (description, tick) followed by data rows with five or six columns:
 * {@code Symbol, Name, Price, Dividend, DividendInterval[, Sectors]}.
 *
 * <p>The {@code Dividend} and {@code DividendInterval} columns are optional
 * when reading; files with only three columns are parsed with dividend values
 * of zero, preserving backward compatibility with existing stock files.
 * The {@code Sectors} column is optional; a missing or empty value maps to
 * {@link Sector#UNCATEGORIZED}.
 */
public class StockFileService {

  private static final int DIVIDEND_COLUMN = 3;
  private static final int DIVIDEND_INTERVAL_COLUMN = 4;
  private static final int SECTOR_COLUMN = 5;
  private static final Set<Sector> DEFAULT_SECTORS = Set.of(Sector.UNCATEGORIZED);

  /**
   * Writes a list of stocks to a file with optional metadata rows.
   *
   * <p>The sector column is written when at least one stock has non-default sectors.
   *
   * @param stockFileRecord the record to write
   * @throws IOException if writing fails
   */
  public static void writeStocks(StockFileRecord stockFileRecord) throws IOException {
    boolean writeSectors = stockFileRecord.getStocks().stream()
        .anyMatch(s -> !s.getSectors().equals(DEFAULT_SECTORS));

    try (CSVWriter writer = new CSVWriter(
        new FileWriter(stockFileRecord.getFileName()))) {
      if (stockFileRecord.getDescription() != null) {
        writer.writeNext(
            new String[] {"metadata", "description", stockFileRecord.getDescription()});
      }
      if (stockFileRecord.getTick() != -1L) {
        writer.writeNext(
            new String[] {"metadata", "tick", String.valueOf(stockFileRecord.getTick())});
      }

      if (writeSectors) {
        writer.writeNext(
            new String[] {"Symbol", "Name", "Price", "Dividend", "DividendInterval", "Sectors"});
      } else {
        writer.writeNext(
            new String[] {"Symbol", "Name", "Price", "Dividend", "DividendInterval"});
      }

      for (Stock stock : stockFileRecord.getStocks()) {
        if (writeSectors) {
          String[] base = stock.toStringList();
          String[] row = Arrays.copyOf(base, base.length + 1);
          row[row.length - 1] = sectorsToString(stock.getSectors());
          writer.writeNext(row);
        } else {
          writer.writeNext(stock.toStringList());
        }
      }
    }
  }

  private static String sectorsToString(Set<Sector> sectors) {
    return sectors.stream()
        .sorted(Comparator.comparing(Sector::name))
        .map(Sector::name)
        .collect(Collectors.joining("|"));
  }

  /**
   * Reads a CSV file containing stocks and optional metadata.
   *
   * <p>Data rows must have at least three columns (Symbol, Name, Price).
   * If {@code Dividend} and {@code DividendInterval} columns are present they
   * are parsed; missing or {@code "0"} values are treated as non-paying.
   * If a {@code Sectors} column is present it is parsed as pipe-separated
   * {@link Sector} names; a missing or empty value maps to
   * {@link Sector#UNCATEGORIZED}. An unrecognized sector name throws
   * {@link IOException} with the data row number.
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
      int dataRow = 0;
      String[] fields;
      while ((fields = reader.readNext()) != null) {
        dataRow++;
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

        Set<Sector> sectors = parseSectors(fields, dataRow);
        stocks.add(new Stock(fields[0], fields[1], new BigDecimal(fields[2]),
            dividendPerShare, dividendIntervalHours, sectors));
      }
    } catch (CsvValidationException e) {
      throw new IOException("Failed to parse CSV: " + e.getMessage(), e);
    }

    return new StockFileRecord(stocks, file, description, tick);
  }

  private static Set<Sector> parseSectors(String[] fields, int dataRow) throws IOException {
    if (fields.length <= SECTOR_COLUMN || fields[SECTOR_COLUMN].isEmpty()) {
      return DEFAULT_SECTORS;
    }
    String[] parts = fields[SECTOR_COLUMN].split("\\|");
    EnumSet<Sector> result = EnumSet.noneOf(Sector.class);
    for (String part : parts) {
      try {
        result.add(Sector.valueOf(part.trim()));
      } catch (IllegalArgumentException e) {
        throw new IOException(
            "Unknown sector '" + part.trim() + "' at data row " + dataRow, e);
      }
    }
    return result.isEmpty() ? DEFAULT_SECTORS : Set.copyOf(result);
  }
}
