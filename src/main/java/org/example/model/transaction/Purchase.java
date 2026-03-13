package org.example.model.transaction;

import org.example.model.Player;
import org.example.model.Share;
import org.example.service.TransactionArchive;

/**
 * A purchase transaction
 */
public class Purchase extends Transaction {

  /**
   * Constructor.
   *
   * @param share The share to transact
   * @param week  The Week of transaction
   */
  public Purchase(Share share, int week) {
    super(share, week, new PurchaseCalculator(share));

  }

  /**
   * Checks if transaction is valid and commits the transaction.
   *
   * @param player The player to perform a transaction on
   */
  @Override
  public void commit(Player player) {

    // Check if transaction has been performed before
    TransactionArchive archive = player.getTransactionArchive();
    if (archive.getTransactions().contains(this)) {
      throw new IllegalArgumentException("This transaction has already been performed.");
    }

    // Check if player has enough money to perform transaction
    if (player.getMoney().compareTo(this.calculator.calculateTotal()) < 0) {
      throw new IllegalArgumentException("Player does not have enough money to perform this transaction.");
    }

    player.withdrawMoney(this.calculator.calculateTotal());
    player.getPortfolio().addShare(this.share);
    player.getTransactionArchive().add(this);

    committed = true;
  }
}
