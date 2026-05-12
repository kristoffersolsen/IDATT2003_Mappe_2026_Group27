package ntnu.idatt2003.millions.model.transaction;

import java.math.BigDecimal;
import ntnu.idatt2003.millions.model.Share;

/**
 * Calculator for a dividend payment.
 *
 * <p>The dividend amount is {@code dividendPerShare × quantity}. No
 * commission or tax is applied to dividend income.
 *
 * <p>The {@code dividendPerShare} rate is stored in {@link Share#purchasePrice()}
 * by the {@link Dividend} constructor.
 */
public class DividendCalculator implements TransactionCalculator {

  private final BigDecimal dividendPerShare;
  private final BigDecimal quantity;

  /**
   * Constructs a calculator from the given share.
   *
   * @param share the share whose {@code purchasePrice} holds the dividend rate
   *              and whose {@code quantity} is the number of shares held
   */
  public DividendCalculator(Share share) {
    this.dividendPerShare = share.purchasePrice();
    this.quantity = share.quantity();
  }

  /**
   * Returns the gross dividend: {@code dividendPerShare × quantity}.
   *
   * @return gross dividend amount
   */
  @Override
  public BigDecimal calculateGross() {
    return dividendPerShare.multiply(quantity);
  }

  /**
   * Returns zero — dividends carry no commission.
   *
   * @return zero
   */
  @Override
  public BigDecimal calculateCommission() {
    return BigDecimal.ZERO;
  }

  /**
   * Returns zero — dividends are not taxed in this model.
   *
   * @return zero
   */
  @Override
  public BigDecimal calculateTax() {
    return BigDecimal.ZERO;
  }

  /**
   * Returns the total dividend payout, equal to the gross.
   *
   * @return total dividend amount
   */
  @Override
  public BigDecimal calculateTotal() {
    return calculateGross();
  }
}
