package org.example.model.transaction;

import org.example.model.Share;
import org.example.model.Stock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SaleCalculatorTest {
    Stock stock = new Stock("sym", "companyName", BigDecimal.valueOf(10));
    Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
    SaleCalculator saleCalculator = new SaleCalculator(share);
    @Test
    void calculateGross() {
        assertEquals(0, BigDecimal.valueOf(100).compareTo(saleCalculator.calculateGross()));
    }

    @Test
    void calculateCommision() {
        assertEquals(0, BigDecimal.valueOf(1.00).compareTo(saleCalculator.calculateCommision()));
    }

    @Test
    void calculateTax() {
        assertEquals(0, BigDecimal.valueOf(49.00).compareTo(saleCalculator.calculateTax()));
    }

    @Test
    void calculateTotal() {
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(saleCalculator.calculateTotal()));
    }
}