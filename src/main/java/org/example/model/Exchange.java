package org.example.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.model.observer.Observable;

/**
 * Represents a stock exchange — its name, current week, and listed stocks.
 *
 * <p>This is a pure model class: it holds state only and contains no
 * business logic. All operations (trading, advancing the week, file I/O)
 * are delegated to {@link org.example.service.ExchangeService}.
 *
 * <p>Extends {@link Observable} so views can react to state
 * changes without being manually refreshed by controllers.
 */
public class Exchange extends Observable {

  private final String name;
  private int week;
  private final Map<String, Stock> stockMap;

  /**
   * Constructs an exchange with a name, starting week, and list of stocks.
   *
   * @param name   the exchange name
   * @param week   the starting week number
   * @param stocks the stocks listed on this exchange
   */
  public Exchange(String name, int week, List<Stock> stocks) {
    this.name = name;
    this.week = week;
    this.stockMap = new HashMap<>();
    for (Stock stock : stocks) {
      this.stockMap.put(stock.getSymbol(), stock);
    }
  }

  public String getName() {
    return name;
  }

  public int getWeek() {
    return week;
  }

  /**
   * Increments the week counter. Called by {@link org.example.service.ExchangeService}
   * after updating all stock prices.
   */
  public void incrementWeek() {
    this.week++;
  }

  /**
   * Returns whether a stock with the given symbol is listed.
   *
   * @param symbol the stock symbol
   * @return true if listed
   */
  public boolean hasStock(String symbol) {
    return stockMap.containsKey(symbol);
  }

  /**
   * Returns the stock for the given symbol.
   *
   * @param symbol the stock symbol
   * @return the stock
   * @throws IllegalArgumentException if the symbol is not listed
   */
  public Stock getStock(String symbol) {
    if (!hasStock(symbol)) {
      throw new IllegalArgumentException("Stock with symbol " + symbol + " does not exist.");
    }
    return stockMap.get(symbol);
  }

  /**
   * Returns an unmodifiable view of all listed stocks.
   *
   * @return all stocks
   */
  public List<Stock> getStocks() {
    return List.copyOf(stockMap.values());
  }
}