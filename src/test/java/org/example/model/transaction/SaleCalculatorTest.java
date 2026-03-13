package org.example.model.transaction;

import org.example.model.Share;
import org.example.model.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SaleCalculator")
class SaleCalculatorTest {

  // stock salesPrice=10, share quantity=10, purchasePrice=5
  // gross = 10*10 = 100
  // commission = 100*0.01 = 1
  // tax = 100 - 1 - (5*10) = 49
  // total = 100 - 1 - 49 = 50
  private final Stock stock = new Stock("SYM", "Company", BigDecimal.valueOf(10));
  private final Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
  private final SaleCalculator calc = new SaleCalculator(share);

  @Test
  @DisplayName("calculateGross returns salesPrice × quantity")
  void calculateGross() {
    assertEquals(0, BigDecimal.valueOf(100).compareTo(calc.calculateGross()));
  }

  @Test
  @DisplayName("calculateCommision is 1% of gross")
  void calculateCommision() {
    assertEquals(0, BigDecimal.valueOf(1.00).compareTo(calc.calculateCommision()));
  }

  @Test
  @DisplayName("calculateTax is gross - commission - (purchasePrice × quantity)")
  void calculateTax() {
    assertEquals(0, BigDecimal.valueOf(49.00).compareTo(calc.calculateTax()));
  }

  @Test
  @DisplayName("calculateTotal is gross - commission - tax (equals purchasePrice × quantity)")
  void calculateTotal() {
    assertEquals(0, BigDecimal.valueOf(50.00).compareTo(calc.calculateTotal()));
  }

  @Nested
  @DisplayName("consistency")
  class Consistency {

    @Test
    @DisplayName("total + commission + tax equals gross")
    void totalPlusCommissionPlusTaxEqualsGross() {
      BigDecimal reconstructed = calc.calculateTotal()
        .add(calc.calculateCommision())
        .add(calc.calculateTax());
      assertEquals(0, reconstructed.compareTo(calc.calculateGross()));
    }

    @Test
    @DisplayName("no profit when salesPrice equals purchasePrice (only commission lost)")
    void noProfitWhenPriceUnchanged() {
      Stock flatStock = new Stock("FLAT", "Flat Co", BigDecimal.valueOf(5));
      Share flatShare = new Share(flatStock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
      SaleCalculator flatCalc = new SaleCalculator(flatShare);
      // gross = 50, commission = 0.5, tax = 50 - 0.5 - 50 = -0.5 (negative tax)
      // total = 50 - 0.5 - (-0.5) = 50  — player gets purchase price back exactly
      assertEquals(0, BigDecimal.valueOf(50).compareTo(flatCalc.calculateTotal()));
    }

    @Test
    @DisplayName("zero quantity gives zero gross and zero total")
    void zeroQuantity() {
      Share zeroShare = new Share(stock, BigDecimal.ZERO, BigDecimal.valueOf(5));
      SaleCalculator zeroCalc = new SaleCalculator(zeroShare);
      assertEquals(0, BigDecimal.ZERO.compareTo(zeroCalc.calculateGross()));
      assertEquals(0, BigDecimal.ZERO.compareTo(zeroCalc.calculateTotal()));
    }
  }
}