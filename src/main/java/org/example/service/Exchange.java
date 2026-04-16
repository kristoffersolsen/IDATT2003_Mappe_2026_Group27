package org.example.service;

import org.example.model.Player;
import org.example.model.Share;
import org.example.model.Status;
import org.example.model.Stock;
import org.example.model.StockFileRecord;
import org.example.model.transaction.Purchase;
import org.example.model.transaction.Sale;
import org.example.model.transaction.Transaction;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The exchange.
 */
public class Exchange {

  private final String name;
  private int week;
  private final Map<String, Stock> stockMap;
  private final StockFileRecord stockFileRecord;

  private final Random random;

  private final double LOWER_CHANGE = -0.1; // -10%
  private final double UPPER_CHANGE = 0.1;  // +10%

  /**
   * Constructor with just a name. Creates an empty stockMap and starts from week 1.
   *
   * @param name name of exchange
   */
  public Exchange(String name) {
    this.name = name;
    this.stockMap = new HashMap<>();
    this.week = 1;
    this.random = new Random();
    this.stockFileRecord = null;
  }

  /**
   * Constructor with week number and list of stocks.
   *
   * @param name   name of exchange
   * @param week   week number
   * @param stocks list of stocks to use
   */
  public Exchange(String name, int week, List<Stock> stocks) {
    this.name = name;
    this.stockMap = new HashMap<>();
    for (Stock stock : stocks) {
      this.stockMap.put(stock.getSymbol(), stock);
    }
    this.week = week;
    this.random = new Random();
    this.stockFileRecord = null;
  }

  /**
   * Constructor from a StockFileRecord. Used when starting from a file.
   *
   * @param name            name of exchange
   * @param stockFileRecord the stockFileRecord to use
   */
  public Exchange(String name, StockFileRecord stockFileRecord) {
    this.name = name;
    this.stockMap = new HashMap<>();
    for (Stock stock : stockFileRecord.getStocks()) {
      this.stockMap.put(stock.getSymbol(), stock);
    }
    this.week = stockFileRecord.getWeek();
    this.random = new Random();
    this.stockFileRecord = stockFileRecord;
  }

  public String getName() {
    return name;
  }

  public int getWeek() {
    return week;
  }

  /**
   * Checks if the Exchange has a stock in the stockMap.
   *
   * @param symbol symbol of stock to check for
   * @return true if found, else false
   */
  public boolean hasStock(String symbol) {
    return stockMap.containsKey(symbol);
  }

  /**
   * Returns the stock from the stockMap if found.
   *
   * @param symbol symbol of stock to check
   * @return the Stock
   */
  public Stock getStock(String symbol) {
    if (!hasStock(symbol)) {
      throw new IllegalArgumentException("Stock with symbol " + symbol + " does not exist.");
    }
    return stockMap.get(symbol);
  }

  /**
   * Returns all stocks currently listed on the exchange.
   *
   * @return unmodifiable list of all stocks
   */
  public List<Stock> getStocks() {
    return List.copyOf(stockMap.values());
  }

  public StockFileRecord getStockFileRecord() {
    return stockFileRecord;
  }

  /**
   * Find stocks based on a search term. searches through company names.
   *
   * @param searchTerm a part of a company name
   * @return list of Stocks
   */
  public List<Stock> findStocks(String searchTerm) {
    return stockMap.values().stream()
        .filter(stock -> stock.getCompany().toLowerCase().contains(searchTerm.toLowerCase()))
        .toList();
  }

  /**
   * Perform a buy transaction
   *
   * @param symbol   The symbol of the Stock to buy
   * @param quantity The quantity of the Stock to buy
   * @param player   The player that buys
   * @return The transaction
   */
  public Transaction buy(String symbol, BigDecimal quantity, Player player) {
    if (!hasStock(symbol)) {
      throw new IllegalArgumentException("Stock with symbol " + symbol + " does not exist.");
    }
    Stock stock = getStock(symbol);
    Share share = new Share(stock, quantity, stock.getSalesPrice());
    Transaction transaction = new Purchase(share, getWeek());
    transaction.commit(player);
    return transaction;
  }

  /**
   * Peform a sell transaction.
   *
   * @param symbol   The symbol of the Stock to sell
   * @param quantity The quantity of the Stock to sell
   * @param player   The player that sells
   * @return The transaction
   */
  public Transaction sell(String symbol, BigDecimal quantity, Player player) {
    if (!hasStock(symbol)) {
      throw new IllegalArgumentException("Stock with symbol " + symbol + " does not exist.");
    }
    Stock stock = getStock(symbol);
    Share share = new Share(stock, quantity, stock.getSalesPrice());
    Transaction transaction = new Sale(share, getWeek());
    transaction.commit(player);
    return transaction;
  }

  /**
   * Advance to the next week. Adds a randomly changed new price to all Stocks.
   */
  public void advance() {
    for (Stock stock : stockMap.values()) {
      BigDecimal newPrice =
          stock.getSalesPrice()
              .multiply(BigDecimal.valueOf(1.0 + random.nextDouble(LOWER_CHANGE, UPPER_CHANGE)));
      stock.addNewSalesPrice(newPrice);
    }
    week++;
  }

  /**
   * Retrieves the top gainers, sorted on the latest price change.
   *
   * @param limit How many gainers to list
   * @return list of Stocks
   */
  public List<Stock> getGainers(int limit) {
    return this.stockMap.values().stream()
        .filter(stock -> stock.getSalesPrice().signum() > 0)
        .sorted(Comparator.comparing(Stock::getLatestPriceChange).reversed())
        .limit(limit)
        .toList();
  }

  /**
   * Retrieves the bottom losers, reverse sorted on the latest price change.
   *
   * @param limit How many losers to list
   * @return list of Stocks
   */
  public List<Stock> getLosers(int limit) {
    return this.stockMap.values().stream()
        .filter(stock -> stock.getLatestPriceChange() != null &&
            stock.getLatestPriceChange().signum() < 0)
        .sorted(Comparator.comparing(Stock::getLatestPriceChange))
        .limit(limit)
        .toList();
  }

  /**
   * Saves the state of the exchange to a file. Requires a StockFileService.
   */
  public void saveState() {
    if (stockFileRecord != null) {
      this.stockFileRecord.setStocks((List<Stock>) this.stockMap.values());
      this.stockFileRecord.writeToFile();
    } else {
      System.err.println("No StockFileRecord saved for Exchange: " + this.name);
    }
  }
}

