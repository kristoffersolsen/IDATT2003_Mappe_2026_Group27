package org.example.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.example.model.Stock;
import org.example.model.StockFileRecord;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StockFileService {
    private static final String FILE_HEADER = "#Symbol, Name, Price";
    private static final String PATH_TO_FILES = System.getProperty("user.dir") + "/src/main/resources/data/stocks/";
    /**
     * Writes a list of Stocks to a file with a header description.
     * @param stockFileRecord StockFilerecord to write, containing list of Stocks, filename and description
     * @throws IOException
     */
    public static void writeStocks(StockFileRecord stockFileRecord) throws IOException {
        File dir = new File(PATH_TO_FILES);
        if (!dir.exists()) dir.mkdirs();

        try (CSVWriter writer = new CSVWriter(new FileWriter(PATH_TO_FILES + stockFileRecord.getFileName()))) {
            // Write description as a metadata row
            if (stockFileRecord.getDescription() != null)
                writer.writeNext(new String[]{"metadata", "description", stockFileRecord.getDescription()});
            if (stockFileRecord.getWeek() != -1)
                writer.writeNext(new String[]{"metadata", "week", String.valueOf(stockFileRecord.getWeek())});


            writer.writeNext(new String[]{"Symbol", "Name", "Price"});

            for (Stock stock : stockFileRecord.getStocks()) {
                writer.writeNext(stock.toStringList());
            }
        }
    }

    /**
     * Reads a file containing stocks
     * @param fileName file name to read from
     * @return a StockFileRecord
     * @throws IOException
     */
    public static StockFileRecord readStocks(String fileName) throws IOException {
        List<Stock> stocks = new ArrayList<>();
        String description = null;
        int week = -1;

        try (CSVReader reader = new CSVReader(new FileReader(PATH_TO_FILES + fileName))) {

            // read metadata
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (!line[0].equals("metadata")) break;

                switch (line[1]) {
                    case "description" -> description = line[2];
                    case "week" -> week = Integer.parseInt(line[2]);
                }
            }

            // read the main csv data
            String[] fields;
            while ((fields = reader.readNext()) != null) {
                stocks.add(new Stock(fields[0], fields[1], new BigDecimal(fields[2])));
            }
        } catch (CsvValidationException e) {
            throw new IOException("Failed to parse CSV: " + e.getMessage(), e);
        }

        return new StockFileRecord(stocks, fileName, description, week);
    }
}
