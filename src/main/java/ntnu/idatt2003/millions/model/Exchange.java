package ntnu.idatt2003.millions.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ntnu.idatt2003.millions.model.observer.Observable;
import ntnu.idatt2003.millions.service.ExchangeService;

/**
 * Represents a stock exchange — its name, current tick count, and listed stocks.
 *
 * <p>This is a pure model class: it holds state only and contains no
 * business logic. All operations (trading, advancing the clock, file I/O)
 * are delegated to {@link ExchangeService}.
 *
 * <p>Extends {@link Observable} so views can react to state
 * changes without being manually refreshed by controllers.
 */
public class Exchange extends Observable {

  private final String name;
  private long tickCount;
  private final Map<String, Stock> stockMap;

  /**
   * Constructs an exchange with a name, starting tick count, and list of stocks.
   *
   * @param name      the exchange name
   * @param tickCount the starting tick count
   * @param stocks    the stocks listed on this exchange
   */
  public Exchange(String name, long tickCount, List<Stock> stocks) {
    this.name = name;
    this.tickCount = tickCount;
    this.stockMap = new HashMap<>();
    for (Stock stock : stocks) {
      this.stockMap.put(stock.getSymbol(), stock);
    }
  }

  public String getName() {
    return name;
  }

  /**
   * Returns the current simulation tick count.
   *
   * @return the tick count (hours elapsed since game start)
   */
  public long getTickCount() {
    return tickCount;
  }

  /**
   * Increments the tick counter by one. Called by {@link ExchangeService}
   * after updating all stock prices for one simulated hour.
   */
  public void incrementTick() {
    this.tickCount++;
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
    return Collections.unmodifiableList(new ArrayList<>(stockMap.values()));
  }
}
