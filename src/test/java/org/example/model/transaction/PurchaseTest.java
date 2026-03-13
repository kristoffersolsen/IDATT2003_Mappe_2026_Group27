package org.example.model.transaction;

import org.example.model.Player;
import org.example.model.Share;
import org.example.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Purchase")
class PurchaseTest {

  // quantity=10, purchasePrice=5, gross=50, commission=0.25, total=50.25
  private Stock stock;
  private Share share;

  @BeforeEach
  void setUp() {
    stock = new Stock("SYM", "Company", BigDecimal.valueOf(20));
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
  }

  @Nested
  @DisplayName("successful commit")
  class SuccessfulCommit {

    @Test
    @DisplayName("isCommitted is true after a valid commit")
    void isCommittedAfterCommit() {
      Player player = new Player("Alice", BigDecimal.valueOf(51));
      Purchase purchase = new Purchase(share, 1);
      purchase.commit(player);
      assertTrue(purchase.isCommitted());
    }

    @Test
    @DisplayName("player's balance is reduced by the total cost")
    void playerBalanceReducedByTotal() {
      Player player = new Player("Alice", BigDecimal.valueOf(200));
      Purchase purchase = new Purchase(share, 1);
      BigDecimal before = player.getMoney();
      purchase.commit(player);
      BigDecimal expected = before.subtract(purchase.getCalculator().calculateTotal());
      assertEquals(0, expected.compareTo(player.getMoney()));
    }

    @Test
    @DisplayName("share is added to player's portfolio after commit")
    void shareAddedToPortfolio() {
      Player player = new Player("Alice", BigDecimal.valueOf(200));
      Purchase purchase = new Purchase(share, 1);
      purchase.commit(player);
      assertTrue(player.getPortfolio().contains(share));
    }

    @Test
    @DisplayName("transaction appears in player's archive after commit")
    void transactionAddedToArchive() {
      Player player = new Player("Alice", BigDecimal.valueOf(200));
      Purchase purchase = new Purchase(share, 1);
      purchase.commit(player);
      assertTrue(player.getTransactionArchive().getTransactions().contains(purchase));
    }
  }

  @Nested
  @DisplayName("failure cases")
  class FailureCases {

    @Test
    @DisplayName("throws when player cannot afford the purchase")
    void throwsWhenInsufficientFunds() {
      Player poor = new Player("Poor", BigDecimal.valueOf(1));
      Purchase purchase = new Purchase(share, 1);
      assertThrows(IllegalArgumentException.class, () -> purchase.commit(poor));
    }

    @Test
    @DisplayName("balance is unchanged after a failed commit")
    void balanceUnchangedAfterFailure() {
      Player poor = new Player("Poor", BigDecimal.valueOf(1));
      BigDecimal before = poor.getMoney();
      Purchase purchase = new Purchase(share, 1);
      assertThrows(IllegalArgumentException.class, () -> purchase.commit(poor));
      assertEquals(0, before.compareTo(poor.getMoney()));
    }

    @Test
    @DisplayName("throws when trying to commit the same transaction twice")
    void throwsOnDoubleCommit() {
      Player player = new Player("Alice", BigDecimal.valueOf(200));
      Purchase purchase = new Purchase(share, 1);
      purchase.commit(player);
      assertThrows(IllegalArgumentException.class, () -> purchase.commit(player));
    }

    @Test
    @DisplayName("player with exact funds equal to total cost can commit")
    void exactFundsAllowsCommit() {
      // total cost = 50.25
      Player exact = new Player("Exact", new BigDecimal("50.25"));
      Purchase purchase = new Purchase(share, 1);
      assertDoesNotThrow(() -> purchase.commit(exact));
    }
  }

  @Test
  @DisplayName("getWeek returns the week passed at construction")
  void getWeek() {
    Purchase purchase = new Purchase(share, 7);
    assertEquals(7, purchase.getWeek());
  }

  @Test
  @DisplayName("getShare returns the share passed at construction")
  void getShare() {
    Purchase purchase = new Purchase(share, 1);
    assertSame(share, purchase.getShare());
  }
}