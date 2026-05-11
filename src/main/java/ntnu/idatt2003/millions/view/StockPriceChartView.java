package ntnu.idatt2003.millions.view;

import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import ntnu.idatt2003.millions.model.Stock;

/**
 * A self-contained line chart component that displays a stock's price history.
 */
public class StockPriceChartView extends LineChart<Number, Number> {

  public StockPriceChartView() {
    super(new NumberAxis(), new NumberAxis());
    configureAxes();
    setLegendVisible(false);
    setAnimated(false);
    setCreateSymbols(true);
    setPadding(new Insets(8, 16, 0, 8));
    getStyleClass().add("stock-chart");
  }

  private void configureAxes() {
    NumberAxis xAxis = (NumberAxis) getXAxis();
    NumberAxis yAxis = (NumberAxis) getYAxis();
    xAxis.setLabel("Week");
    xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#555555"));
    yAxis.setLabel("Price ($)");
    yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#555555"));
  }

  /**
   * Replaces the chart series with price history from the given stock.
   *
   * @param stock     the stock whose history to display
   * @param startWeek the week number corresponding to the first history entry
   */
  public void update(Stock stock, int startWeek) {
    getData().clear();
    XYChart.Series<Number, Number> series = new XYChart.Series<>();

    List<BigDecimal> prices = stock.getHistoricalPrices();
    for (int i = 0; i < prices.size(); i++) {
      series.getData().add(
          new XYChart.Data<>(startWeek + i, prices.get(i).doubleValue()));
    }
    getData().add(series);
  }
}
