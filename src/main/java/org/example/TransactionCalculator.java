package org.example;

import java.math.BigDecimal;

interface TransactionCalculator {
    BigDecimal calculateGross();
    BigDecimal calculateCommision();
    BigDecimal calculateTax();
    BigDecimal calculateTotal();
}
