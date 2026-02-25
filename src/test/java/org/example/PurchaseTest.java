package org.example;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTest {

    @Test
    void commit() {
        // Test successful purchase
        Player player = new Player("playerName", BigDecimal.valueOf(51));
        Share share = new Share(new Stock("sym", "companyName", BigDecimal.valueOf(20)), BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);
        assertTrue(purchase.isCommitted());

        // Test failure: player has insufficient funds
        Player player1 = new Player("playerName", BigDecimal.valueOf(50));
        Share share1 = new Share(new Stock("sym", "companyName", BigDecimal.valueOf(20)), BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        Purchase purchase1 = new Purchase(share1, 1);
        System.out.println(player1.getMoney());
        assertThrows(IllegalArgumentException.class, () -> purchase1.commit(player1));

        // Test failure: purchase already committed
        assertThrows(IllegalArgumentException.class, () -> purchase.commit(player));
    }

}