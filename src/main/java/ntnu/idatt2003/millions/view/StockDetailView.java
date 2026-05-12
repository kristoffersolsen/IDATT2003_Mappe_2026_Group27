package ntnu.idatt2003.millions.view;

import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.millions.controller.StockDetailController;
import ntnu.idatt2003.millions.model.Stock;
import ntnu.idatt2003.millions.model.transaction.Dividend;
import ntnu.idatt2003.millions.model.transaction.Purchase;
import ntnu.idatt2003.millions.model.transaction.PurchaseCalculator;
import ntnu.idatt2003.millions.model.transaction.Sale;
import ntnu.idatt2003.millions.model.transaction.Transaction;
import ntnu.idatt2003.millions.util.Format;

/**
 * Center panel that displays stock details, a price-history chart,
 * a buy form, and a mini transaction log for the selected stock.
 *
 * <p>Responsible only for constructing and arranging nodes.
 * All event handling is performed by
 * {@link StockDetailController}.
 */
public class StockDetailView extends VBox {

  // ---- Header ----
  private final Label symbolLabel = new Label();
  private final Label companyLabel = new Label();
  private final Label priceLabel = new Label();
  private final Label changeAbsLabel = new Label();
  private final Label changePctLabel = new Label();
  private final Label dividendLabel = new Label();
  private final Button highestPriceButton = new Button();
  private final Button lowestPriceButton = new Button();

  // ---- Chart ----
  private final StockPriceChartView chartView = new StockPriceChartView();

