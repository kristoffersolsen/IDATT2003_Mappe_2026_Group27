package org.example.service;

import org.example.model.transaction.Purchase;
import org.example.model.transaction.Sale;
import org.example.model.*;
import org.example.model.transaction.Transaction;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Exchange {

    private final String name;
    private int week;
    private final Map<String, Stock> stockMap;
    private final StockFileRecord stockFileRecord;

    private final Random random;

    private final double LOWER_CHANGE = -0.1; // -10%
    private final double UPPER_CHANGE = 0.1;  // +10%

    /**
     * Constructor with just a name. Creates an empty stockMap and starts from week 1.
     * @param name
     */
    public Exchange(String name) {
        this.name = name;
        this.stockMap = new HashMap<>();
        this.week = 1;
        this.random = new Random();
        this.stockFileRecord = null;
    }

    public Exchange(String name, int week, List<Stock> stocks) {
        this.name = name;
        this.stockMap = new HashMap<>();
        for (Stock stock : stocks) {
            this.stockMap.put(stock.getSymbol(), stock);
        }
        this.week = week;
        this.random = new Random();
        this.stockFileRecord = null;
    }

    public Exchange(String name, StockFileRecord stockFileRecord) {
        this.name = name;
        this.stockMap = new HashMap<>();
        for (Stock stock : stockFileRecord.getStocks()) {
            this.stockMap.put(stock.getSymbol(), stock);
        }
        this.week = stockFileRecord.getWeek();
        this.random = new Random();
        this.stockFileRecord = stockFileRecord;
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

    public StockFileRecord getStockFileRecord() {
        return stockFileRecord;
    }

    /**
     * Find stocks based on a search term. searches through company names.
     * @param searchTerm a part of a company name
     * @return list of Stocks
     */
    public List<Stock> findStocks(String searchTerm) {
        return stockMap.values().stream()
                .filter(stock -> stock.getCompany().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();
    }

    /**
     * Perform a buy transaction
     * @param symbol The symbol of the Stock to buy
     * @param quantity The quantity of the Stock to buy
     * @param player The player that buys
     * @return The transaction
     */
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

    /**
     * Peform a sell transaction
     * @param symbol The symbol of the Stock to sell
     * @param quantity The quantity of the Stock to sell
     * @param player The player that sells
     * @return The transaction
     */
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

    /**
     * Advance to the next week. Adds a randomly changed new price to all Stocks.
     */
    public void advance() {
        for (Stock stock : stockMap.values()) {
            BigDecimal newPrice = stock.getSalesPrice().multiply(BigDecimal.valueOf(random.nextDouble(LOWER_CHANGE, UPPER_CHANGE)));
            stock.addNewSalesPrice(newPrice);
        }
        week++;
    }

    /**
     * Retrieves the top gainers, sorted on the latest price change.
     * @param limit How many gainers to list
     * @return list of Stocks
     */
    public List<Stock> getGainers(int limit) {
        return this.stockMap.values().stream()
                .filter(stock -> stock.getSalesPrice().signum() > 0)
                .sorted(Comparator.comparing(Stock::getLatestPriceChange))
                .limit(limit)
                .toList();
    }

    /**
     * Retrieves the bottom losers, reverse sorted on the latest price change.
     * @param limit How many losers to list
     * @return list of Stocks
     */
    public List<Stock> getLosers(int limit) {
        return this.stockMap.values().stream()
                .filter(stock -> stock.getSalesPrice().signum() < 0)
                .sorted(Comparator.comparing(Stock::getLatestPriceChange).reversed())
                .limit(limit)
                .toList();
    }

    public Status getStatus(Player player) {
        return player.getStatus(week);
    }

    public void saveState() {
        this.stockFileRecord.setStocks((List<Stock>) this.stockMap.values());
        this.stockFileRecord.writeToFile();
    }
}

