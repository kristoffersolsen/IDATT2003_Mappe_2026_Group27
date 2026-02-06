package org.example;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Stock {
    public String symbol;
    private String company;
    private ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>();

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

    BigDecimal getSalesPrice(BigDecimal price) {
        return this.prices.get(prices.size() - 1);
    }

    void addNewSalesPrice(BigDecimal price) {
        this.prices.add(price);
    }
}
