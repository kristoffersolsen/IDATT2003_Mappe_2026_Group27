package org.example.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ShareTest {
    Stock stock = new Stock("sym", "companyName", BigDecimal.valueOf(10));
    Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
    @Test
    void getStock() {
        assertEquals(stock, share.getStock());
    }

    @Test
    void getQuantity() {
        assertEquals(BigDecimal.valueOf(10), share.getQuantity());
    }

    @Test
    void getPurchasePrice() {
        assertEquals(BigDecimal.valueOf(5), share.getPurchasePrice());
    }
}