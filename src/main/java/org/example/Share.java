package org.example;

import java.math.BigDecimal;

public class Share {
    private Stock stock;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;

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
