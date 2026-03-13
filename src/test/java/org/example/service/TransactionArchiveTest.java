package org.example.service;

import org.example.model.Player;
import org.example.model.Share;
import org.example.model.Stock;
import org.example.model.transaction.Purchase;
import org.example.model.transaction.Sale;
import org.example.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionArchive")
class TransactionArchiveTest {

  private TransactionArchive archive;
  private Stock stock;

  @BeforeEach
  void setUp() {
    archive = new TransactionArchive();
    stock = new Stock("AAA", "Company A", BigDecimal.valueOf(10));
  }

  private Purchase makePurchase(int week) {
    Share share = new Share(stock, BigDecimal.valueOf(1), BigDecimal.valueOf(10));
    return new Purchase(share, week);
  }

  private Sale makeSale(int week) {
    Share share = new Share(stock, BigDecimal.valueOf(1), BigDecimal.valueOf(10));
    return new Sale(share, week);
  }

  @Test
  @DisplayName("new archive is empty")
  void newArchiveIsEmpty() {
    assertTrue(archive.isEmpty());
  }

  @Test
  @DisplayName("archive is not empty after adding a transaction")
  void notEmptyAfterAdd() {
    archive.add(makePurchase(1));
    assertFalse(archive.isEmpty());
  }

  @Test
  @DisplayName("getTransactions returns all added transactions")
  void getTransactionsAll() {
    Purchase p = makePurchase(1);
    Sale s = makeSale(2);
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
      archive.add(makePurchase(1));
      archive.add(makeSale(2));
      archive.add(makePurchase(3));
      List<Purchase> purchases = archive.getPurchases();
      assertEquals(2, purchases.size());
      purchases.forEach(p -> assertInstanceOf(Purchase.class, p));
    }

    @Test
    @DisplayName("getSales returns only Sale instances")
    void getSalesFiltered() {
      archive.add(makePurchase(1));
      archive.add(makeSale(2));
      archive.add(makeSale(3));
      List<Sale> sales = archive.getSales();
      assertEquals(2, sales.size());
      sales.forEach(s -> assertInstanceOf(Sale.class, s));
    }

    @Test
    @DisplayName("getPurchases returns empty list when there are none")
    void getPurchasesEmptyWhenNone() {
      archive.add(makeSale(1));
      assertTrue(archive.getPurchases().isEmpty());
    }

    @Test
    @DisplayName("getSales returns empty list when there are none")
    void getSalesEmptyWhenNone() {
      archive.add(makePurchase(1));
      assertTrue(archive.getSales().isEmpty());
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
      archive.add(makePurchase(1));
      archive.add(makeSale(1));
      assertEquals(1, archive.countDistinctWeeks());
    }

    @Test
    @DisplayName("counts each unique week exactly once")
    void countsUniqueWeeks() {
      archive.add(makePurchase(1));
      archive.add(makeSale(2));
      archive.add(makePurchase(3));
      archive.add(makeSale(3));
      assertEquals(3, archive.countDistinctWeeks());
    }
  }
}