  // ---- Buy form ----
  private final Spinner<Integer> quantitySpinner =
      new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100_000, 1));
  private final Button buyButton = new Button("Buy");
  private final Button limitBuyToggle = new Button("Limit Buy");
  private final TextField triggerPriceField = new TextField();
  private final VBox triggerPriceSection = new VBox();
  private final Label buyTotalLabel = new Label();
  private final Label buyErrorLabel = new Label();

  // ---- Mini transaction log ----
  private final VBox txRowContainer = new VBox(4);

  /**
   * Builds the stock detail layout.
   */
  public StockDetailView() {
    buildHeader();
    VBox bottomSection = buildBottomSection();

    setSpacing(0);
    getStyleClass().add("content-light-grey");
    getChildren().addAll(buildHeaderBox(), chartView, bottomSection);
    VBox.setVgrow(chartView, Priority.ALWAYS);
  }

  private void buildHeader() {
    symbolLabel.getStyleClass().addAll("font-black", "font-title");
    companyLabel.getStyleClass().addAll("font-grey", "font-small");
    priceLabel.getStyleClass().add("stock-price-label");
    changeAbsLabel.getStyleClass().addAll("font-small");
    changePctLabel.getStyleClass().addAll("font-small");
    dividendLabel.getStyleClass().addAll("font-grey", "font-small");
    dividendLabel.setVisible(false);
    dividendLabel.setManaged(false);
    highestPriceButton.getStyleClass().add("stat-button");
    lowestPriceButton.getStyleClass().add("stat-button");
  }

  private HBox buildHeaderBox() {
    VBox nameBlock = new VBox(2, symbolLabel, companyLabel);
    nameBlock.setAlignment(Pos.CENTER_LEFT);

    VBox priceBlock = new VBox(2, priceLabel, changeAbsLabel, changePctLabel, dividendLabel);
    priceBlock.setAlignment(Pos.CENTER_LEFT);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox statButtons = new HBox(8, highestPriceButton, lowestPriceButton);
    statButtons.setAlignment(Pos.CENTER_RIGHT);

    HBox header = new HBox(16, nameBlock, priceBlock, spacer, statButtons);
    header.setAlignment(Pos.CENTER_LEFT);
    header.setPadding(new Insets(16, 16, 12, 16));
    header.getStyleClass().add("stock-header");
    return header;
  }

  private VBox buildBottomSection() {
    Label purchaseTitle = new Label("Purchase");
    purchaseTitle.getStyleClass().addAll("font-black", "font-content", "section-title");

    Label qtyLabel = new Label("Quantity");
    qtyLabel.getStyleClass().addAll("font-black", "font-small");

    quantitySpinner.setPrefWidth(100);
    quantitySpinner.setEditable(true);

    buyTotalLabel.getStyleClass().addAll("font-grey", "font-small");

    buyErrorLabel.getStyleClass().addAll("font-small", "error-text");
    buyErrorLabel.setVisible(false);
    buyErrorLabel.setManaged(false);

    buyButton.getStyleClass().add("button-light-grey");
    limitBuyToggle.getStyleClass().add("button-light-grey");

    HBox buyRow = new HBox(12, qtyLabel, quantitySpinner, buyButton, limitBuyToggle);
    buyRow.setAlignment(Pos.CENTER_LEFT);

    Label triggerLabel = new Label("Trigger price ($)");
    triggerLabel.getStyleClass().addAll("font-black", "font-small");
    triggerPriceField.setPromptText("e.g. 150.00");
    triggerPriceField.setPrefWidth(120);
    triggerPriceSection.setSpacing(4);
    triggerPriceSection.getChildren().addAll(triggerLabel, triggerPriceField);
    triggerPriceSection.setVisible(false);
    triggerPriceSection.setManaged(false);

    VBox buyForm = new VBox(8, purchaseTitle, buyRow, triggerPriceSection, buyTotalLabel,
        buyErrorLabel);
    buyForm.setPadding(new Insets(14, 16, 14, 16));
    buyForm.getStyleClass().add("buy-form");

    Label txTitle = new Label("Transactions");
    txTitle.getStyleClass().addAll("font-black", "font-content", "section-title");

    Label emptyHint = new Label("No transactions yet.");
    emptyHint.getStyleClass().addAll("font-grey", "font-small");
    txRowContainer.getChildren().add(emptyHint);

    ScrollPane txScroll = new ScrollPane(txRowContainer);
    txScroll.setFitToWidth(true);
    txScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    txScroll.setPrefHeight(120);
    txScroll.getStyleClass().add("transparent-scroll");
    VBox.setVgrow(txScroll, Priority.ALWAYS);

    VBox txPanel = new VBox(6, txTitle, txScroll);
    txPanel.setPadding(new Insets(14, 16, 14, 16));
    txPanel.getStyleClass().add("tx-panel");
    HBox.setHgrow(txPanel, Priority.ALWAYS);

    HBox bottomRow = new HBox(16, buyForm, txPanel);
    bottomRow.setPadding(new Insets(14, 16, 14, 16));
    bottomRow.setAlignment(Pos.TOP_LEFT);
    bottomRow.getStyleClass().add("bottom-section");

    VBox wrapper = new VBox(bottomRow);
    wrapper.getStyleClass().add("bottom-section");
    return wrapper;
  }

  /**
   * Populates all header data for the given stock.
   *
   * <p>Chart updates are handled separately by {@link
   * ntnu.idatt2003.millions.controller.StockPriceChartController}.
   *
   * @param stock the stock to display
   */
  public void setStock(Stock stock) {
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

    if (stock.paysDividend()) {
      dividendLabel.setText("Dividend: $" + Format.formatMoney(stock.getDividendPerShare())
          + " / " + stock.getDividendIntervalHours() + "h");
      dividendLabel.setVisible(true);
      dividendLabel.setManaged(true);
    } else {
      dividendLabel.setVisible(false);
      dividendLabel.setManaged(false);
    }

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
    BigDecimal commission = gross.multiply(PurchaseCalculator.COMMISSION_RATE);
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
    if (tx instanceof Dividend div) {
      return buildDividendTxRow(div);
    }
    if (tx instanceof Sale sale) {
      return buildSaleTxRow(sale);
    }
    return buildPurchaseTxRow((Purchase) tx);
  }

  private VBox buildPurchaseTxRow(Purchase tx) {
    Label typeLabel = new Label("Buy");
    typeLabel.getStyleClass().add("tx-type-buy");
    typeLabel.setPrefWidth(28);

    String qtyStr = tx.getShare().quantity().stripTrailingZeros().toPlainString();
    Label detail = new Label(qtyStr + " x $" + Format.formatMoney(tx.getShare().purchasePrice()));
    detail.getStyleClass().addAll("font-black", "font-small");
    HBox.setHgrow(detail, Priority.ALWAYS);

    HBox row = new HBox(6, typeLabel, detail, weekLabel(tx));
    row.setAlignment(Pos.CENTER_LEFT);
    return wrap(row);
  }

  private VBox buildSaleTxRow(Sale sale) {
    Label typeLabel = new Label("Sell");
    typeLabel.getStyleClass().add("tx-type-sell");
    typeLabel.setPrefWidth(28);

    String qtyStr = sale.getShare().quantity().stripTrailingZeros().toPlainString();
    Label detail = new Label(qtyStr + " x $" + Format.formatMoney(sale.getSalePrice()));
    detail.getStyleClass().addAll("font-black", "font-small");
    HBox.setHgrow(detail, Priority.ALWAYS);

    HBox topRow = new HBox(6, typeLabel, detail, weekLabel(sale));
    topRow.setAlignment(Pos.CENTER_LEFT);

    BigDecimal gain = sale.getRealisedGain();
    boolean positive = gain.signum() >= 0;
    String sign = positive ? "+" : "";
    Label gainLabel = new Label(
        "avg $" + Format.formatMoney(sale.getShare().purchasePrice())
            + "   " + sign + "$" + Format.formatMoney(gain.abs()));
    gainLabel.getStyleClass().add(positive ? "gain-positive" : "gain-negative");

    return wrap(topRow, gainLabel);
  }

  private VBox buildDividendTxRow(Dividend div) {
    Label typeLabel = new Label("Div");
    typeLabel.getStyleClass().add("tx-type-dividend");
    typeLabel.setPrefWidth(28);

    Label detail = new Label(
        "$" + Format.formatMoney(div.getDividendPerShare()) + "/share  +"
            + "$" + Format.formatMoney(div.getTotalPaid()));
    detail.getStyleClass().addAll("font-black", "font-small");
    HBox.setHgrow(detail, Priority.ALWAYS);

    HBox row = new HBox(6, typeLabel, detail, weekLabel(div));
    row.setAlignment(Pos.CENTER_LEFT);
    return wrap(row);
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

  /**
   * Shows or hides the trigger-price section used for limit buy orders.
   *
   * <p>When visible, the section reveals a text field where the player
   * enters the trigger price before placing a limit buy order.
   *
   * @param visible true to reveal the trigger-price field, false to hide it
   */
  public void setLimitBuyMode(boolean visible) {
    triggerPriceSection.setVisible(visible);
    triggerPriceSection.setManaged(visible);
    buyButton.setText(visible ? "Place limit buy" : "Buy");
  }

  public StockPriceChartView getChartView() {
    return chartView;
  }

  public Spinner<Integer> getQuantitySpinner() {
    return quantitySpinner;
  }

  public Button getBuyButton() {
    return buyButton;
  }

  public Button getLimitBuyToggle() {
    return limitBuyToggle;
  }

  public TextField getTriggerPriceField() {
    return triggerPriceField;
  }
}