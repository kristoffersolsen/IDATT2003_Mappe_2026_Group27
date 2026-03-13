package org.example.model.transaction;

import org.example.model.Share;
import org.example.model.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurchaseCalculator")
class PurchaseCalculatorTest {

  // share: quantity=10, purchasePrice=5 => gross = 50
  private final Stock stock = new Stock("SYM", "Company", BigDecimal.valueOf(10));
  private final Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
  private final PurchaseCalculator calc = new PurchaseCalculator(share);

  @Test
  @DisplayName("calculateGross returns purchasePrice × quantity")
  void calculateGross() {
    assertEquals(0, BigDecimal.valueOf(50).compareTo(calc.calculateGross()));
  }

  @Test
  @DisplayName("calculateCommision is 0.5% of gross")
  void calculateCommision() {
    assertEquals(0, BigDecimal.valueOf(0.25).compareTo(calc.calculateCommision()));
  }

  @Test
  @DisplayName("calculateTax is always zero for purchases")
  void calculateTax() {
    assertEquals(0, BigDecimal.ZERO.compareTo(calc.calculateTax()));
  }

  @Test
  @DisplayName("calculateTotal is gross + commission + tax")
  void calculateTotal() {
    assertEquals(0, BigDecimal.valueOf(50.25).compareTo(calc.calculateTotal()));
  }

  @Nested
  @DisplayName("consistency")
  class Consistency {

    @Test
    @DisplayName("total equals gross plus commission")
    void totalEqualsGrossPlusCommission() {
      BigDecimal expected = calc.calculateGross().add(calc.calculateCommision());
      assertEquals(0, expected.compareTo(calc.calculateTotal()));
    }

    @Test
    @DisplayName("higher quantity produces proportionally higher gross")
    void higherQuantityHigherGross() {
      Share bigShare = new Share(stock, BigDecimal.valueOf(100), BigDecimal.valueOf(5));
      PurchaseCalculator bigCalc = new PurchaseCalculator(bigShare);
      assertTrue(bigCalc.calculateGross().compareTo(calc.calculateGross()) > 0);
    }

    @Test
    @DisplayName("zero quantity gives zero gross and zero total")
    void zeroQuantity() {
      Share zeroShare = new Share(stock, BigDecimal.ZERO, BigDecimal.valueOf(5));
      PurchaseCalculator zeroCalc = new PurchaseCalculator(zeroShare);
      assertEquals(0, BigDecimal.ZERO.compareTo(zeroCalc.calculateGross()));
      assertEquals(0, BigDecimal.ZERO.compareTo(zeroCalc.calculateTotal()));
    }
  }
}