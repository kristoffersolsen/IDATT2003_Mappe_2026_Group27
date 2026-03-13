package org.example.model.transaction;

import org.example.model.Player;
import org.example.model.Share;
import org.example.model.Stock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SaleTest {

    @Test
    void commit() {
        // Test successful sale
        Player player = new Player("playerName", BigDecimal.valueOf(100));
        Stock stock = new Stock("sym", "companyName", BigDecimal.valueOf(20));
        Share share = new Share(stock, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        Sale sale = new Sale(share, 1);
        player.getPortfolio().addShare(share);
        sale.commit(player);
        assertTrue(sale.isCommitted());

        // Test failure: player does not own the share
        Player player1 = new Player("playerName", BigDecimal.valueOf(100));
        Stock stock1 = new Stock("sym", "companyName", BigDecimal.valueOf(20));
        Share share1 = new Share(stock1, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        Sale sale1 = new Sale(share1, 1);
        assertThrows(IllegalArgumentException.class, () -> sale1.commit(player1));

        // Test failure: sale already committed
        assertThrows(IllegalArgumentException.class, () -> sale.commit(player));
    }
}