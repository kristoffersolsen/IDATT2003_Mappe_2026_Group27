package org.example;

import java.util.ArrayList;
import java.util.List;

public class TransactionArchive {
    private List<Transaction> transactions;

        public TransactionArchive() {
                this.transactions = new ArrayList<>();
        }

        public void add(Transaction transaction) {
            transactions.add(transaction);
        }

        public boolean isEmpty() {
            return transactions.isEmpty();
        }

        public List<Transaction> getTransactions() {
            return transactions;
        }

        public List<Purchase> getPurchases() {
            List<Purchase> purchases = new ArrayList<>();

            for (Transaction transaction : transactions) {
                if (transaction instanceof Purchase) {
                    purchases.add((Purchase) transaction);
                }
            }
            return purchases;
        }

        public List<Sale> getSales() {
            List<Sale> sales = new ArrayList<>();

            for (Transaction transaction : transactions) {
                if (transaction instanceof Sale) {
                    sales.add((Sale) transaction);
                }
            }
            return sales;
        }

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
