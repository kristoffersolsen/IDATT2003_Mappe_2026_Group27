package org.example.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {
    Stock stock = new Stock("symb", "companyName", BigDecimal.valueOf(10));

    @Test
    void getSymbol() {
        assertEquals("symb", stock.getSymbol());
    }

    @Test
    void getCompany() {
        assertEquals("companyName", stock.getCompany());
    }

    @Test
    void getSalesPrice() {
        assertEquals(BigDecimal.valueOf(10), stock.getSalesPrice());
    }

    @Test
    void addNewSalesPrice() {
        assertEquals(BigDecimal.valueOf(10), stock.getSalesPrice());
        stock.addNewSalesPrice(BigDecimal.valueOf(20));
        assertEquals(BigDecimal.valueOf(20), stock.getSalesPrice());
    }
}