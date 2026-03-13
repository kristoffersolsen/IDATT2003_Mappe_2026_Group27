package org.example.model.transaction;

import org.example.model.Share;

import java.math.BigDecimal;

/**
 * Calculators for when a share is sold
 */
public class SaleCalculator implements TransactionCalculator {
  private final BigDecimal purchasePrice;
  private final BigDecimal salesPrice;
  private final BigDecimal quantity;

  /**
   * Constructor.
   *
   * @param share The share to calculate on
   */
  public SaleCalculator(Share share) {
    this.purchasePrice = share.purchasePrice();
    this.salesPrice = share.stock().getSalesPrice();
    this.quantity = share.quantity();
  }

  /**
   * Calculates the gross, salesprice * quantity.
   *
   * @return The gross
   */
  public BigDecimal calculateGross() {
    return this.salesPrice.multiply(this.quantity);
  }

  /**
   * Calculates the commision, gross * 1%.
   *
   * @return the commision
   */
  public BigDecimal calculateCommision() {
    return calculateGross().multiply(BigDecimal.valueOf(0.01));
  }

  /**
   * Calculates the tax, gross - commision - (salesprice*quantity).
   *
   * @return The tax
   */
  public BigDecimal calculateTax() {
    return calculateGross().subtract(calculateCommision()).subtract(this.purchasePrice.multiply(this.quantity));
  }

  /**
   * Calculates the total sales value, gross - commision - tax.
   *
   * @return The total
   */
  public BigDecimal calculateTotal() {
    return calculateGross().subtract(calculateCommision()).subtract(calculateTax());
  }
}
