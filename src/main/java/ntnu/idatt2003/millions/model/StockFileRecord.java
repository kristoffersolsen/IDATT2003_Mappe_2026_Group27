package ntnu.idatt2003.millions.model;

import java.io.File;
import java.util.List;

/**
 * Holds information about a read/write of stocks from a file.
 */
public class StockFileRecord {

  private List<Stock> stocks;
  private final File fileName;
  private final String description;
  private final int week;

  /**
   * Constructor with only list of stocks and filename.
   *
   * @param stocks   stocks to read/write
   * @param fileName the file
   */
  public StockFileRecord(List<Stock> stocks, File fileName) {
    this.stocks = stocks;
    this.fileName = fileName;
    this.description = null;
    this.week = -1;
  }

  /**
   * Constructor with description.
   *
   * @param stocks      stocks to read/write
   * @param fileName    file
   * @param description description
   */
  public StockFileRecord(List<Stock> stocks, File fileName, String description) {
    this.stocks = stocks;
    this.fileName = fileName;
    this.description = description;
    this.week = -1;
  }

  /**
   * Constructor with description and week number.
   *
   * @param stocks      stocks to read/write
   * @param fileName    file
   * @param description description
   * @param week        week number
   */
  public StockFileRecord(List<Stock> stocks, File fileName, String description, int week) {
    this.stocks = stocks;
    this.fileName = fileName;
    this.description = description;
    this.week = week;
  }

  public List<Stock> getStocks() {
    return stocks;
  }

  public File getFileName() {
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
}
