package org.example.model;

import org.example.service.StockFileService;

import java.io.IOException;
import java.util.List;

/**
 * Holds information about a read/write of stocks from a file.
 */
public class StockFileRecord {

  List<Stock> stocks;
  String fileName;
  String description;
  int week;

  /**
   * Default constructor with only list of stocks and filename.
   *
   * @param stocks   stocks to read/write
   * @param fileName the filename
   */
  public StockFileRecord(List<Stock> stocks, String fileName) {
    this.stocks = stocks;
    this.fileName = fileName;
    this.description = null;
    this.week = -1;
  }

  /**
   * Constructor with description.
   *
   * @param stocks      Stocks to read/write
   * @param fileName    Filename
   * @param description description
   */
  public StockFileRecord(List<Stock> stocks, String fileName, String description) {
    this.stocks = stocks;
    this.fileName = fileName;
    this.description = description;
    this.week = -1;
  }

  /**
   * Constructor with description and week number.
   *
   * @param stocks      stocks to read/write
   * @param fileName    filename
   * @param description description
   * @param week        weeknumber
   */
  public StockFileRecord(List<Stock> stocks, String fileName, String description, int week) {
    this.stocks = stocks;
    this.fileName = fileName;
    this.description = description;
    this.week = week;
  }

  public List<Stock> getStocks() {
    return stocks;
  }

  public String getFileName() {
    return this.fileName;
  }

  public String getDescription() {
    return this.description;
  }

  public int getWeek() {
    return this.week;
  }

  public void setStocks(List<Stock> stocks) {
    this.stocks = stocks;
  }

  /**
   * Writes the current record to its file.
   */
  public void writeToFile() {
    try {
      StockFileService.writeStocks(this);
    } catch (IOException e) {
      System.err.println("Error writing stockFileRecord to file: " + e.getMessage());
    }
  }
}
