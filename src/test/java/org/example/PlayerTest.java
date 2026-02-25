package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    Player player;

    @BeforeEach
    void setUp() {
        player = new Player("playerName", BigDecimal.valueOf(100));
    }

    @Test
    void addMoney() {
        player.addMoney(BigDecimal.valueOf(20));
        assertEquals(0, BigDecimal.valueOf(120).compareTo(player.getMoney()));
    }

    @Test
    void withdrawMoney() {
        player.withdrawMoney(BigDecimal.valueOf(30));
        assertEquals(0, BigDecimal.valueOf(70).compareTo(player.getMoney()));
    }
}