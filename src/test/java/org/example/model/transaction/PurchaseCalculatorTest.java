package org.example.model.transaction;

import org.example.model.Share;
import org.example.model.Stock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseCalculatorTest {
    Stock stock = new Stock("sym", "companyName", BigDecimal.valueOf(10));
    Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
    PurchaseCalculator purchaseCalculator = new PurchaseCalculator(share);
    @Test
    void calculateGross() {
        assertEquals(0, BigDecimal.valueOf(50).compareTo(purchaseCalculator.calculateGross()));
    }

    @Test
    void calculateCommision() {
        assertEquals(0, BigDecimal.valueOf(0.25).compareTo(purchaseCalculator.calculateCommision()));
    }

    @Test
    void calculateTax() {
        assertEquals(0, BigDecimal.valueOf(0).compareTo(purchaseCalculator.calculateTax()));
    }

    @Test
    void calculateTotal() {
        assertEquals(0, BigDecimal.valueOf(50.25).compareTo(purchaseCalculator.calculateTotal()));
    }
}