package ntnu.idatt2003.millions.model;

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
  private BigDecimal priceAtSkipStart;
  private final BigDecimal dividendPerShare;
  private final int dividendIntervalHours;

  /**
   * Constructs a non-dividend-paying stock.
   *
   * @param symbol     ticker symbol representing the company
   * @param company    full company name
   * @param salesPrice initial sales price
   */
  public Stock(String symbol, String company, BigDecimal salesPrice) {
    this(symbol, company, salesPrice, BigDecimal.ZERO, 0);
  }

  /**
   * Constructs a stock with optional dividend data.
   *
   * <p>A stock pays dividends when both {@code dividendPerShare} is positive
   * and {@code dividendIntervalHours} is positive.
   *
   * @param symbol                the ticker symbol
   * @param company               the full company name
   * @param salesPrice            the initial sales price
   * @param dividendPerShare      the per-share dividend amount, or zero if non-paying
   * @param dividendIntervalHours the payment interval in hours, or zero if non-paying
   */
  public Stock(String symbol, String company, BigDecimal salesPrice,
      BigDecimal dividendPerShare, int dividendIntervalHours) {
    this.symbol = symbol;
    this.company = company;
    this.prices.add(salesPrice);
    this.dividendPerShare = dividendPerShare;
    this.dividendIntervalHours = dividendIntervalHours;
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
   * Snapshots the current sales price as the baseline for the next skip.
   *
   * <p>Called by {@link ntnu.idatt2003.millions.model.time.GameClock} before
   * the tick loop begins so that {@link #getSkipPriceChange()} reflects the
   * full change over the skip rather than just the last tick.
   */
  public void markSkipStart() {
    this.priceAtSkipStart = getSalesPrice();
  }

  /**
   * Returns the change in price since the last call to {@link #markSkipStart()}.
   *
   * <p>Returns {@link BigDecimal#ZERO} if {@link #markSkipStart()} has never been called.
   *
   * @return price change over the most recent skip
   */
  public BigDecimal getSkipPriceChange() {
    if (priceAtSkipStart == null) {
      return BigDecimal.ZERO;
    }
    return getSalesPrice().subtract(priceAtSkipStart);
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
   * Returns whether this stock pays periodic dividends.
   *
   * @return true if both {@code dividendPerShare} and {@code dividendIntervalHours}
   *         are positive
   */
  public boolean paysDividend() {
    return dividendPerShare != null
        && dividendPerShare.signum() > 0
        && dividendIntervalHours > 0;
  }

  /**
   * Returns the per-share dividend amount, or {@link BigDecimal#ZERO} if non-paying.
   *
   * @return dividend per share
   */
  public BigDecimal getDividendPerShare() {
    return dividendPerShare == null ? BigDecimal.ZERO : dividendPerShare;
  }

  /**
   * Returns the dividend payment interval in simulated hours, or {@code 0} if non-paying.
   *
   * @return dividend interval in hours
   */
  public int getDividendIntervalHours() {
    return dividendIntervalHours;
  }

  /**
   * Converts this stock to a string array for CSV serialisation.
   *
   * <p>Returns five elements: symbol, company, current price, dividend per share,
   * and dividend interval. Non-paying stocks use {@code "0"} for the last two.
   *
   * @return stock fields as a string array
   */
  public String[] toStringList() {
    return new String[] {
        this.getSymbol(),
        this.getCompany(),
        this.getSalesPrice().toString(),
        getDividendPerShare().toPlainString(),
        String.valueOf(dividendIntervalHours)
    };
  }
}
