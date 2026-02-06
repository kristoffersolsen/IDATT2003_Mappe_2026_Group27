package org.example;

/**
 * A purchase transaction
 */
public class Purchase extends Transaction{

    /**
     *
     * @param share The share to transact
     * @param week The Week of transaction
     */
    Purchase(Share share, int week) {
        super(share, week, new PurchaseCalculator(share));

    }

    /**
     * Checks if transaction is valid and commits the transaction
     * @param player The player to perform a transaction on
     */
    @Override
    public void commit(Player player) {

        // Check if transaction has been performed before
        TransactionArchive archive = player.getTransactionArchive();
        if (archive.contains(this)) {
            return;
        }

        if (player.getmoney())

        player.withdrawMoney(this.calculator.calculateTotal());
        player.getPortfolio().addShare(this.share);
        player.getTransactionArchive().add(this);

        committed = true;
    }
}
