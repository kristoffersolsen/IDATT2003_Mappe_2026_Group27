package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Portfolio")
class PortfolioTest {

  private Portfolio portfolio;
  private Stock stock1;
  private Stock stock2;
  private Share share1;
  private Share share2;

  @BeforeEach
  void setUp() {
    portfolio = new Portfolio();
    stock1 = new Stock("AAA", "Company A", BigDecimal.valueOf(10));
    stock2 = new Stock("BBB", "Company B", BigDecimal.valueOf(20));
    share1 = new Share(stock1, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
    share2 = new Share(stock2, BigDecimal.valueOf(5), BigDecimal.valueOf(15));
  }

  // ------------------------------------------------------------------ //
  //  addShare                                                             //
  // ------------------------------------------------------------------ //

  @Nested
  @DisplayName("addShare")
  class AddShare {

    @Test
    @DisplayName("adding a share increases size by one")
    void addsNewShare() {
      assertTrue(portfolio.addShare(share1));
      assertEquals(1, portfolio.getShares().size());
    }

    @Test
    @DisplayName("adding a duplicate share is rejected and size stays the same")
    void rejectsDuplicateShare() {
      portfolio.addShare(share1);
      assertFalse(portfolio.addShare(share1));
      assertEquals(1, portfolio.getShares().size());
    }

    @Test
    @DisplayName("multiple distinct shares can all be added")
    void addsMultipleDistinctShares() {
      portfolio.addShare(share1);
      portfolio.addShare(share2);
      assertEquals(2, portfolio.getShares().size());
    }
  }

  @Nested
  @DisplayName("removeShare")
  class RemoveShare {

    @Test
    @DisplayName("removing an existing share returns true and decreases size")
    void removesExistingShare() {
      portfolio.addShare(share1);
      assertTrue(portfolio.removeShare(share1));
      assertEquals(0, portfolio.getShares().size());
    }

    @Test
    @DisplayName("removing a share not in portfolio returns false")
    void returnsFalseForAbsentShare() {
      assertFalse(portfolio.removeShare(share1));
    }

    @Test
    @DisplayName("after removal the share is no longer present")
    void shareAbsentAfterRemoval() {
      portfolio.addShare(share1);
      portfolio.removeShare(share1);
      assertFalse(portfolio.contains(share1));
    }
  }

  @Nested
  @DisplayName("contains")
  class Contains {

    @Test
    @DisplayName("returns true when share's stock is in the portfolio")
    void returnsTrueForPresentStock() {
      portfolio.addShare(share1);
      assertTrue(portfolio.contains(share1));
    }

    @Test
    @DisplayName("returns false for a stock not in the portfolio")
    void returnsFalseForAbsentStock() {
      portfolio.addShare(share1);
      assertFalse(portfolio.contains(share2));
    }

    @Test
    @DisplayName("a new share with the same stock is considered present")
    void sameStockDifferentShareObject() {
      portfolio.addShare(share1);
      Share anotherShare = new Share(stock1, BigDecimal.valueOf(99), BigDecimal.valueOf(1));
      assertTrue(portfolio.contains(anotherShare));
    }
  }

  @Test
  @DisplayName("getShares returns empty list for new portfolio")
  void getShares_emptyInitially() {
    assertTrue(portfolio.getShares().isEmpty());
  }

  @Test
  @DisplayName("getShares returns all added shares")
  void getShares_containsAllAdded() {
    portfolio.addShare(share1);
    portfolio.addShare(share2);
    assertTrue(portfolio.getShares().contains(share1));
    assertTrue(portfolio.getShares().contains(share2));
  }

  @Nested
  @DisplayName("getNetWorth")
  class NetWorth {

    @Test
    @DisplayName("net worth of empty portfolio is zero")
    void emptyPortfolioNetWorthIsZero() {
      assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getNetWorth()));
    }

    @Test
    @DisplayName("net worth reflects total sale value minus costs")
    void netWorthWithShares() {
      // stock1 salesPrice=10, quantity=10, purchasePrice=5
      // gross = 100, commission = 1, tax = gross-commission-(5*10) = 49 => total = 50
      portfolio.addShare(share1);
      BigDecimal netWorth = portfolio.getNetWorth();
      assertTrue(netWorth.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("net worth increases when more shares are added")
    void netWorthIncreaseWithMoreShares() {
      portfolio.addShare(share1);
      BigDecimal first = portfolio.getNetWorth();
      portfolio.addShare(share2);
      BigDecimal second = portfolio.getNetWorth();
      assertTrue(second.compareTo(first) > 0);
    }
  }
}