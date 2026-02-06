package org.example;

/**
 * A sale transaction
 */
public class Sale extends Transaction{

    /**
     *
     * @param share The share to transact
     * @param week Week of transaction
     */
    Sale(Share share, int week) {
        super(share, week, new SaleCalculator(share));

    }

    /**
     * Checks if transaction is valid and commits the transaction.
     * @param player The player to perform a transaction on
     */
    @Override
    public void commit(Player player) {
        Portfolio portfolio = player.getportfolio();
        // Check if player portfolio has the share
        if (!portfolio.contains(share)) {
            return;
        }
        // Check if transaction has been performed before
        TransactionArchive archive = player.getTransactionArchive();
        if (archive.contains(this)) {
            return;
        }
        player.addMoney(this.calculator.calculateTotal());
        player.getPortfolio().removeShare(share);
        player.getTransactionArchive().add(this);

        committed = true;
    }
}
