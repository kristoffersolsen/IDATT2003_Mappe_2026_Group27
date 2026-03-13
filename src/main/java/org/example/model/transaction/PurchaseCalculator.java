package org.example.model.transaction;

import org.example.model.transaction.TransactionCalculator;
import org.example.model.Share;

import java.math.BigDecimal;

/**
 * Calculators for when a share is purchased.
 */
public class PurchaseCalculator implements TransactionCalculator {
    private BigDecimal purchasePrice;
    private BigDecimal quantity;

    /**
     *
     * @param share The share to calculate on
     */
    public PurchaseCalculator(Share share) {
        this.purchasePrice = share.getPurchasePrice();
        this.quantity = share.getQuantity();
    }

    /**
     * Calculates the gross, purchasePrice * quantity
     * @return The gross
     */
    public BigDecimal calculateGross(){
        return this.purchasePrice.multiply(this.quantity);
    }

    /**
     * Calculates the commision, 0.5% of gross
     * @return The commision
     */
    public BigDecimal calculateCommision(){
        return calculateGross().multiply(BigDecimal.valueOf(0.005));
    }

    /**
     * Calculates the tax, always 0 for purchases
     * @return The tax
     */
    public BigDecimal calculateTax(){
        return BigDecimal.valueOf(0);
    }

    /**
     * Calculates the total cost of the purcahse, gross + commision + tax
     * @return The total cost
     */
    public BigDecimal calculateTotal() {
        return calculateGross().add(calculateCommision()).add(calculateTax());
    }
}
