package ntnu.idatt2003.millions.player.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.millions.player.model.Share;
import ntnu.idatt2003.millions.transaction.model.SaleCalculator;
import ntnu.idatt2003.millions.shared.util.Format;

/**
 * Slide-in panel that lists every position in the player's portfolio.
 *
 * <p>Each row shows symbol, quantity, weighted-average cost, current P&L,
 * and a Sell button. Pressing Sell opens an overlay modal inside this panel
 * where the player can choose a partial or full quantity before confirming.
 *
 * <p>The panel is purely presentational. Confirmed sells are forwarded via
 * the {@code onSell} callback: {@code (share, quantityToSell) -> ...}.
 */
public class PortfolioPanel extends VBox {

  private final BiConsumer<Share, BigDecimal> onSell;
  private final VBox rowContainer = new VBox(8);
  private final StackPane root = new StackPane();
  private VBox modalOverlay;

  /**
   * Builds the portfolio panel.
   *
   * @param onSell callback invoked with (share, quantityToSell) when the
   *               player confirms a sell in the modal
   */
  public PortfolioPanel(BiConsumer<Share, BigDecimal> onSell) {
    this.onSell = onSell;

    Label title = new Label("Portfolio");
    title.getStyleClass().addAll("font-white", "font-title");

    ScrollPane scroll = new ScrollPane(rowContainer);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.getStyleClass().add("transparent-scroll");
    VBox.setVgrow(scroll, Priority.ALWAYS);

    VBox content = new VBox(12, title, scroll);
    content.setPadding(new Insets(20));
    VBox.setVgrow(content, Priority.ALWAYS);

    root.getChildren().add(content);
    VBox.setVgrow(root, Priority.ALWAYS);

    getChildren().add(root);
    getStyleClass().add("content-dark");
    setPrefWidth(300);
    VBox.setVgrow(this, Priority.ALWAYS);
  }

  /**
   * Replaces the displayed rows with the current portfolio positions.
   *
   * @param shares current portfolio shares
   */
  public void setShares(List<Share> shares) {
    rowContainer.getChildren().clear();
    closeModal();

    if (shares.isEmpty()) {
      Label empty = new Label("No shares held.");
      empty.getStyleClass().addAll("font-grey", "font-small");
      rowContainer.getChildren().add(empty);
      return;
    }

    for (Share share : shares) {
      rowContainer.getChildren().add(buildRow(share));
    }
  }

  private VBox buildRow(Share share) {
    BigDecimal currentPrice = share.stock().getSalesPrice();
    BigDecimal avgCost = share.purchasePrice();
    BigDecimal qty = share.quantity();

    BigDecimal pnlPerShare = currentPrice.subtract(avgCost);
    BigDecimal pnlTotal = pnlPerShare.multiply(qty);
    BigDecimal pnlPct = avgCost.signum() == 0 ? BigDecimal.ZERO
        : pnlPerShare.divide(avgCost, 4, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100));

    boolean positive = pnlTotal.signum() >= 0;
    String sign = positive ? "+" : "";

    Label symbolLabel = new Label(share.stock().getSymbol());
    symbolLabel.getStyleClass().addAll("font-white", "font-content", "font-bold");

    Label companyLabel = new Label(share.stock().getCompany());
    companyLabel.getStyleClass().addAll("font-grey", "font-small");

    VBox nameBlock = new VBox(1, symbolLabel, companyLabel);

    Label pnlBadge = new Label(
        sign + Format.formatMoney(pnlTotal)
            + "  (" + sign + String.format("%.2f", pnlPct.doubleValue()) + "%)");
    pnlBadge.getStyleClass().add(positive ? "pnl-badge-positive" : "pnl-badge-negative");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox topRow = new HBox(8, nameBlock, spacer, pnlBadge);
    topRow.setAlignment(Pos.CENTER_LEFT);

