package ntnu.idatt2003.millions.model.transaction;

import java.math.BigDecimal;

/**
 * Interface for purchase and sale calculators.
 */
public interface TransactionCalculator {
  BigDecimal calculateGross();

  BigDecimal calculateCommission();

  BigDecimal calculateTax();

  BigDecimal calculateTotal();
}
