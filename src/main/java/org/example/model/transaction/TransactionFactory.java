package org.example.model.transaction;

import org.example.model.Share;

/**
 * Factory for creating {@link Transaction} instances.
 *
 * <p>Centralises transaction construction so that callers (e.g. {@code Exchange})
 * are decoupled from the concrete {@link Purchase} and {@link Sale} classes.
 * Adding a new transaction type only requires a new factory method here,
 * with no changes needed elsewhere.
 *
 * <p>This is a static utility class and cannot be instantiated.
 */
public final class TransactionFactory {

  private TransactionFactory() {}

  /**
   * Creates a new {@link Purchase} transaction.
   *
   * @param share the share being purchased
   * @param week  the current game week
   * @return a new, uncommitted {@link Purchase}
   */
  public static Purchase createPurchase(Share share, int week) {
    return new Purchase(share, week);
  }

  /**
   * Creates a new {@link Sale} transaction.
   *
   * @param share the share being sold
   * @param week  the current game week
   * @return a new, uncommitted {@link Sale}
   */
  public static Sale createSale(Share share, int week) {
    return new Sale(share, week);
  }
}