package ntnu.idatt2003.millions.transaction.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.millions.player.model.Share;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.transaction.model.Dividend;
import ntnu.idatt2003.millions.transaction.model.Purchase;
import ntnu.idatt2003.millions.transaction.model.Sale;
import ntnu.idatt2003.millions.transaction.model.Transaction;
import ntnu.idatt2003.millions.transaction.model.TransactionArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TransactionArchive")
class TransactionArchiveTest {

  /** Ticks per trading week: 8 h/day × 5 days/week = 40. */
  private static final long TICKS_PER_WEEK = 40L;

  private TransactionArchive archive;
  private Stock stock;

  @BeforeEach
  void setUp() {
    archive = new TransactionArchive();
    stock = new Stock("AAA", "Company A", BigDecimal.valueOf(10));
  }

  private Purchase makePurchase(long tick) {
    Share share = new Share(stock, BigDecimal.valueOf(1), BigDecimal.valueOf(10));
    return new Purchase(share, tick);
  }

  private Sale makeSale(long tick) {
    Share share = new Share(stock, BigDecimal.valueOf(1), BigDecimal.valueOf(10));
    return new Sale(share, tick);
  }

  private Dividend makeDividend(long tick) {
    Share share = new Share(stock, BigDecimal.valueOf(5), new BigDecimal("0.50"));
    return new Dividend(share, tick);
  }

  @Test
  @DisplayName("new archive is empty")
  void newArchiveIsEmpty() {
    assertTrue(archive.isEmpty());
  }

  @Test
  @DisplayName("archive is not empty after adding a transaction")
  void notEmptyAfterAdd() {
    archive.add(makePurchase(0L));
    assertFalse(archive.isEmpty());
  }

  @Test
  @DisplayName("getTransactions returns all added transactions")
  void getTransactionsAll() {
    Purchase p = makePurchase(0L);
    Sale s = makeSale(TICKS_PER_WEEK);
    archive.add(p);
    archive.add(s);
    List<Transaction> all = archive.getTransactions();
    assertEquals(2, all.size());
    assertTrue(all.contains(p));
    assertTrue(all.contains(s));
  }

  @Nested
  @DisplayName("getPurchases / getSales")
  class Filtering {

    @Test
    @DisplayName("getPurchases returns only Purchase instances")
    void getPurchasesFiltered() {
      archive.add(makePurchase(0L));
      archive.add(makeSale(TICKS_PER_WEEK));
      archive.add(makePurchase(2 * TICKS_PER_WEEK));
      List<Purchase> purchases = archive.getPurchases();
      assertEquals(2, purchases.size());
      purchases.forEach(p -> assertInstanceOf(Purchase.class, p));
    }

    @Test
    @DisplayName("getSales returns only Sale instances")
    void getSalesFiltered() {
      archive.add(makePurchase(0L));
      archive.add(makeSale(TICKS_PER_WEEK));
      archive.add(makeSale(2 * TICKS_PER_WEEK));
      List<Sale> sales = archive.getSales();
      assertEquals(2, sales.size());
      sales.forEach(s -> assertInstanceOf(Sale.class, s));
    }

    @Test
    @DisplayName("getPurchases returns empty list when there are none")
    void getPurchasesEmptyWhenNone() {
      archive.add(makeSale(0L));
      assertTrue(archive.getPurchases().isEmpty());
    }

    @Test
    @DisplayName("getSales returns empty list when there are none")
    void getSalesEmptyWhenNone() {
      archive.add(makePurchase(0L));
      assertTrue(archive.getSales().isEmpty());
    }
  }

  @Nested
  @DisplayName("Dividend round-trip")
  class DividendRoundTrip {

    @Test
    @DisplayName("dividend_addedToArchive_retrievedAsTransaction")
    void dividend_addedToArchive_retrievedAsTransaction() {
      Dividend div = makeDividend(0L);
      archive.add(div);
      List<Transaction> all = archive.getTransactions();
      assertEquals(1, all.size());
      assertInstanceOf(Dividend.class, all.get(0));
    }

    @Test
    @DisplayName("dividend_preservesDividendPerShare")
    void dividend_preservesDividendPerShare() {
      Dividend div = makeDividend(0L);
      archive.add(div);
      Dividend retrieved = (Dividend) archive.getTransactions().get(0);
      assertEquals(0, new BigDecimal("0.50").compareTo(retrieved.getDividendPerShare()));
    }

    @Test
    @DisplayName("dividend_preservesTotalPaid")
    void dividend_preservesTotalPaid() {
      Dividend div = makeDividend(0L);
      archive.add(div);
      Dividend retrieved = (Dividend) archive.getTransactions().get(0);
      BigDecimal expected = new BigDecimal("0.50").multiply(BigDecimal.valueOf(5));
      assertEquals(0, expected.compareTo(retrieved.getTotalPaid()));
    }

    @Test
    @DisplayName("dividend_mixedArchive_retrievedAlongPurchasesAndSales")
    void dividend_mixedArchive_retrievedAlongPurchasesAndSales() {
      archive.add(makePurchase(0L));
      archive.add(makeDividend(5L));
      archive.add(makeSale(TICKS_PER_WEEK));
      assertEquals(3, archive.getTransactions().size());
    }
  }

  @Nested
  @DisplayName("countDistinctWeeks")
  class DistinctWeeks {

    @Test
    @DisplayName("zero weeks when archive is empty")
    void zeroWhenEmpty() {
      assertEquals(0, archive.countDistinctWeeks());
    }

    @Test
    @DisplayName("one distinct week for multiple transactions in same week")
    void oneWeekForSameWeek() {
      archive.add(makePurchase(0L));           // week 1 (tick 0)
      archive.add(makeSale(5L));               // week 1 (tick 5)
      assertEquals(1, archive.countDistinctWeeks());
    }

    @Test
    @DisplayName("counts each unique week exactly once")
    void countsUniqueWeeks() {
      archive.add(makePurchase(0L));               // week 1
      archive.add(makeSale(TICKS_PER_WEEK));       // week 2
      archive.add(makePurchase(2 * TICKS_PER_WEEK)); // week 3
      archive.add(makeSale(2 * TICKS_PER_WEEK + 5L)); // week 3
      assertEquals(3, archive.countDistinctWeeks());
    }
  }
}
