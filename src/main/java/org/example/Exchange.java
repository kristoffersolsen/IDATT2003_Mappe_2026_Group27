package org.example;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Exchange {

    private String name;
    private int week;
    private Map<String, Stock> stockMap;
    private Random random;

    private final double LOWER_CHANGE = -0.1; // -10%
    private final double UPPER_CHANGE = 0.1;  // +10%

    public Exchange(String name, List<Stock> stocks) {
        this.name = name;
        this.stockMap = new HashMap<>();
        for (Stock stock : stocks) {
            this.stockMap.put(stock.getSymbol(), stock);
        }
        this.week = 1;
        this.random = new Random();
    }

    public String getName() {
        return name;
    }

    public int getWeek() {
        return week;
    }

    public boolean hasStock(String symbol) {
        return stockMap.containsKey(symbol);
    }

    public Stock getStock(String symbol) {
        if (!hasStock(symbol)) {
            throw new IllegalArgumentException("Stock with symbol " + symbol + " does not exist.");
        }
        return stockMap.get(symbol);
    }

    public List<Stock> findStocks(String searchTerm) {
        return stockMap.values().stream()
                .filter(stock -> stock.getCompany().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();
    }

    public Transaction buy(String symbol, BigDecimal quantity, Player player) {
        if (!hasStock(symbol)) {
            throw new IllegalArgumentException("Stock with symbol " + symbol + " does not exist.");
        }
        Stock stock = getStock(symbol);
        Share share = new Share(stock, quantity, stock.getSalesPrice());
        Transaction transaction = new Purchase(share, getWeek());
        transaction.commit(player);
        return transaction;
    }

    public Transaction sell(String symbol, BigDecimal quantity, Player player) {
        if (!hasStock(symbol)) {
            throw new IllegalArgumentException("Stock with symbol " + symbol + " does not exist.");
        }
        Stock stock = getStock(symbol);
        Share share = new Share(stock, quantity, stock.getSalesPrice());
        Transaction transaction = new Sale(share, getWeek());
        transaction.commit(player);
        return transaction;
    }

    public void advance() {
        for (Stock stock : stockMap.values()) {
            BigDecimal newPrice = stock.getSalesPrice().multiply(BigDecimal.valueOf(random.nextDouble(LOWER_CHANGE, UPPER_CHANGE)));
            stock.addNewSalesPrice(newPrice);
        }
        week++;
    }
}

