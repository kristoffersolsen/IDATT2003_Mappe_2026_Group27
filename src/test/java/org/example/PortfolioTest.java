package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioTest {
    Portfolio portfolio;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
    }

    @Test
    void addShare() {
        assertEquals(0, portfolio.getShares().size());
        Stock stock = new Stock("sym", "companyName", BigDecimal.valueOf(10));
        Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        portfolio.addShare(share);
        assertEquals(1, portfolio.getShares().size());
    }

    @Test
    void removeShare() {
        Stock stock = new Stock("sym", "companyName", BigDecimal.valueOf(10));
        Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        portfolio.addShare(share);
        assertEquals(1, portfolio.getShares().size());
        portfolio.removeShare(share);
        assertEquals(0, portfolio.getShares().size());
    }

    @Test
    void getShares() {
        Stock stock1 = new Stock("sym1", "companyName1", BigDecimal.valueOf(10));
        Share share1 = new Share(stock1, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        Stock stock2 = new Stock("sym2", "companyName2", BigDecimal.valueOf(20));
        Share share2 = new Share(stock2, BigDecimal.valueOf(20), BigDecimal.valueOf(10));
        portfolio.addShare(share1);
        portfolio.addShare(share2);
        assertEquals(2, portfolio.getShares().size());
    }

    @Test
    void testGetShares() {
        Stock stock1 = new Stock("sym1", "companyName1", BigDecimal.valueOf(10));
        Share share1 = new Share(stock1, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        Stock stock2 = new Stock("sym2", "companyName2", BigDecimal.valueOf(20));
        Share share2 = new Share(stock2, BigDecimal.valueOf(20), BigDecimal.valueOf(10));
        portfolio.addShare(share1);
        portfolio.addShare(share2);
        assertTrue(portfolio.getShares().contains(share1));
        assertTrue(portfolio.getShares().contains(share2));
    }

    @Test
    void contains() {
        Stock stock1 = new Stock("sym1", "companyName1", BigDecimal.valueOf(10));
        Share share1 = new Share(stock1, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        Stock stock2 = new Stock("sym2", "companyName2", BigDecimal.valueOf(20));
        Share share2 = new Share(stock2, BigDecimal.valueOf(20), BigDecimal.valueOf(10));
        portfolio.addShare(share1);
        assertTrue(portfolio.contains(share1));
        assertFalse(portfolio.contains(share2));
    }
}