package ntnu.idatt2003.millions.model.transaction;

import ntnu.idatt2003.millions.model.Share;

/**
 * Factory for creating {@link Transaction} instances.
 *
 * <p>Centralises transaction construction so that callers (e.g. {@code ExchangeService})
 * are decoupled from the concrete {@link Purchase} and {@link Sale} classes.
 * Adding a new transaction type only requires a new factory method here,
 * with no changes needed elsewhere.
 *
 * <p>This is a static utility class and cannot be instantiated.
 */
public final class TransactionFactory {

  private TransactionFactory() {
  }

  /**
   * Creates a new {@link Purchase} transaction.
   *
   * @param share the share being purchased
   * @param tick  the current simulation tick
   * @return a new, uncommitted {@link Purchase}
   */
  public static Purchase createPurchase(Share share, long tick) {
    return new Purchase(share, tick);
  }

  /**
   * Creates a new {@link Sale} transaction.
   *
   * @param share the share being sold
   * @param tick  the current simulation tick
   * @return a new, uncommitted {@link Sale}
   */
  public static Sale createSale(Share share, long tick) {
    return new Sale(share, tick);
  }

  /**
   * Creates a new {@link Dividend} transaction.
   *
   * @param share the share encoding the dividend rate in its purchase-price field
   * @param tick  the current simulation tick
   * @return a new, uncommitted {@link Dividend}
   */
  public static Dividend createDividend(Share share, long tick) {
    return new Dividend(share, tick);
  }
}
