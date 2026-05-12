package ntnu.idatt2003.millions.order.view;

import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.millions.order.model.LimitOrder;
import ntnu.idatt2003.millions.order.model.OrderType;
import ntnu.idatt2003.millions.shared.util.Format;

/**
 * Slide-in panel that lists the player's pending limit orders.
 *
 * <p>Each row shows the order type, stock symbol, quantity, trigger price,
 * and a cancel button. Confirmed cancellations are forwarded via the
 * {@code onCancel} callback: {@code order -> ...}.
 *
 * <p>The panel is purely presentational; all logic lives in the controller.
 */
public class OrdersPanel extends VBox {

  private final VBox rowContainer = new VBox(8);
  private final Consumer<LimitOrder> onCancel;

  /**
   * Builds the orders panel.
   *
   * @param onCancel callback invoked with the order when the player clicks Cancel
   */
  public OrdersPanel(Consumer<LimitOrder> onCancel) {
    this.onCancel = onCancel;

    Label title = new Label("Pending Orders");
    title.getStyleClass().addAll("font-white", "font-title");

    Label emptyHint = new Label("No pending orders.");
    emptyHint.getStyleClass().addAll("font-grey", "font-small");
    rowContainer.getChildren().add(emptyHint);

    ScrollPane scroll = new ScrollPane(rowContainer);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.getStyleClass().add("transparent-scroll");
    VBox.setVgrow(scroll, Priority.ALWAYS);

    setSpacing(12);
    setPadding(new Insets(20));
    setPrefWidth(300);
    getStyleClass().add("content-dark");
    getChildren().addAll(title, scroll);
  }

  /**
   * Replaces the displayed rows with the given list of pending orders.
   *
   * @param orders current pending orders for the player
   */
  public void setOrders(List<LimitOrder> orders) {
    rowContainer.getChildren().clear();

    if (orders.isEmpty()) {
      Label empty = new Label("No pending orders.");
      empty.getStyleClass().addAll("font-grey", "font-small");
      rowContainer.getChildren().add(empty);
      return;
    }

    for (LimitOrder order : orders) {
      rowContainer.getChildren().add(buildRow(order));
    }
  }

  private VBox buildRow(LimitOrder order) {
    boolean isBuy = order.type() == OrderType.LIMIT_BUY;

    Label typeLabel = new Label(isBuy ? "Limit Buy" : "Limit Sell");
    typeLabel.getStyleClass().add(isBuy ? "tx-type-buy" : "tx-type-sell");

    Label symbolLabel = new Label(order.stockSymbol());
    symbolLabel.getStyleClass().addAll("font-white", "font-content", "font-bold");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox topRow = new HBox(8, typeLabel, symbolLabel, spacer);
    topRow.setAlignment(Pos.CENTER_LEFT);

    Label detail = new Label(
        order.quantity().stripTrailingZeros().toPlainString()
            + " shares @ $" + Format.formatMoney(order.triggerPrice()));
    detail.getStyleClass().addAll("font-grey", "font-small");

    Button cancelBtn = new Button("Cancel");
    cancelBtn.getStyleClass().add("button-light-grey");
    cancelBtn.setOnAction(e -> onCancel.accept(order));

    HBox bottomRow = new HBox(8, detail, spacer, cancelBtn);
    HBox.setHgrow(detail, Priority.ALWAYS);
    bottomRow.setAlignment(Pos.CENTER_LEFT);

    VBox card = new VBox(6, topRow, bottomRow);
    card.setPadding(new Insets(10, 12, 10, 12));
    card.getStyleClass().add("order-card");
    return card;
  }
}
