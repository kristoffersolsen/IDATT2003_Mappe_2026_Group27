package org.example.service;

import org.example.model.transaction.Purchase;
import org.example.model.transaction.Sale;
import org.example.model.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps an archive of transactions
 */
public class TransactionArchive {
  private final List<Transaction> transactions;

  /**
   * Default constructor.
   */
  public TransactionArchive() {
    this.transactions = new ArrayList<>();
  }

  /**
   * Adds a transaction to the archive.
   *
   * @param transaction transaction to add
   */
  public void add(Transaction transaction) {
    transactions.add(transaction);
  }

  public boolean isEmpty() {
    return transactions.isEmpty();
  }

  public List<Transaction> getTransactions() {
    return transactions;
  }

  /**
   * Retrieves all purchases in the archive.
   *
   * @return List of Purchases
   */
  public List<Purchase> getPurchases() {
    List<Purchase> purchases = new ArrayList<>();

    for (Transaction transaction : transactions) {
      if (transaction instanceof Purchase) {
        purchases.add((Purchase) transaction);
      }
    }
    return purchases;
  }

  /**
   * Retrives all sales in the archive.
   *
   * @return List of Sales
   */
  public List<Sale> getSales() {
    List<Sale> sales = new ArrayList<>();

    for (Transaction transaction : transactions) {
      if (transaction instanceof Sale) {
        sales.add((Sale) transaction);
      }
    }
    return sales;
  }

  /**
   * Counts how many distinct weeks the archive spans.
   *
   * @return number of weeks
   */
  public int countDistinctWeeks() {
    int weeks = 0;
    List<Integer> distinctWeeks = new ArrayList<>();

    for (Transaction transaction : transactions) {
      if (!distinctWeeks.contains(transaction.getWeek())) {
        distinctWeeks.add(transaction.getWeek());
        weeks++;
      }
    }
    return weeks;
  }
}
