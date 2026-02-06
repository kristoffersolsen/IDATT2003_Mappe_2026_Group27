package org.example;

import java.math.BigDecimal;

/**
 * Represents a share belonging to a company after a stock has been bought.
 */
public class Share {
    private Stock stock;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;


    /**
     *
     * @param stock The stock that has been bought
     * @param quantity Quantity of the stock
     * @param purchasePrice The purchaseprice of the stock
     */
    Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {
        this.stock = stock;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }

    Stock getStock() {
        return this.stock;
    }

    BigDecimal getQuantity() {
        return this.quantity;
    }

    BigDecimal getPurchasePrice() {
        return this.purchasePrice;
    }
}
