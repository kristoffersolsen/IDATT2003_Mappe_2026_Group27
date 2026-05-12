package ntnu.idatt2003.millions.model.transaction;

import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.Share;
import ntnu.idatt2003.millions.service.ExchangeService;

/**
 * Abstract base for all transactions.
 *
 * <p>Sealed so that future transaction types must be explicitly permitted here.
 * Currently permits {@link Purchase}, {@link Sale}, and {@link Dividend}.
 */
public abstract sealed class Transaction permits Purchase, Sale, Dividend {

  /**
   * Number of simulation ticks in one trading week (8 h/day × 5 days/week).
   *
   * <p>Used by {@link #getWeek()} to derive a display week from the stored tick.
   */
  private static final int TICKS_PER_WEEK = 40;

  protected Share share;
  protected long tick;
  protected TransactionCalculator calculator;
  protected boolean committed = false;

  /**
   * Constructor.
   *
   * @param share      the share that is transacted
   * @param tick       the simulation tick at the time of the transaction
   * @param calculator the calculator to use
   */
  Transaction(Share share, long tick, TransactionCalculator calculator) {
    this.share = share;
    this.tick = tick;
    this.calculator = calculator;
  }

  public Share getShare() {
    return share;
  }

  /**
   * Returns the simulation tick at which this transaction was created.
   *
   * @return the tick
   */
  public long getTick() {
    return tick;
  }

  /**
   * Returns the trading week derived from the stored tick for display purposes.
   *
   * <p>Computed as {@code tick / TICKS_PER_WEEK + 1} so that tick 0 is week 1.
   *
   * @return the derived week number (1-based)
   */
  public int getWeek() {
    return (int) (tick / TICKS_PER_WEEK) + 1;
  }

  public TransactionCalculator getCalculator() {
    return calculator;
  }

  public boolean isCommitted() {
    return committed;
  }

  /**
   * Checks that the transaction is valid and commits it.
   *
   * @param player the player to perform a transaction on
   */
  public abstract void commit(Player player);

  /**
   * Marks this transaction as committed.
   *
   * <p>Called by {@link ExchangeService} when it manually
   * applies the transaction side-effects instead of delegating to {@link #commit}.
   */
  public void markCommitted() {
    this.committed = true;
  }
}
