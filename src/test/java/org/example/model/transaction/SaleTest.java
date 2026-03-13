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

@DisplayName("Sale")
class SaleTest {

  private Stock stock;
  private Share share;
  private Player player;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAA", "Company A", BigDecimal.valueOf(20));
    share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
    player = new Player("Player", BigDecimal.valueOf(100));
    player.getPortfolio().addShare(share);
  }

  @Nested
  @DisplayName("successful commit")
  class SuccessfulCommit {

    @Test
    @DisplayName("isCommitted is true after a valid commit")
    void isCommittedAfterCommit() {
      Sale sale = new Sale(share, 1);
      sale.commit(player);
      assertTrue(sale.isCommitted());
    }

    @Test
    @DisplayName("player receives money equal to calculateTotal()")
    void playerReceivesMoney() {
      Sale sale = new Sale(share, 1);
      BigDecimal before = player.getMoney();
      sale.commit(player);
      BigDecimal expected = before.add(sale.getCalculator().calculateTotal());
      assertEquals(0, expected.compareTo(player.getMoney()));
    }

    @Test
    @DisplayName("share is removed from player's portfolio after commit")
    void shareRemovedFromPortfolio() {
      Sale sale = new Sale(share, 1);
      sale.commit(player);
      assertFalse(player.getPortfolio().contains(share));
    }

    @Test
    @DisplayName("transaction is recorded in player's archive")
    void transactionAddedToArchive() {
      Sale sale = new Sale(share, 1);
      sale.commit(player);
      assertTrue(player.getTransactionArchive().getTransactions().contains(sale));
    }
  }

  @Nested
  @DisplayName("failure cases")
  class FailureCases {

    @Test
    @DisplayName("throws when player does not own the share")
    void throwsWhenShareNotOwned() {
      Player stranger = new Player("Player B", BigDecimal.valueOf(100));
      Sale sale = new Sale(share, 1);
      assertThrows(IllegalArgumentException.class, () -> sale.commit(stranger));
    }

    @Test
    @DisplayName("player balance is unchanged after failed sale")
    void balanceUnchangedAfterFailure() {
      Player stranger = new Player("Player B", BigDecimal.valueOf(100));
      BigDecimal before = stranger.getMoney();
      Sale sale = new Sale(share, 1);
      assertThrows(IllegalArgumentException.class, () -> sale.commit(stranger));
      assertEquals(0, before.compareTo(stranger.getMoney()));
    }

    @Test
    @DisplayName("throws when same sale is committed twice")
    void throwsOnDoubleCommit() {
      Sale sale = new Sale(share, 1);
      sale.commit(player);
      // Re-add the share so the second commit doesn't fail for the wrong reason
      player.getPortfolio().addShare(share);
      assertThrows(IllegalArgumentException.class, () -> sale.commit(player));
    }
  }
}