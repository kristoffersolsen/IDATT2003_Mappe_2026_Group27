package org.example.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.example.config.Difficulty;
import org.example.config.GameDefaults;
import org.example.config.GameSettings;
import org.example.model.Exchange;
import org.example.model.Player;
import org.example.model.Portfolio;
import org.example.model.Share;
import org.example.model.Stock;
import org.example.model.StockFileRecord;
import org.example.model.market.MarketContext;
import org.example.model.market.PriceModel;
import org.example.model.observer.GameEvent;
import org.example.model.transaction.Transaction;
import org.example.model.transaction.TransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that operates on an {@link Exchange}.
 *
 * <p>Encapsulates all business logic for trading, week advancement,
 * stock queries, and file persistence. The {@link Exchange} model class
 * itself holds only state.
 */
public class ExchangeService {

  private static final Logger log = LoggerFactory.getLogger(ExchangeService.class);

  private static final long TESTING_SEED = 0L;

  private final Exchange exchange;
  private final PriceModel priceModel;
  private final MarketContext marketContext;
  private StockFileRecord stockFileRecord;

  /**
   * Package-private constructor used by the public factory methods.
   *
   * @param exchange      the exchange to operate on
   * @param priceModel    the price model to use for simulation
   * @param marketContext the market context providing settings and random source
   */
  ExchangeService(Exchange exchange, PriceModel priceModel, MarketContext marketContext) {
    this.exchange = exchange;
    this.priceModel = priceModel;
    this.marketContext = marketContext;
  }

  /**
   * Constructs a service from a {@link StockFileRecord}, building the
   * exchange from the file's stock data and metadata.
   *
   * <p>A default {@link PriceModel} is constructed automatically. The
   * {@link MarketContext} is seeded from {@link GameSettings#randomSeed()}.
   *
   * @param name            exchange name
   * @param stockFileRecord the file record to read stocks and week from
   * @param settings        game settings supplying volatility, drift bias, and seed
   */
  public ExchangeService(String name, StockFileRecord stockFileRecord, GameSettings settings) {
    this(new Exchange(name,
            stockFileRecord.getWeek() == -1 ? 1 : stockFileRecord.getWeek(),
            stockFileRecord.getStocks()),
        new PriceModel(),
        new MarketContext(settings, new Random(settings.randomSeed()), 0.0, 0.0));
    this.stockFileRecord = stockFileRecord;
  }

  /**
   * Creates a service wrapping the given exchange, intended for test use.
   *
   * <p>Uses a default {@link PriceModel} and a deterministic
   * {@link MarketContext} seeded with {@code 0} and NORMAL difficulty
   * settings.
   *
   * @param exchange the exchange to operate on
   * @return a new service ready for testing
   */
  public static ExchangeService forTesting(Exchange exchange) {
    GameSettings settings = GameDefaults.forDifficulty(Difficulty.NORMAL);
    return new ExchangeService(exchange, new PriceModel(),
        new MarketContext(settings, new Random(TESTING_SEED), 0.0, 0.0));
  }

  /**
   * Returns the underlying {@link Exchange} model.
   *
   * @return the exchange model
   */
  public Exchange getExchange() {
    return exchange;
  }

  /**
   * Finds stocks whose symbol or company name contains the search term
   * (case-insensitive).
   *
   * @param searchTerm partial symbol or company name
   * @return matching stocks
   */
  public List<Stock> findStocks(String searchTerm) {
    String lower = searchTerm.toLowerCase(Locale.ROOT);
    return exchange.getStocks().stream()
        .filter(s -> s.getCompany().toLowerCase(Locale.ROOT).contains(lower)
            || s.getSymbol().toLowerCase(Locale.ROOT).contains(lower))
        .toList();
  }

  /**
   * Returns the top gainers sorted by latest price change, descending.
   *
   * @param limit maximum results
   * @return top gaining stocks
   */
  public List<Stock> getGainers(int limit) {
    return exchange.getStocks().stream()
        .filter(s -> s.getLatestPriceChange().signum() > 0)
        .sorted(Comparator.comparing(Stock::getLatestPriceChange).reversed())
        .limit(limit)
        .toList();
  }

