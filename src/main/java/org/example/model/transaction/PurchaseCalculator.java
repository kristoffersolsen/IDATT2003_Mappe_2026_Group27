package org.example.model.transaction;

import java.math.BigDecimal;
import org.example.model.Share;

/**
 * Calculator for when a share is purchased.
 */
public class PurchaseCalculator implements TransactionCalculator {

  /** Commission charged on purchases: 0.5% of gross. */
  public static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");

  private final BigDecimal purchasePrice;
  private final BigDecimal quantity;

  /**
   * Constructor.
   *
   * @param share the share to calculate on
   */
  public PurchaseCalculator(Share share) {
    this.purchasePrice = share.purchasePrice();
    this.quantity = share.quantity();
  }

  /**
   * Calculates the gross: purchasePrice × quantity.
   *
   * @return the gross
   */
  public BigDecimal calculateGross() {
    return this.purchasePrice.multiply(this.quantity);
  }

  /**
   * Calculates the commission: {@link #COMMISSION_RATE} of gross.
   *
   * @return the commission
   */
  public BigDecimal calculateCommission() {
    return calculateGross().multiply(COMMISSION_RATE);
  }

  /**
   * Calculates the tax, always zero for purchases.
   *
   * @return zero
   */
  public BigDecimal calculateTax() {
    return BigDecimal.ZERO;
  }

  /**
   * Calculates the total cost of the purchase: gross + commission + tax.
   *
   * @return the total cost
   */
  public BigDecimal calculateTotal() {
    return calculateGross().add(calculateCommission()).add(calculateTax());
  }
}
