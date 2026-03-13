package org.example.model.transaction;

import java.math.BigDecimal;

/**
 * Interface for purchase and sale calculators
 */
interface TransactionCalculator {
    BigDecimal calculateGross();
    BigDecimal calculateCommision();
    BigDecimal calculateTax();
    BigDecimal calculateTotal();
}
