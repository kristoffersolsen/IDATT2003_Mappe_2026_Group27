package org.example.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.example.model.Stock;
import org.example.model.StockFileRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains information when reading/writing from a file. Used by Exchange to save its state.
 */
public class StockFileService {
  private static final String FILE_HEADER = "#Symbol, Name, Price";
  private static final String PATH_TO_FILES =
      System.getProperty("user.dir") + "/src/main/resources/data/stocks/";

  /**
   * Writes a list of Stocks to a file with a header description.
   *
   * @param stockFileRecord StockFilerecord to write, containing list of Stocks, filename and description
   * @throws IOException unable to write
   */
  public static void writeStocks(StockFileRecord stockFileRecord) throws IOException {
    try (CSVWriter writer = new CSVWriter(
        new FileWriter(stockFileRecord.getFileName()))) {
      // Write description as a metadata row
      if (stockFileRecord.getDescription() != null) {
        writer.writeNext(
            new String[] {"metadata", "description", stockFileRecord.getDescription()});
      }
      if (stockFileRecord.getWeek() != -1) {
        writer.writeNext(
            new String[] {"metadata", "week", String.valueOf(stockFileRecord.getWeek())});
      }


      writer.writeNext(new String[] {"Symbol", "Name", "Price"});

      for (Stock stock : stockFileRecord.getStocks()) {
        writer.writeNext(stock.toStringList());
      }
    }
  }

  /**
   * Reads a file containing stocks
   *
   * @param file file name to read from
   * @return a StockFileRecord
   * @throws IOException unable to read from file
   */
  public static StockFileRecord readStocks(File file) throws IOException {
    List<Stock> stocks = new ArrayList<>();
    String description = null;
    int week = -1;

    try (CSVReader reader = new CSVReader(new FileReader(file))) {

      // read metadata
      String[] line;
      while ((line = reader.readNext()) != null) {
        if (!line[0].equals("metadata")) {
          break;
        }

        switch (line[1]) {
          case "description" -> description = line[2];
          case "week" -> week = Integer.parseInt(line[2]);
          default -> throw new CsvValidationException("invalid metatada");
        }
      }

      // read the main csv data
      String[] fields;
      while ((fields = reader.readNext()) != null) {
        stocks.add(new Stock(fields[0], fields[1], new BigDecimal(fields[2])));
      }
    } catch (CsvValidationException e) {
      throw new IOException("Failed to parse CSV: " + e.getMessage(), e);
    }

    return new StockFileRecord(stocks, file, description, week);
  }
}
