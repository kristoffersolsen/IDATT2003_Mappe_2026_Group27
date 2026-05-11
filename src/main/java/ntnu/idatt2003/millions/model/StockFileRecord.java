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
  private final long tick;

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
    this.tick = -1L;
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
    this.tick = -1L;
  }

  /**
   * Constructor with description and tick count.
   *
   * @param stocks      stocks to read/write
   * @param fileName    file
   * @param description description
   * @param tick        simulation tick count
   */
  public StockFileRecord(List<Stock> stocks, File fileName, String description, long tick) {
    this.stocks = stocks;
    this.fileName = fileName;
    this.description = description;
    this.tick = tick;
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

  /**
   * Returns the simulation tick count stored in this record, or {@code -1} if absent.
   *
   * @return the tick count, or -1 if not set
   */
  public long getTick() {
    return this.tick;
  }

  public void setStocks(List<Stock> stocks) {
    this.stocks = stocks;
  }
}
