package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stock in a company. A symbol can, for example be "AAPL" for the company Apple.
 */
public class Stock {

  private static final Logger log = LoggerFactory.getLogger(Stock.class);

  private final String symbol;
  private final String company;
  private final List<BigDecimal> prices = new ArrayList<>();

  /**
   * Default stock constructor.
   *
   * @param symbol     set of letters representing the company name
   * @param company    the name of the company
   * @param salesPrice last sales price of the stock
   */
  public Stock(String symbol, String company, BigDecimal salesPrice) {
    this.symbol = symbol;
    this.company = company;
    this.prices.add(salesPrice);
  }

  public String getSymbol() {
    return this.symbol;
  }

  public String getCompany() {
    return this.company;
  }

  public BigDecimal getSalesPrice() {
    return this.prices.get(prices.size() - 1);
  }

  /**
   * Adds a new sales price to the price history.
   *
   * @param price price to add
   */
  public void addNewSalesPrice(BigDecimal price) {
    this.prices.add(price);
  }

  /**
   * Returns an unmodifiable view of all historical prices.
   *
   * @return list of prices, oldest first
   */
  public List<BigDecimal> getHistoricalPrices() {
    return Collections.unmodifiableList(prices);
  }

  /**
   * Returns the highest historical price.
   *
   * @return the highest price
   */
  public BigDecimal getHighestPrice() {
    return prices.stream().max(BigDecimal::compareTo).orElseThrow();
  }

  /**
   * Returns the lowest historical price.
   *
   * @return the lowest price
   */
  public BigDecimal getLowestPrice() {
    return prices.stream().min(BigDecimal::compareTo).orElseThrow();
  }

  /**
   * Returns the difference between the latest and second-latest prices.
   *
   * <p>Returns {@link BigDecimal#ZERO} when fewer than two prices exist.
   *
   * @return the latest price change
   */
  public BigDecimal getLatestPriceChange() {
    if (this.prices.size() < 2) {
      log.debug("Not enough prices for comparison in stock: {}", symbol);
      return BigDecimal.ZERO;
    }
    BigDecimal latestPrice = this.prices.get(this.prices.size() - 1);
    BigDecimal nextLatestPrice = this.prices.get(this.prices.size() - 2);
    return latestPrice.subtract(nextLatestPrice);
  }

  /**
   * Converts parameters to a string array. Used in file handling.
   *
   * @return stock parameters as a string array
   */
  public String[] toStringList() {
    return new String[] {
        this.getSymbol(),
        this.getCompany(),
        this.getSalesPrice().toString()
    };
  }
}
