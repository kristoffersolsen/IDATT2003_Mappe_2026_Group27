package org.example.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.example.model.Exchange;
import org.example.model.Player;
import org.example.model.Portfolio;
import org.example.model.Share;
import org.example.model.Stock;
import org.example.model.StockFileRecord;
import org.example.model.observer.GameEvent;
import org.example.model.transaction.Transaction;
import org.example.model.transaction.TransactionFactory;

/**
 * Service that operates on an {@link Exchange}.
 *
 * <p>Encapsulates all business logic for trading, week advancement,
 * stock queries, and file persistence. The {@link Exchange} model class
 * itself holds only state.
 */
public class ExchangeService {

  private static final double LOWER_CHANGE = -0.1;
  private static final double UPPER_CHANGE = 0.1;

  private final Exchange exchange;
  private final Random random;
  private StockFileRecord stockFileRecord;

  /**
   * Constructs a service wrapping the given exchange.
   *
   * @param exchange the exchange to operate on
   */
  public ExchangeService(Exchange exchange) {
    this.exchange = exchange;
    this.random = new Random();
  }

  /**
   * Constructs a service from a {@link StockFileRecord}, building the
   * exchange from the file's stock data and metadata.
   *
   * @param name            exchange name
   * @param stockFileRecord the file record to read stocks and week from
   */
  public ExchangeService(String name, StockFileRecord stockFileRecord) {
    int startWeek = stockFileRecord.getWeek() == -1 ? 1 : stockFileRecord.getWeek();
    this.exchange = new Exchange(name, startWeek, stockFileRecord.getStocks());
    this.stockFileRecord = stockFileRecord;
    this.random = new Random();
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
    String lower = searchTerm.toLowerCase();
    return exchange.getStocks().stream()
        .filter(s -> s.getCompany().toLowerCase().contains(lower)
            || s.getSymbol().toLowerCase().contains(lower))
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

    exchange.notifyObservers(GameEvent.STOCK_SOLD);
    return transaction;
  }

  /**
   * Advances the exchange by one week, applying a random ±10% price
   * change to every stock, then notifies observers.
   */
  public void advance() {
    for (Stock stock : exchange.getStocks()) {
      BigDecimal newPrice = stock.getSalesPrice()
          .multiply(BigDecimal.valueOf(1.0 + random.nextDouble(LOWER_CHANGE, UPPER_CHANGE)));
      stock.addNewSalesPrice(newPrice);
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
      stockFileRecord.writeToFile();
    } else {
      System.err.println("No StockFileRecord associated with this ExchangeService.");
    }
  }
}