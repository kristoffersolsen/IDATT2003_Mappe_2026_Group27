package org.example;

import java.math.BigDecimal;

/**
 * Calculators for when a share is sold
 */
public class SaleCalculator implements TransactionCalculator{
    private BigDecimal purchasePrice;
    private BigDecimal salesPrice;
    private BigDecimal quantity;

    /**
     *
     * @param share The share to calculate on
     */
    SaleCalculator(Share share) {
        this.purchasePrice = share.getPurchasePrice();
        this.salesPrice = share.getStock().getSalesPrice();
        this.quantity = share.getQuantity();
    }

    /**
     * Calculates the gross, salesprice * quantity
     * @return The gross
     */
    public BigDecimal calculateGross(){
        return this.salesPrice.multiply(this.quantity);
    }

    /**
     * Calculates the commision, gross * 1%
     * @return the commision
     */
    public BigDecimal calculateCommision(){
        return calculateGross().multiply(BigDecimal.valueOf(0.01));
    }

    /**
     * Calculates the tax, gross - commision - (salesprice*quantity)
     * @return The tax
     */
    public BigDecimal calculateTax(){
        return calculateGross().subtract(calculateCommision()).subtract(this.purchasePrice.multiply(this.quantity));
    }

    /**
     * Calculates the total sales value, gross - commision - tax
     * @return
     */
    public BigDecimal calculateTotal() {
        return calculateGross().subtract(calculateCommision()).subtract(calculateTax());
    }
}