  /**
   * Returns the worst losers sorted by latest price change, ascending.
   *
   * @param limit maximum results
   * @return worst losing stocks
   */
  public List<Stock> getLosers(int limit) {
    return exchange.getStocks().stream()
        .filter(s -> s.getLatestPriceChange().signum() < 0)
        .sorted(Comparator.comparing(Stock::getLatestPriceChange))
        .limit(limit)
        .toList();
  }

  /**
   * Executes a purchase of the given stock for the given player.
   *
   * @param symbol   stock symbol
   * @param quantity shares to buy
   * @param player   the buying player
   * @return the committed transaction
   * @throws IllegalArgumentException if symbol unknown or player cannot afford it
   */
  public Transaction buy(String symbol, BigDecimal quantity, Player player) {
    Stock stock = exchange.getStock(symbol);
    Share share = new Share(stock, quantity, stock.getSalesPrice());
    Transaction transaction = TransactionFactory.createPurchase(share, exchange.getWeek());
    transaction.commit(player);
    exchange.notifyObservers(GameEvent.STOCK_PURCHASED);
    return transaction;
  }

  /**
   * Executes a partial or full sale of a stock position for the given player.
   *
   * <p>The share used is retrieved from the player's portfolio so the stored
   * quantity and weighted-average purchase price are correct. A new
   * {@link Share} is constructed with {@code quantityToSell} for the
   * transaction, and {@link Portfolio#removeShare(Share, BigDecimal)} is
   * called to reduce (or remove) the position accordingly.
   *
   * @param symbol         stock symbol
   * @param quantityToSell number of shares to sell (1 .. held quantity)
   * @param player         the selling player
   * @return the committed transaction
   * @throws IllegalArgumentException if symbol unknown, player does not own the
   *                                  share, or quantityToSell exceeds held quantity
   */
  public Transaction sell(String symbol, BigDecimal quantityToSell, Player player) {
    if (!exchange.hasStock(symbol)) {
      throw new IllegalArgumentException("Stock with symbol " + symbol + " does not exist.");
    }

    Portfolio portfolio = player.getPortfolio();
    Share heldShare = portfolio.getShareBySymbol(symbol)
        .orElseThrow(() -> new IllegalArgumentException(
            "Player does not have this share in their portfolio."));

    // Build a share for exactly the quantity being sold, at the held avg price
    Share shareToSell = new Share(heldShare.stock(), quantityToSell, heldShare.purchasePrice());

    Transaction transaction = TransactionFactory.createSale(shareToSell, exchange.getWeek());

    // Manually commit: bypass Sale.commit()'s contains() check since shareToSell
    // is a new object. We own the pre-flight checks here in the service.
    player.addMoney(transaction.getCalculator().calculateTotal());
    portfolio.removeShare(heldShare, quantityToSell);
    player.getTransactionArchive().add(transaction);
    transaction.markCommitted();

    exchange.notifyObservers(GameEvent.STOCK_SOLD);
    return transaction;
  }

  /**
   * Advances the exchange by one week, updating every stock price via
   * {@link PriceModel}, then notifies observers.
   */
  public void advance() {
    for (Stock stock : exchange.getStocks()) {
      stock.addNewSalesPrice(priceModel.nextPrice(stock, marketContext));
    }
    exchange.incrementWeek();
    exchange.notifyObservers(GameEvent.WEEK_ADVANCED);
  }

  /**
   * Saves the current exchange state to its associated file, if one exists.
   */
  public void saveState() {
    if (stockFileRecord != null) {
      stockFileRecord.setStocks(exchange.getStocks());
      try {
        StockFileService.writeStocks(stockFileRecord);
      } catch (IOException e) {
        log.error("Failed to write stock file record", e);
      }
    } else {
      log.warn("No StockFileRecord associated with this ExchangeService");
    }
  }
}
