package org.example.view;

import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.model.Stock;
import org.example.model.transaction.Purchase;
import org.example.model.transaction.Sale;
import org.example.model.transaction.Transaction;
import org.example.util.Format;

/**
 * Center panel that displays stock details, a price-history chart,
 * a buy form, and a mini transaction log for the selected stock.
 *
 * <p>Responsible only for constructing and arranging nodes.
 * All event handling is performed by
 * {@link org.example.controller.StockDetailController}.
 */
public class StockDetailView extends VBox {

  // ---- Header ----
  private final Label symbolLabel = new Label();
  private final Label companyLabel = new Label();
  private final Label priceLabel = new Label();
  private final Label changeAbsLabel = new Label();
  private final Label changePctLabel = new Label();
  private final Button highestPriceButton = new Button();
  private final Button lowestPriceButton = new Button();

  // ---- Chart ----
  private final NumberAxis xAxis = new NumberAxis();
  private final NumberAxis yAxis = new NumberAxis();
  private final LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

  // ---- Buy form ----
  private final Spinner<Integer> quantitySpinner =
      new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100_000, 1));
  private final Button buyButton = new Button("Buy");
  private final Label buyTotalLabel = new Label();
  private final Label buyErrorLabel = new Label();

  // ---- Mini transaction log ----
  private final VBox txRowContainer = new VBox(4);

  /**
   * Builds the stock detail layout.
   */
  public StockDetailView() {
    buildHeader();
    buildChart();
    VBox bottomSection = buildBottomSection();

    setSpacing(0);
    getStyleClass().add("content-light-grey");
    getChildren().addAll(buildHeaderBox(), chart, bottomSection);
    VBox.setVgrow(chart, Priority.ALWAYS);
  }

  private void buildHeader() {
    symbolLabel.getStyleClass().addAll("font-black", "font-title");
    companyLabel.getStyleClass().addAll("font-grey", "font-small");
    priceLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #222222;");
    changeAbsLabel.getStyleClass().addAll("font-small");
    changePctLabel.getStyleClass().addAll("font-small");
    highestPriceButton.getStyleClass().add("stat-button");
    lowestPriceButton.getStyleClass().add("stat-button");
  }

  private HBox buildHeaderBox() {
    VBox nameBlock = new VBox(2, symbolLabel, companyLabel);
    nameBlock.setAlignment(Pos.CENTER_LEFT);

    VBox priceBlock = new VBox(2, priceLabel, changeAbsLabel, changePctLabel);
    priceBlock.setAlignment(Pos.CENTER_LEFT);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox statButtons = new HBox(8, highestPriceButton, lowestPriceButton);
    statButtons.setAlignment(Pos.CENTER_RIGHT);

    HBox header = new HBox(16, nameBlock, priceBlock, spacer, statButtons);
    header.setAlignment(Pos.CENTER_LEFT);
    header.setPadding(new Insets(16, 16, 12, 16));
    header.setStyle("-fx-background-color: #d9d9d9; -fx-border-color: #cccccc;"
        + " -fx-border-width: 0 0 1 0;");
    return header;
  }

  private void buildChart() {
    xAxis.setLabel("Week");
    xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#555555"));
    yAxis.setLabel("Price ($)");
    yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#555555"));

    chart.setLegendVisible(false);
    chart.setAnimated(false);
    chart.setCreateSymbols(true);
    chart.setPadding(new Insets(8, 16, 0, 8));
    chart.setStyle("-fx-background-color: transparent;");
  }

  private VBox buildBottomSection() {
    Label purchaseTitle = new Label("Purchase");
    purchaseTitle.getStyleClass().addAll("font-black", "font-content");
    purchaseTitle.setStyle("-fx-font-weight: bold;");

    Label qtyLabel = new Label("Quantity");
    qtyLabel.getStyleClass().addAll("font-black", "font-small");

    quantitySpinner.setPrefWidth(100);
    quantitySpinner.setEditable(true);

    buyTotalLabel.getStyleClass().addAll("font-grey", "font-small");

    buyErrorLabel.getStyleClass().add("font-small");
    buyErrorLabel.setStyle("-fx-text-fill: #e53935;");
    buyErrorLabel.setVisible(false);
    buyErrorLabel.setManaged(false);

    buyButton.getStyleClass().add("button-light-grey");

    HBox buyRow = new HBox(12, qtyLabel, quantitySpinner, buyButton);
    buyRow.setAlignment(Pos.CENTER_LEFT);

    VBox buyForm = new VBox(8, purchaseTitle, buyRow, buyTotalLabel, buyErrorLabel);
    buyForm.setPadding(new Insets(14, 16, 14, 16));
    buyForm.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 6px;");

    Label txTitle = new Label("Transactions");
    txTitle.getStyleClass().addAll("font-black", "font-content");
    txTitle.setStyle("-fx-font-weight: bold;");

    Label emptyHint = new Label("No transactions yet.");
    emptyHint.getStyleClass().addAll("font-grey", "font-small");
    txRowContainer.getChildren().add(emptyHint);

    ScrollPane txScroll = new ScrollPane(txRowContainer);
    txScroll.setFitToWidth(true);
    txScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    txScroll.setPrefHeight(120);
    txScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    VBox.setVgrow(txScroll, Priority.ALWAYS);

    VBox txPanel = new VBox(6, txTitle, txScroll);
    txPanel.setPadding(new Insets(14, 16, 14, 16));
    txPanel.setStyle("-fx-background-color: #d9d9d9; -fx-background-radius: 6px;");
    HBox.setHgrow(txPanel, Priority.ALWAYS);

    HBox bottomRow = new HBox(16, buyForm, txPanel);
    bottomRow.setPadding(new Insets(14, 16, 14, 16));
    bottomRow.setAlignment(Pos.TOP_LEFT);
    bottomRow.setStyle("-fx-background-color: #e8e8e8;");

    VBox wrapper = new VBox(bottomRow);
    wrapper.setStyle("-fx-background-color: #e8e8e8;");
    return wrapper;
  }

  /**
   * Populates all header and chart data for the given stock.
   *
   * @param stock     the stock to display
   * @param startWeek the week number of the first price entry
   */
  public void setStock(Stock stock, int startWeek) {
    symbolLabel.setText(stock.getSymbol());
    companyLabel.setText(stock.getCompany());
    priceLabel.setText("$" + Format.formatMoney(stock.getSalesPrice()));

    BigDecimal change = stock.getLatestPriceChange();
    String sign = change.signum() >= 0 ? "+" : "";
    changeAbsLabel.setText(sign + Format.formatMoney(change));
    changePctLabel.setText("(" + sign + formatPct(change, stock) + "%)");

    applyChangeStyle(changeAbsLabel, change);
    applyChangeStyle(changePctLabel, change);

    highestPriceButton.setText("Highest Price\n$" + Format.formatMoney(stock.getHighestPrice()));
    lowestPriceButton.setText("Lowest Price\n$" + Format.formatMoney(stock.getLowestPrice()));

    updateChart(stock, startWeek);
    updateBuyTotal(stock.getSalesPrice());
  }

  /**
   * Updates the estimated purchase total shown below the buy form.
   *
   * @param pricePerShare current sales price per share
   */
  public void updateBuyTotal(BigDecimal pricePerShare) {
    int qty = quantitySpinner.getValue();
    BigDecimal gross = pricePerShare.multiply(BigDecimal.valueOf(qty));
    BigDecimal commission = gross.multiply(BigDecimal.valueOf(0.005));
    buyTotalLabel.setText("Est. total: $" + Format.formatMoney(gross.add(commission))
        + "  (incl. 0.5% commission)");
  }

  /**
   * Shows or hides a buy-error message beneath the form.
   *
   * @param message the error text, or null/empty to hide
   */
  public void showBuyError(String message) {
    if (message == null || message.isBlank()) {
      buyErrorLabel.setVisible(false);
      buyErrorLabel.setManaged(false);
    } else {
      buyErrorLabel.setText(message);
      buyErrorLabel.setVisible(true);
      buyErrorLabel.setManaged(true);
    }
  }

  /**
   * Replaces the mini transaction log with transactions for the current stock.
   *
   * <p>Buy rows show purchase price. Sell rows additionally show sale price
   * and realised gain or loss.
   *
   * @param transactions transactions pre-filtered to the current stock
   */
  public void setTransactions(List<Transaction> transactions) {
    txRowContainer.getChildren().clear();

    if (transactions.isEmpty()) {
      Label empty = new Label("No transactions yet.");
      empty.getStyleClass().addAll("font-grey", "font-small");
      txRowContainer.getChildren().add(empty);
      return;
    }

    for (int i = transactions.size() - 1; i >= 0; i--) {
      txRowContainer.getChildren().add(buildTxRow(transactions.get(i)));
    }
  }

  private VBox buildTxRow(Transaction tx) {
    boolean isBuy = tx instanceof Purchase;

    Label typeLabel = new Label(isBuy ? "Buy" : "Sell");
    typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: "
        + (isBuy ? "#4caf50" : "#f44336") + ";");
    typeLabel.setPrefWidth(28);

    String qtyStr = tx.getShare().quantity().stripTrailingZeros().toPlainString();

    if (isBuy) {
      Label detail = new Label(
          qtyStr + " x $" + Format.formatMoney(tx.getShare().purchasePrice()));
      detail.getStyleClass().addAll("font-black", "font-small");
      HBox.setHgrow(detail, Priority.ALWAYS);

      HBox row = new HBox(6, typeLabel, detail, weekLabel(tx));
      row.setAlignment(Pos.CENTER_LEFT);
      return wrap(row);
    }

    Sale sale = (Sale) tx;
    Label detail = new Label(
        qtyStr + " x $" + Format.formatMoney(sale.getSalePrice()));
    detail.getStyleClass().addAll("font-black", "font-small");
    HBox.setHgrow(detail, Priority.ALWAYS);

    HBox topRow = new HBox(6, typeLabel, detail, weekLabel(tx));
    topRow.setAlignment(Pos.CENTER_LEFT);

    BigDecimal gain = sale.getRealisedGain();
    boolean positive = gain.signum() >= 0;
    String sign = positive ? "+" : "";
    Label gainLabel = new Label(
        "avg $" + Format.formatMoney(sale.getShare().purchasePrice())
            + "   " + sign + "$" + Format.formatMoney(gain.abs()));
    gainLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: "
        + (positive ? "#4caf50" : "#f44336") + ";");

    return wrap(topRow, gainLabel);
  }

  private Label weekLabel(Transaction tx) {
    Label wk = new Label("Wk " + tx.getWeek());
    wk.getStyleClass().addAll("font-grey", "font-small");
    return wk;
  }

  /**
   * Wraps the given nodes in a VBox with small spacing.
   *
   * @param nodes one or more nodes to include
   * @return the assembled wrapper
   */
  private VBox wrap(Node... nodes) {
    VBox box = new VBox(2);
    box.getChildren().addAll(nodes);
    return box;
  }

  private void updateChart(Stock stock, int startWeek) {
    chart.getData().clear();
    XYChart.Series<Number, Number> series = new XYChart.Series<>();

    List<BigDecimal> prices = stock.getHistoricalPrices();
    for (int i = 0; i < prices.size(); i++) {
      series.getData().add(
          new XYChart.Data<>(startWeek + i, prices.get(i).doubleValue()));
    }
    chart.getData().add(series);
  }

  private void applyChangeStyle(Label label, BigDecimal change) {
    label.getStyleClass().removeAll("price-up", "price-down", "price-flat");
    if (change.signum() > 0) {
      label.getStyleClass().add("price-up");
    } else if (change.signum() < 0) {
      label.getStyleClass().add("price-down");
    } else {
      label.getStyleClass().add("price-flat");
    }
  }

  private String formatPct(BigDecimal change, Stock stock) {
    List<BigDecimal> prices = stock.getHistoricalPrices();
    if (prices.size() < 2) {
      return "0.00";
    }
    BigDecimal prev = prices.get(prices.size() - 2);
    if (prev.signum() == 0) {
      return "0.00";
    }
    return String.format("%.2f",
        change.divide(prev, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)).doubleValue());
  }

  public Spinner<Integer> getQuantitySpinner() {
    return quantitySpinner;
  }

  public Button getBuyButton() {
    return buyButton;
  }
}