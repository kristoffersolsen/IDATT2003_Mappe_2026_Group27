package org.example.service;

import org.example.model.Stock;
import org.example.model.StockFileRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockFileServiceTest {
    List<Stock> stocks;

    StockFileServiceTest() {
        stocks = new ArrayList<>();
        stocks.add(new Stock("sym1", "company1", BigDecimal.valueOf(1)));
        stocks.add(new Stock("sym2", "company2", BigDecimal.valueOf(2)));
        stocks.add(new Stock("sym3", "company3", BigDecimal.valueOf(3)));
    }

    @Test
    void readWriteStocks() {
        try {
            StockFileService.writeStocks(new StockFileRecord(stocks, "testStocks.csv"));

            StockFileRecord stockFileRecord = StockFileService.readStocks("testStocks.csv");

            List<Stock> readStocks = stockFileRecord.getStocks();
            assertEquals(readStocks.size(), stocks.size());
            for (int i = 0; i < readStocks.size(); i++) {
                assertEquals(readStocks.get(i).getSymbol(), stocks.get(i).getSymbol());
                assertEquals(readStocks.get(i).getCompany(), stocks.get(i).getCompany());
                assertEquals(readStocks.get(i).getSalesPrice(), stocks.get(i).getSalesPrice());
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }
    }

    @Test
    void metadata() {
        try {
            StockFileService.writeStocks(new StockFileRecord(stocks, "testmetadata.csv", "description here"));
            StockFileRecord stocksRecord = StockFileService.readStocks("testmetadata.csv");
            assertEquals("description here", stocksRecord.getDescription());

            StockFileService.writeStocks(new StockFileRecord(stocks, "testmetadata.csv", "description here", 5));
            stocksRecord = StockFileService.readStocks("testmetadata.csv");
            assertEquals("description here", stocksRecord.getDescription());
            assertEquals(5, stocksRecord.getWeek());


        } catch (IOException e) {
            System.err.println(e.getMessage());
            fail();
        }
    }

}