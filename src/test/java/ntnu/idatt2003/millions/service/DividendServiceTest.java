package ntnu.idatt2003.millions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.millions.model.Exchange;
import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.Share;
import ntnu.idatt2003.millions.model.Stock;
import ntnu.idatt2003.millions.model.transaction.Dividend;
import ntnu.idatt2003.millions.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DividendService")
class DividendServiceTest {

  private static final BigDecimal DIVIDEND_PER_SHARE = new BigDecimal("0.50");
  private static final int INTERVAL = 10;
  private static final BigDecimal STARTING_MONEY = new BigDecimal("5000");

  private DividendService service;
  private Player player;
  private Stock dividendStock;
  private Stock nonDividendStock;
  private Exchange exchange;

  @BeforeEach
  void setUp() {
    dividendStock = new Stock("DIV", "Dividend Co", new BigDecimal("100.00"),
        DIVIDEND_PER_SHARE, INTERVAL);
    nonDividendStock = new Stock("NODIV", "Plain Co", new BigDecimal("50.00"));
    exchange = new Exchange("Test", 0L, List.of(dividendStock, nonDividendStock));
    service = new DividendService(exchange);
    player = new Player("Alice", STARTING_MONEY);
    service.registerPlayer(player);
  }

  @Nested
  @DisplayName("payDividends — payment eligibility")
  class PaymentEligibility {

    @Test
    @DisplayName("payDividends_holdsShares_creditsCash")
    void payDividends_holdsShares_creditsCash() {
      player.getPortfolio().addShare(new Share(dividendStock, new BigDecimal("10"),
          new BigDecimal("90.00")));

      BigDecimal cashBefore = player.getMoney();
      service.payDividends(INTERVAL);

      BigDecimal expected = DIVIDEND_PER_SHARE.multiply(new BigDecimal("10"));
      assertEquals(0, cashBefore.add(expected).compareTo(player.getMoney()));
    }

    @Test
    @DisplayName("payDividends_noSharesHeld_noCreditIssued")
    void payDividends_noSharesHeld_noCreditIssued() {
      BigDecimal cashBefore = player.getMoney();
      service.payDividends(INTERVAL);
      assertEquals(cashBefore, player.getMoney());
    }

    @Test
    @DisplayName("payDividends_nonDividendStock_noCreditIssued")
    void payDividends_nonDividendStock_noCreditIssued() {
      player.getPortfolio().addShare(new Share(nonDividendStock, new BigDecimal("10"),
          new BigDecimal("45.00")));

      BigDecimal cashBefore = player.getMoney();
      service.payDividends(INTERVAL);
      assertEquals(cashBefore, player.getMoney());
    }
  }

  @Nested
  @DisplayName("payDividends — interval boundaries")
  class IntervalBoundaries {

    @BeforeEach
    void giveShares() {
      player.getPortfolio().addShare(new Share(dividendStock, new BigDecimal("5"),
          new BigDecimal("90.00")));
    }

    @Test
    @DisplayName("payDividends_tickAtInterval_paymentIssued")
    void payDividends_tickAtInterval_paymentIssued() {
      BigDecimal cashBefore = player.getMoney();
      service.payDividends(INTERVAL);
      assertTrue(player.getMoney().compareTo(cashBefore) > 0);
    }

    @Test
    @DisplayName("payDividends_tickAtDoubleInterval_paymentIssued")
    void payDividends_tickAtDoubleInterval_paymentIssued() {
      BigDecimal cashBefore = player.getMoney();
      service.payDividends(INTERVAL * 2L);
      assertTrue(player.getMoney().compareTo(cashBefore) > 0);
    }

    @Test
    @DisplayName("payDividends_tickNotAtInterval_noPayment")
    void payDividends_tickNotAtInterval_noPayment() {
      BigDecimal cashBefore = player.getMoney();
      service.payDividends(INTERVAL - 1L);
      assertEquals(cashBefore, player.getMoney());
    }

    @Test
    @DisplayName("payDividends_tickZero_noPayment")
    void payDividends_tickZero_noPayment() {
      BigDecimal cashBefore = player.getMoney();
      service.payDividends(0L);
      assertEquals(cashBefore, player.getMoney());
    }
  }

  @Nested
  @DisplayName("payDividends — archive")
  class Archive {

    @BeforeEach
    void giveShares() {
      player.getPortfolio().addShare(new Share(dividendStock, new BigDecimal("3"),
          new BigDecimal("90.00")));
    }

    @Test
    @DisplayName("payDividends_tickAtInterval_transactionAddedToArchive")
    void payDividends_tickAtInterval_transactionAddedToArchive() {
      service.payDividends(INTERVAL);
      assertFalse(player.getTransactionArchive().getTransactions().isEmpty());
    }

    @Test
    @DisplayName("payDividends_archived_isDividendInstance")
    void payDividends_archived_isDividendInstance() {
      service.payDividends(INTERVAL);
      List<Transaction> txs = player.getTransactionArchive().getTransactions();
      assertTrue(txs.get(0) instanceof Dividend);
    }

    @Test
    @DisplayName("payDividends_archived_correctAmount")
    void payDividends_archived_correctAmount() {
      service.payDividends(INTERVAL);
      Dividend div = (Dividend) player.getTransactionArchive().getTransactions().get(0);
      BigDecimal expected = DIVIDEND_PER_SHARE.multiply(new BigDecimal("3"));
      assertEquals(0, expected.compareTo(div.getTotalPaid()));
    }

    @Test
    @DisplayName("payDividends_twoIntervals_twoArchiveEntries")
    void payDividends_twoIntervals_twoArchiveEntries() {
      service.payDividends(INTERVAL);
      service.payDividends(INTERVAL * 2L);
      assertEquals(2, player.getTransactionArchive().getTransactions().size());
    }
  }

  @Nested
  @DisplayName("payDividends — unregistered player")
  class UnregisteredPlayer {

    @Test
    @DisplayName("payDividends_unregisteredPlayer_receivesNoDividend")
    void payDividends_unregisteredPlayer_receivesNoDividend() {
      Player other = new Player("Bob", STARTING_MONEY);
      other.getPortfolio().addShare(new Share(dividendStock, new BigDecimal("5"),
          new BigDecimal("90.00")));
      // Bob is not registered; service only tracks Alice

      BigDecimal cashBefore = other.getMoney();
      service.payDividends(INTERVAL);
      assertEquals(cashBefore, other.getMoney());
    }
  }
}
