package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A stock in a company. A symbol can, for example be "AAPL" for the company Apple.
 */
public class Stock {
    public String symbol;
    private String company;
    private ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>();

    /**
     *
     * @param symbol Set of letters representing the company name.
     * @param company The name of the company.
     * @param salesPrice Last salesPrice of the stock
     */
    public Stock(String symbol, String company, BigDecimal salesPrice) {
        this.symbol = symbol;
        this.company = company;
        this.prices.add(salesPrice);
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getCompany() {
        return this.company;
    }

    public BigDecimal getSalesPrice() {
        return this.prices.get(prices.size() - 1);
    }

    public void addNewSalesPrice(BigDecimal price) {
        this.prices.add(price);
    }

    /**
     * Retrieves all historical prices.
     * @return List of prices
     */
    public List<BigDecimal> getHistoricalPrices() {
        return this.prices;
    }

    /**
     * Retrieves the highest historical price.
     * @return The highest price
     */
    public BigDecimal getHighestPrice() {
        try {
            return this.prices.stream().max(BigDecimal::compareTo).get();
        } catch (NoSuchElementException e) {
            System.out.println("No prices found");
            return null;
        }
    }

    /**
     * Retrieves  the lowest historical price.
     * @return the lowest price
     */
    public BigDecimal getLowestPrice() {
        try {
            return this.prices.stream().min(BigDecimal::compareTo).get();
        } catch (NoSuchElementException e) {
            System.out.println("No prices found");
            return null;
        }
    }

    /**
     * Retrieves the difference between the latest and second latest prices.
     * @return the difference
     */
    public BigDecimal getLatestPriceChange() {
        if (this.prices.size() < 2) {
            System.out.println("Not enough prices for comparison in stock: " + this.symbol);
            return null;
        }
        BigDecimal latestPrice = this.prices.get(this.prices.size() - 1);
        BigDecimal nextLatestPrice = this.prices.get(this.prices.size() - 2);
        return latestPrice.subtract(nextLatestPrice);
    }

    public String[] toStringList() {
        return new String[]{
                this.getSymbol(),
                this.getCompany(),
                this.getSalesPrice().toString()
        };
    }
}
