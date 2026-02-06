package org.example;

import java.math.BigDecimal;
import java.util.ArrayList;

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
    Stock(String symbol, String company, BigDecimal salesPrice) {
        this.symbol = symbol;
        this.company = company;
        this.prices.add(salesPrice);
    }

    String getSymbol() {
        return this.symbol;
    }

    String getCompany() {
        return this.company;
    }

    BigDecimal getSalesPrice() {
        return this.prices.get(prices.size() - 1);
    }

    void addNewSalesPrice(BigDecimal price) {
        this.prices.add(price);
    }
}