    Label detailLabel = new Label(
        qty.stripTrailingZeros().toPlainString()
            + " shares · avg $" + Format.formatMoney(avgCost)
            + "  ·  now $" + Format.formatMoney(currentPrice));
    detailLabel.getStyleClass().addAll("font-grey", "font-small");

    BigDecimal positionValue = currentPrice.multiply(qty);
    Label valueLabel = new Label("$" + Format.formatMoney(positionValue));
    valueLabel.getStyleClass().addAll("font-white", "font-small");

    Region spacer2 = new Region();
    HBox.setHgrow(spacer2, Priority.ALWAYS);

    Button sellBtn = new Button("Sell");
    sellBtn.getStyleClass().add("button-light-grey");
    sellBtn.setOnAction(e -> openModal(share));

    HBox bottomRow = new HBox(8, valueLabel, spacer2, sellBtn);
    bottomRow.setAlignment(Pos.CENTER_LEFT);

    VBox card = new VBox(6, topRow, detailLabel, bottomRow);
    card.setPadding(new Insets(10, 12, 10, 12));
    card.getStyleClass().add("portfolio-card");
    return card;
  }

  private void openModal(Share share) {
    closeModal();

    BigDecimal currentPrice = share.stock().getSalesPrice();
    BigDecimal avgCost = share.purchasePrice();
    int maxQty = share.quantity().intValue();

    SpinnerValueFactory.IntegerSpinnerValueFactory spinnerFactory =
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxQty, maxQty);
    Spinner<Integer> spinner = new Spinner<>(spinnerFactory);
    spinner.setEditable(true);
    spinner.setPrefWidth(80);

    Slider slider = new Slider(1, maxQty, maxQty);
    slider.setSnapToTicks(false);
    slider.setShowTickMarks(maxQty > 1);
    HBox.setHgrow(slider, Priority.ALWAYS);

    Label maxLabel = new Label("/ " + maxQty);
    maxLabel.getStyleClass().addAll("font-grey", "font-small");

    spinner.valueProperty().addListener((obs, o, n) -> {
      if (n != null) {
        slider.setValue(n);
      }
    });
    slider.valueProperty().addListener((obs, o, n) ->
        spinnerFactory.setValue((int) Math.round(n.doubleValue())));

    Label grossLabel = new Label();
    Label commLabel = new Label();
    Label taxLabel = new Label();
    Label netLabel = new Label();
    Label pnlLabel = new Label();

    grossLabel.getStyleClass().addAll("font-white", "font-small");
    commLabel.getStyleClass().addAll("font-grey", "font-small");
    taxLabel.getStyleClass().addAll("font-grey", "font-small");
    netLabel.getStyleClass().addAll("font-white", "font-small", "net-label");
    pnlLabel.getStyleClass().add("font-small");

    Runnable updateSummary = () -> {
      int qty = spinnerFactory.getValue();
      BigDecimal q = BigDecimal.valueOf(qty);
      BigDecimal gross = currentPrice.multiply(q);
      BigDecimal commission = gross.multiply(SaleCalculator.COMMISSION_RATE);
      BigDecimal costBasis = avgCost.multiply(q);
      BigDecimal gain = gross.subtract(commission).subtract(costBasis);
      BigDecimal tax = gain.signum() > 0
          ? gain.multiply(SaleCalculator.TAX_RATE)
          : BigDecimal.ZERO;
      BigDecimal net = gross.subtract(commission).subtract(tax);

      grossLabel.setText("$" + Format.formatMoney(gross));
      commLabel.setText("−$" + Format.formatMoney(commission));
      taxLabel.setText("−$" + Format.formatMoney(tax));
      netLabel.setText("$" + Format.formatMoney(net));

      BigDecimal pnlTotal = currentPrice.subtract(avgCost).multiply(q);
      boolean pos = pnlTotal.signum() >= 0;
      String s = pos ? "+" : "";
      pnlLabel.setText(s + "$" + Format.formatMoney(pnlTotal.abs()));
      pnlLabel.getStyleClass().removeAll("pnl-positive", "pnl-negative");
      pnlLabel.getStyleClass().add(pos ? "pnl-positive" : "pnl-negative");
    };
    spinnerFactory.valueProperty().addListener((obs, o, n) -> updateSummary.run());
    updateSummary.run();

    VBox posInfo = new VBox(4,
        infoRow("Held", share.quantity().stripTrailingZeros().toPlainString() + " shares"),
        infoRow("Avg cost", "$" + Format.formatMoney(avgCost)),
        infoRow("Current price", "$" + Format.formatMoney(currentPrice)));
    posInfo.setPadding(new Insets(10, 12, 10, 12));
    posInfo.getStyleClass().add("modal-info-panel");

    VBox breakdown = new VBox(5,
        breakdownRow("Gross proceeds", grossLabel),
        breakdownRow("Commission (1%)", commLabel),
        breakdownRow("Capital gains tax", taxLabel),
        divider(),
        breakdownRow("You receive", netLabel),
        breakdownRow("Realised P&L", pnlLabel));
    breakdown.setPadding(new Insets(10, 12, 10, 12));
    breakdown.getStyleClass().add("modal-info-panel");

    Button cancelBtn = new Button("Cancel");
    cancelBtn.getStyleClass().add("button-light-grey");
    cancelBtn.setPrefWidth(105);
    cancelBtn.setOnAction(e -> closeModal());

    Button confirmBtn = new Button("Confirm sell");
    confirmBtn.setPrefWidth(125);
    confirmBtn.getStyleClass().add("button-danger");
    confirmBtn.setOnAction(e -> {
      BigDecimal qty = BigDecimal.valueOf(spinnerFactory.getValue());
      closeModal();
      onSell.accept(share, qty);
    });

    HBox buttonRow = new HBox(8, cancelBtn, confirmBtn);
    buttonRow.setAlignment(Pos.CENTER_RIGHT);

    Label modalTitle = new Label("Sell " + share.stock().getSymbol());
    modalTitle.getStyleClass().addAll("font-white", "font-title");

    Label modalSub = new Label(share.stock().getCompany());
    modalSub.getStyleClass().addAll("font-grey", "font-small");

    Label qtyLabel = new Label("Quantity to sell");
    qtyLabel.getStyleClass().addAll("font-grey", "font-small");

    HBox qtyRow = new HBox(8, spinner, slider, maxLabel);
    qtyRow.setAlignment(Pos.CENTER_LEFT);

    VBox card = new VBox(14,
        new VBox(2, modalTitle, modalSub),
        posInfo,
        new VBox(6, qtyLabel, qtyRow),
        breakdown,
        buttonRow);
    card.setPadding(new Insets(20));
    card.setMaxWidth(270);
    card.getStyleClass().add("sell-modal-card");

    modalOverlay = new VBox(card);
    modalOverlay.setAlignment(Pos.CENTER);
    modalOverlay.getStyleClass().add("modal-overlay");
    modalOverlay.setOnMouseClicked(e -> {
      if (e.getTarget() == modalOverlay) {
        closeModal();
      }
    });

    root.getChildren().add(modalOverlay);
  }

  private void closeModal() {
    if (modalOverlay != null) {
      root.getChildren().remove(modalOverlay);
      modalOverlay = null;
    }
  }

  private HBox infoRow(String labelText, String valueText) {
    Label lbl = new Label(labelText);
    lbl.getStyleClass().addAll("font-grey", "font-small");
    lbl.setPrefWidth(110);

    Label val = new Label(valueText);
    val.getStyleClass().addAll("font-white", "font-small");

    HBox row = new HBox(8, lbl, val);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
  }

  private HBox breakdownRow(String labelText, Label valueLabel) {
    Label lbl = new Label(labelText);
    lbl.getStyleClass().addAll("font-grey", "font-small");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox row = new HBox(8, lbl, spacer, valueLabel);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
  }

  private Region divider() {
    Region d = new Region();
    d.setPrefHeight(1);
    d.getStyleClass().add("modal-divider");
    return d;
  }
}