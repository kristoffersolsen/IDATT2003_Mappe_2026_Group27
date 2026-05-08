package org.example.model.transaction;

import java.math.BigDecimal;
import org.example.model.Share;

/**
 * Calculator for when a share is sold.
 *
 * <p>Formulas:
 * <ul>
 *   <li>gross = salesPrice × quantity</li>
 *   <li>commission = gross × {@link #COMMISSION_RATE}</li>
 *   <li>gain = gross − costBasis − commission</li>
 *   <li>tax = gain &gt; 0 ? gain × {@link #TAX_RATE} : 0</li>
 *   <li>total = gross − commission − tax</li>
 * </ul>
 */
public class SaleCalculator implements TransactionCalculator {

  /** Commission charged on sales: 1% of gross. */
  public static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");

  /** Capital-gains tax rate applied to realized gains: 22%. */
  public static final BigDecimal TAX_RATE = new BigDecimal("0.22");

  private final BigDecimal purchasePrice;
  private final BigDecimal salesPrice;
  private final BigDecimal quantity;

  /**
   * Constructor.
   *
   * @param share the share to calculate on
   */
  public SaleCalculator(Share share) {
    this.purchasePrice = share.purchasePrice();
    this.salesPrice = share.stock().getSalesPrice();
    this.quantity = share.quantity();
  }

  /**
   * Calculates the gross: salesPrice × quantity.
   *
   * @return the gross
   */
  public BigDecimal calculateGross() {
    return this.salesPrice.multiply(this.quantity);
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
   * Calculates the capital-gains tax.
   *
   * <p>Tax is {@link #TAX_RATE} of the realized gain. No tax is charged on losses.
   *
   * @return the tax
   */
  public BigDecimal calculateTax() {
    BigDecimal costBasis = purchasePrice.multiply(quantity);
    BigDecimal gain = calculateGross().subtract(costBasis).subtract(calculateCommission());
    return gain.signum() > 0 ? gain.multiply(TAX_RATE) : BigDecimal.ZERO;
  }

  /**
   * Calculates the total proceeds: gross − commission − tax.
   *
   * @return the total
   */
  public BigDecimal calculateTotal() {
    return calculateGross().subtract(calculateCommission()).subtract(calculateTax());
  }
}
