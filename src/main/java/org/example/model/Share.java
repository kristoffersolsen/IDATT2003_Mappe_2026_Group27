package org.example.model;

import java.math.BigDecimal;

/**
 * Represents a share belonging to a company after a stock has been bought.
 */
public record Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {

  /**
   * Default constructor
   *
   * @param stock         The stock that has been bought
   * @param quantity      Quantity of the stock
   * @param purchasePrice The purchaseprice of the stock
   */
  public Share {
  }
}
