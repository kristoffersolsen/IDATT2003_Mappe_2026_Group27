package org.example.model.transaction;

import org.example.model.Portfolio;
import org.example.service.TransactionArchive;
import org.example.model.Player;
import org.example.model.Share;

/**
 * A sale transaction
 */
public class Sale extends Transaction {

    /**
     *
     * @param share The share to transact
     * @param week Week of transaction
     */
    public Sale(Share share, int week) {
        super(share, week, new SaleCalculator(share));

    }

    /**
     * Checks if transaction is valid and commits the transaction.
     * @param player The player to perform a transaction on
     */
    @Override
    public void commit(Player player) {
        Portfolio portfolio = player.getPortfolio();

        // Check if player portfolio has the share
        if (!portfolio.contains(share)) {
            throw new IllegalArgumentException("Player does not have this share in their portfolio.");
        }
        // Check if transaction has been performed before
        TransactionArchive archive = player.getTransactionArchive();
        if (archive.getTransactions().contains(this)) {
            throw new IllegalArgumentException("This transaction has already been performed.");
        }
        player.addMoney(this.calculator.calculateTotal());
        player.getPortfolio().removeShare(share);
        player.getTransactionArchive().add(this);

        committed = true;
    }
}
