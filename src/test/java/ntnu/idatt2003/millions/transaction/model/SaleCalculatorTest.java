package ntnu.idatt2003.millions.transaction.model;

import ntnu.idatt2003.millions.player.model.Share;
import ntnu.idatt2003.millions.market.model.Stock;
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
  // costBasis = 5*10 = 50
  // gain = 100 - 50 - 1 = 49
  // tax = 49 * 0.22 = 10.78
  // total = 100 - 1 - 10.78 = 88.22
  private final Stock stock = new Stock("SYM", "Company", BigDecimal.valueOf(10));
  private final Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
  private final SaleCalculator calc = new SaleCalculator(share);

  @Test
  @DisplayName("calculateGross returns salesPrice × quantity")
  void calculateGross() {
    assertEquals(0, BigDecimal.valueOf(100).compareTo(calc.calculateGross()));
  }

  @Test
  @DisplayName("calculateCommission is 1% of gross")
  void calculateCommission() {
    assertEquals(0, BigDecimal.valueOf(1.00).compareTo(calc.calculateCommission()));
  }

  @Test
  @DisplayName("calculateTax is 22% of realized gain")
  void calculateTax() {
    assertEquals(0, BigDecimal.valueOf(10.78).compareTo(calc.calculateTax()));
  }

  @Test
  @DisplayName("calculateTotal is gross − commission − tax")
  void calculateTotal() {
    assertEquals(0, BigDecimal.valueOf(88.22).compareTo(calc.calculateTotal()));
  }

  @Nested
  @DisplayName("profitability")
  class Profitability {

    @Test
    @DisplayName("profitable sale yields total greater than purchase cost basis")
    void profitableSale_yieldsTotalGreaterThanCostBasis() {
      BigDecimal costBasis = share.purchasePrice().multiply(share.quantity());
      assertTrue(calc.calculateTotal().compareTo(costBasis) > 0);
    }

    @Test
    @DisplayName("loss-making sale yields total less than cost basis but greater than zero")
    void lossMakingSale_yieldsTotalLessThanCostBasisButPositive() {
      Stock loserStock = new Stock("LOS", "Loser Co", BigDecimal.valueOf(3));
      Share loserShare = new Share(loserStock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
      SaleCalculator loserCalc = new SaleCalculator(loserShare);
      BigDecimal costBasis = loserShare.purchasePrice().multiply(loserShare.quantity());
      assertTrue(loserCalc.calculateTotal().compareTo(costBasis) < 0);
      assertTrue(loserCalc.calculateTotal().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("when salesPrice equals purchasePrice only commission is lost")
    void noProfitWhenPriceUnchanged() {
      Stock flatStock = new Stock("FLAT", "Flat Co", BigDecimal.valueOf(5));
      Share flatShare = new Share(flatStock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
      SaleCalculator flatCalc = new SaleCalculator(flatShare);
      // No gain, so no tax — only commission (0.5) is deducted from gross (50)
      assertEquals(0, BigDecimal.valueOf(49.50).compareTo(flatCalc.calculateTotal()));
    }
  }

  @Nested
  @DisplayName("consistency")
  class Consistency {

    @Test
    @DisplayName("total + commission + tax equals gross")
    void totalPlusCommissionPlusTaxEqualsGross() {
      BigDecimal reconstructed = calc.calculateTotal()
          .add(calc.calculateCommission())
          .add(calc.calculateTax());
      assertEquals(0, reconstructed.compareTo(calc.calculateGross()));
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
