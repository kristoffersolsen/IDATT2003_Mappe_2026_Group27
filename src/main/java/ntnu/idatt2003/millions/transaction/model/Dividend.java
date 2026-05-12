package ntnu.idatt2003.millions.transaction.model;

import java.math.BigDecimal;
import ntnu.idatt2003.millions.player.model.Player;
import ntnu.idatt2003.millions.player.model.Share;

/**
 * A dividend payment credited to a player for shares they hold.
 *
 * <p>The {@link Share} passed to the constructor carries the dividend-paying
 * stock, the quantity held at the time of payment, and the per-share dividend
 * rate in the {@link Share#purchasePrice()} field.
 *
 * <p>Dividend transactions are created and committed by DividendService;
 * the {@link #commit} method is provided for direct test use only.
 */
public final class Dividend extends Transaction {

  /**
   * Constructs a dividend transaction.
   *
   * @param share the share whose {@code purchasePrice} is the per-share
   *              dividend rate and whose {@code quantity} is the shares held
   * @param tick  the simulation tick at the time of payment
   */
  public Dividend(Share share, long tick) {
    super(share, tick, new DividendCalculator(share));
  }

  /**
   * Returns the per-share dividend rate paid in this transaction.
   *
   * @return dividend per share
   */
  public BigDecimal getDividendPerShare() {
    return share.purchasePrice();
  }

  /**
   * Returns the total cash amount credited to the player.
   *
   * @return total dividend paid
   */
  public BigDecimal getTotalPaid() {
    return calculator.calculateTotal();
  }

  /**
   * Credits the dividend to the player and records it in the archive.
   *
   * <p>Throws if this dividend has already been committed.
   *
   * @param player the player receiving the dividend
   * @throws IllegalArgumentException if already committed
   */
  @Override
  public void commit(Player player) {
    if (committed) {
      throw new IllegalArgumentException("Dividend has already been applied.");
    }
    player.addMoney(calculator.calculateTotal());
    player.getTransactionArchive().add(this);
    committed = true;
  }
}
