package org.example.service;

import org.example.model.Player;
import org.example.model.Share;
import org.example.model.Stock;
import org.example.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeTest {
    Exchange exchange;

    @BeforeEach
    void setUp() {
        List<Stock> stocks = List.of(
                new Stock("sym1", "companyName1", BigDecimal.valueOf(10)),
                new Stock("sym2", "companyName2", BigDecimal.valueOf(20)),
                new Stock("symb3", "companyNam3", BigDecimal.valueOf(30))
        );
        exchange = new Exchange("exchangeName", 1, stocks);
    }

    @Test
    void hasStock() {
        assertTrue(exchange.hasStock("sym1"));
        assertFalse(exchange.hasStock("nonexistent"));
    }

    @Test
    void getStock() {
        assertThrows(IllegalArgumentException.class, () -> exchange.getStock("symb2"));
        assertEquals("sym1", exchange.getStock("sym1").getSymbol());
    }

    @Test
    void findStocks() {
            List<Stock> stocks = exchange.findStocks("companyName");
            assertEquals(2, stocks.size());
            assertTrue(stocks.stream().anyMatch(stock -> stock.getSymbol().equals("sym1")));
            assertTrue(stocks.stream().anyMatch(stock -> stock.getSymbol().equals("sym2")));
    }

    @Test
    void buy() {
        Player player = new Player("playerName", BigDecimal.valueOf(100));
        Transaction transaction = exchange.buy("sym1", BigDecimal.valueOf(1), player);
    }

    @Test
    void sell() {
        Player player = new Player("playerName", BigDecimal.valueOf(100));
            Stock stock = exchange.getStock("sym1");
            Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
            player.getPortfolio().addShare(share);
            Transaction transaction = exchange.sell("sym1", BigDecimal.valueOf(1), player);
    }

    @Test
    void advance() {

    }
}