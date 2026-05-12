package ntnu.idatt2003.millions.transaction.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps an archive of all committed transactions for a player.
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

  /**
   * Returns an unmodifiable view of all transactions in the archive.
   *
   * @return live unmodifiable view
   */
  public List<Transaction> getTransactions() {
    return Collections.unmodifiableList(transactions);
  }

  /**
   * Retrieves all purchases in the archive.
   *
   * @return list of purchases
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
   * Retrieves all sales in the archive.
   *
   * @return list of sales
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
   * @return number of distinct weeks
   */
  public int countDistinctWeeks() {
    return (int) transactions.stream()
        .map(Transaction::getWeek)
        .distinct()
        .count();
  }
}
