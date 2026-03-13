package org.example.model;

import org.example.service.StockFileService;

import java.io.IOException;
import java.util.List;

/**
 * Holds information about a read/write of stocks from a file.
 */
public class StockFileRecord {

    List<Stock> stocks;
    String fileName;
    String description;
    int week;

    public StockFileRecord(List<Stock> stocks, String fileName) {
        this.stocks = stocks;
        this.fileName = fileName;
        this.description = null;
        this.week = -1;
    }

    public StockFileRecord(List<Stock> stocks, String fileName, String description) {
        this.stocks = stocks;
        this.fileName = fileName;
        this.description = description;
        this.week = -1;
    }

    public StockFileRecord(List<Stock> stocks, String fileName, String description, int week) {
        this.stocks = stocks;
        this.fileName = fileName;
        this.description = description;
        this.week = week;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getDescription() {
        return this.description;
    }

    public int getWeek() {
        return this.week;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public void writeToFile() {
        try {
            StockFileService.writeStocks(this);
        } catch (IOException e) {
            System.err.println("Error writing stockFileRecord to file: " + e.getMessage());
        }
    }
}
