package ntnu.idatt2003.millions.market.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import ntnu.idatt2003.millions.shared.config.Difficulty;
import ntnu.idatt2003.millions.shared.config.GameDefaults;
import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.market.model.Exchange;
import ntnu.idatt2003.millions.player.model.Player;
import ntnu.idatt2003.millions.player.model.Portfolio;
import ntnu.idatt2003.millions.player.model.Share;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.market.model.StockFileRecord;
import ntnu.idatt2003.millions.market.model.MarketContext;
import ntnu.idatt2003.millions.market.model.PriceModel;
import ntnu.idatt2003.millions.shared.observer.GameEvent;
import ntnu.idatt2003.millions.transaction.model.Transaction;
import ntnu.idatt2003.millions.transaction.model.TransactionFactory;
import ntnu.idatt2003.millions.event.service.EventService;
import ntnu.idatt2003.millions.order.service.OrderService;
import ntnu.idatt2003.millions.transaction.service.DividendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that operates on an {@link Exchange}.
 *
 * <p>Encapsulates all business logic for trading, tick advancement,
 * stock queries, and file persistence. The {@link Exchange} model class
 * itself holds only state.
 *
 * <p>TODO(team): split simulation logic into a dedicated SimulationService
 * when AI traders are introduced in version 4.0.
 */
public class ExchangeService {

  private static final Logger log = LoggerFactory.getLogger(ExchangeService.class);

  private static final long TESTING_SEED = 0L;

  private final Exchange exchange;
  private final PriceModel priceModel;
  private final GameSettings settings;
  private final Random marketRandom;
  private final OrderService orderService;
  private final DividendService dividendService;
  private final EventService eventService;
  private StockFileRecord stockFileRecord;

  /**
   * Package-private constructor used by the public factory and test entry points.
   *
   * @param exchange        the exchange to operate on
   * @param priceModel      the price model to use for simulation
   * @param settings        game settings supplying volatility, drift bias, and seed
   * @param marketRandom    the seeded random source shared across all per-tick contexts
   * @param orderService    limit-order evaluator, may be {@code null} in tests
   * @param dividendService dividend payer, may be {@code null} in tests
   * @param eventService    event scheduler, may be {@code null} in tests
   */
  ExchangeService(Exchange exchange, PriceModel priceModel, GameSettings settings,
      Random marketRandom, OrderService orderService, DividendService dividendService,
      EventService eventService) {
    this.exchange = exchange;
    this.priceModel = priceModel;
    this.settings = settings;
    this.marketRandom = marketRandom;
    this.orderService = orderService;
    this.dividendService = dividendService;
    this.eventService = eventService;
  }

  /**
   * Constructs a fully-wired service for a game session.
   *
   * <p>A default {@link PriceModel} is constructed automatically. The random
   * source is seeded from {@link GameSettings#randomSeed()}.
   *
   * @param exchange        the pre-built exchange model
   * @param stockFileRecord the file record associated with this exchange (for save)
   * @param settings        game settings supplying volatility, drift bias, and seed
   * @param orderService    the limit-order service to evaluate each tick
   * @param dividendService the dividend service to run each tick
   * @param eventService    the event service to tick before price updates
   */
  public ExchangeService(Exchange exchange, StockFileRecord stockFileRecord,
      GameSettings settings, OrderService orderService, DividendService dividendService,
      EventService eventService) {
    this(exchange, new PriceModel(), settings, new Random(settings.randomSeed()),
        orderService, dividendService, eventService);
    this.stockFileRecord = stockFileRecord;
  }

  /**
   * Creates a service wrapping the given exchange, intended for test use.
   *
   * <p>Uses a default {@link PriceModel}, NORMAL difficulty settings, and a
   * deterministic random source seeded with {@code 0}.
   *
   * @param exchange the exchange to operate on
   * @return a new service ready for testing
   */
  public static ExchangeService forTesting(Exchange exchange) {
    GameSettings settings = GameDefaults.forDifficulty(Difficulty.NORMAL);
    return new ExchangeService(exchange, new PriceModel(), settings, new Random(TESTING_SEED),
        null, null, null);
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
   * Returns the top gainers by skip price change, descending.
   *
   * <p>Uses {@link Stock#getSkipPriceChange()} so the sidebar reflects the
   * full movement across the most recent skip rather than just the last tick.
   *
   * @param limit maximum results
   * @return top gaining stocks
   */
  public List<Stock> getGainers(int limit) {
    return exchange.getStocks().stream()
        .filter(s -> s.getSkipPriceChange().signum() > 0)
        .sorted(Comparator.comparing(Stock::getSkipPriceChange).reversed())
        .limit(limit)
        .toList();
  }

  /**
   * Returns the worst losers by skip price change, ascending.
   *
   * <p>Uses {@link Stock#getSkipPriceChange()} so the sidebar reflects the
   * full movement across the most recent skip rather than just the last tick.
   *
   * @param limit maximum results
   * @return worst losing stocks
   */
  public List<Stock> getLosers(int limit) {
    return exchange.getStocks().stream()
        .filter(s -> s.getSkipPriceChange().signum() < 0)
        .sorted(Comparator.comparing(Stock::getSkipPriceChange))
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
    Transaction transaction = TransactionFactory.createPurchase(share, exchange.getTickCount());
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

    Share shareToSell = new Share(heldShare.stock(), quantityToSell, heldShare.purchasePrice());

    Transaction transaction = TransactionFactory.createSale(shareToSell, exchange.getTickCount());

    player.addMoney(transaction.getCalculator().calculateTotal());
    portfolio.removeShare(heldShare, quantityToSell);
    player.getTransactionArchive().add(transaction);
    transaction.markCommitted();

    exchange.notifyObservers(GameEvent.STOCK_SOLD);
    return transaction;
  }

  /**
   * Advances the exchange by one simulated hour.
   *
   * <p>Processing order per tick:
   * <ol>
   *   <li>Update every stock price via {@link PriceModel}.</li>
   *   <li>Increment the tick counter.</li>
   *   <li>Evaluate pending limit orders via {@link OrderService}.</li>
   *   <li>Pay due dividends via {@link DividendService}.</li>
   *   <li>Notify observers with {@link GameEvent#HOUR_ADVANCED}.</li>
   * </ol>
   *
   * <p>Called by {@link ntnu.idatt2003.millions.market.service.GameClock} once per
   * hour in a skip loop.
   */
  public void tick() {
    if (eventService != null) {
      eventService.tick(exchange.getTickCount(), settings, marketRandom);
    }
    for (Stock stock : exchange.getStocks()) {
      double modifier = eventService != null ? eventService.modifierFor(stock) : 0.0;
      MarketContext context = new MarketContext(settings, marketRandom, 0.0, modifier);
      stock.addNewSalesPrice(priceModel.nextPrice(stock, context));
    }
    exchange.incrementTick();
    if (orderService != null) {
      orderService.evaluateOrders(exchange.getTickCount());
    }
    if (dividendService != null) {
      dividendService.payDividends(exchange.getTickCount());
    }
    exchange.notifyObservers(GameEvent.HOUR_ADVANCED);
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
