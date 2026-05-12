package ntnu.idatt2003.millions.market.view;

import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Chart panel that displays a stock's price history with a navigation toolbar.
 *
 * <p>The toolbar (top) has a context label on the left (week/day of the
 * rightmost visible point) and six control buttons on the right: Back,
 * Forward, Day, Week, Month, All.
 *
 * <p>This is a pure view — all state and event wiring live in
 * {@link ntnu.idatt2003.millions.market.controller.StockPriceChartController}.
 */
public class StockPriceChartView extends VBox {

  private final Label contextLabel = new Label();

  private final Button backButton = new Button("‹");
  private final Button forwardButton = new Button("›");
  private final Button dayButton = new Button("Day");
  private final Button weekButton = new Button("Week");
  private final Button monthButton = new Button("Month");
  private final Button allButton = new Button("All");

  private final NumberAxis xAxis = new NumberAxis();
  private final NumberAxis yAxis = new NumberAxis();
  private final LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

  private Button activeZoomButton;

  /**
   * Builds the toolbar and chart.
   */
  public StockPriceChartView() {
    HBox toolbar = buildToolbar();
    buildChart();
    getChildren().addAll(toolbar, chart);
    VBox.setVgrow(chart, Priority.ALWAYS);
    setActiveZoomButton(allButton);
  }

  private HBox buildToolbar() {
    contextLabel.getStyleClass().addAll("chart-context-label", "font-small");

    backButton.getStyleClass().add("chart-nav-button");
    forwardButton.getStyleClass().add("chart-nav-button");

    for (Button b : List.of(dayButton, weekButton, monthButton, allButton)) {
      b.getStyleClass().add("chart-zoom-button");
    }

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox rightControls = new HBox(4,
        backButton, forwardButton,
        dayButton, weekButton, monthButton, allButton);
    rightControls.setAlignment(Pos.CENTER_RIGHT);

    HBox toolbar = new HBox(contextLabel, spacer, rightControls);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    toolbar.setPadding(new Insets(6, 12, 6, 12));
    toolbar.getStyleClass().add("chart-toolbar");
    return toolbar;
  }

  private void buildChart() {
    xAxis.setAutoRanging(false);
    xAxis.setLabel("");
    xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#555555"));
    yAxis.setLabel("Price ($)");
    yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#555555"));

    chart.setLegendVisible(false);
    chart.setAnimated(false);
    chart.setPadding(new Insets(8, 16, 0, 8));
    chart.getStyleClass().add("stock-chart");
  }

  /**
   * Renders the given prices on the chart with caller-controlled axis formatting.
   *
   * <p>X-values are always 0-based so the data fills the full chart width.
   * The axis is explicitly bounded and labelled according to the active zoom level.
   *
   * @param prices      the data points to display (possibly downsampled)
   * @param tickUnit    spacing between labeled ticks on the x-axis
   * @param xFormatter  converts an x-axis tick value to its display label
   * @param showSymbols whether to draw a dot at each data point
   */
  public void update(List<BigDecimal> prices, double tickUnit,
                     StringConverter<Number> xFormatter, boolean showSymbols) {
    chart.setCreateSymbols(showSymbols);
    chart.getData().clear();
    if (prices.isEmpty()) {
      return;
    }

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    for (int i = 0; i < prices.size(); i++) {
      series.getData().add(new XYChart.Data<>(i, prices.get(i).doubleValue()));
    }
    chart.getData().add(series);

    xAxis.setLowerBound(0);
    xAxis.setUpperBound(prices.size() - 1);
    xAxis.setTickUnit(tickUnit);
    xAxis.setTickLabelFormatter(xFormatter);
  }

  /**
   * Updates the context label in the upper left.
   *
   * @param text the text to display (e.g. "Wk 3 · Wed")
   */
  public void setContext(String text) {
    contextLabel.setText(text);
  }

  /**
   * Sets which zoom button appears active (highlighted).
   *
   * @param btn the button to highlight
   */
  public void setActiveZoomButton(Button btn) {
    if (activeZoomButton != null) {
      activeZoomButton.getStyleClass().remove("chart-zoom-button-active");
    }
    activeZoomButton = btn;
    if (btn != null) {
      btn.getStyleClass().add("chart-zoom-button-active");
    }
  }

  public void setBackEnabled(boolean enabled) {
    backButton.setDisable(!enabled);
  }

  public void setForwardEnabled(boolean enabled) {
    forwardButton.setDisable(!enabled);
  }

  public Button getBackButton() {
    return backButton;
  }

  public Button getForwardButton() {
    return forwardButton;
  }

  public Button getDayButton() {
    return dayButton;
  }

  public Button getWeekButton() {
    return weekButton;
  }

  public Button getMonthButton() {
    return monthButton;
  }

  public Button getAllButton() {
    return allButton;
  }
}
