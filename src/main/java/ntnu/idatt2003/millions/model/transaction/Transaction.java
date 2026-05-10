package ntnu.idatt2003.millions.model.transaction;

import ntnu.idatt2003.millions.service.ExchangeService;
import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.Share;

/**
 * Abstract class for all transactions
 */
public abstract class Transaction {
  protected Share share;
  protected int week;
  protected TransactionCalculator calculator;
  protected boolean committed = false;

  /**
   * Constructor.
   *
   * @param share      The share that is transacted
   * @param week       The week of the transaction
   * @param calculator The calculator to use
   */
  Transaction(Share share, int week, TransactionCalculator calculator) {
    this.share = share;
    this.week = week;
    this.calculator = calculator;
  }

  public Share getShare() {
    return share;
  }

  public int getWeek() {
    return week;
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
