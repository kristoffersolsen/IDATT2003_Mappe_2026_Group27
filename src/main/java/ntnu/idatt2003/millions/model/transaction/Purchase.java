package ntnu.idatt2003.millions.model.transaction;

import ntnu.idatt2003.millions.model.Player;
import ntnu.idatt2003.millions.model.Share;

/**
 * A purchase transaction.
 */
public final class Purchase extends Transaction {

  /**
   * Constructor.
   *
   * @param share the share to transact
   * @param tick  the simulation tick at the time of the transaction
   */
  public Purchase(Share share, long tick) {
    super(share, tick, new PurchaseCalculator(share));

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
      throw new IllegalArgumentException(
          "Player does not have enough money to perform this transaction.");
    }

    player.withdrawMoney(this.calculator.calculateTotal());
    player.getPortfolio().addShare(this.share);
    player.getTransactionArchive().add(this);

    committed = true;
  }
}
