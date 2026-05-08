package org.example.model.transaction;

import java.math.BigDecimal;
import org.example.model.Player;
import org.example.model.Share;

/**
 * A sale transaction.
 *
 * <p>In addition to the fields inherited from {@link Transaction}, a sale
 * records the actual sale price per share and the realised gain (or loss)
 * at the time it is created, so the transaction history can display them
 * without recomputing.
 */
public class Sale extends Transaction {

  private final BigDecimal salePrice;
  private final BigDecimal realisedGain;

  /**
   * Constructor.
   *
   * @param share the share to transact
   * @param week  week of transaction
   */
  public Sale(Share share, int week) {
    super(share, week, new SaleCalculator(share));

    this.salePrice = share.stock().getSalesPrice();

    SaleCalculator calc = (SaleCalculator) this.calculator;
    // Realised gain = net proceeds − original cost basis
    BigDecimal proceeds = calc.calculateTotal();
    BigDecimal costBasis = share.purchasePrice().multiply(share.quantity());
    this.realisedGain = proceeds.subtract(costBasis);
  }

  /**
   * Returns the sale price per share at the time of the transaction.
   *
   * @return sale price per share
   */
  public BigDecimal getSalePrice() {
    return salePrice;
  }

  /**
   * Returns the realised gain (positive) or loss (negative) for this sale.
   *
   * <p>Calculated as net proceeds minus original cost basis.
   *
   * @return realised gain/loss
   */
  public BigDecimal getRealisedGain() {
    return realisedGain;
  }

  /**
   * Checks if transaction is valid and commits the transaction.
   *
   * <p>Note: when selling through {@link org.example.service.ExchangeService}
   * the commit is handled manually by the service to support partial sells.
   * This method is retained for direct use in tests.
   *
   * @param player the player to perform a transaction on
   */
  @Override
  public void commit(Player player) {
    if (!player.getPortfolio().contains(share)) {
      throw new IllegalArgumentException(
          "Player does not have this share in their portfolio.");
    }
    if (player.getTransactionArchive().getTransactions().contains(this)) {
      throw new IllegalArgumentException(
          "This transaction has already been performed.");
    }

    player.addMoney(this.calculator.calculateTotal());
    player.getPortfolio().removeShare(share);
    player.getTransactionArchive().add(this);

    committed = true;
  }
}