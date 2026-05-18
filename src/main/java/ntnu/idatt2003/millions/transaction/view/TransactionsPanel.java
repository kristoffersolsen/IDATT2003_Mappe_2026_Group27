package ntnu.idatt2003.millions.transaction.view;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.millions.transaction.model.Dividend;
import ntnu.idatt2003.millions.transaction.model.Purchase;
import ntnu.idatt2003.millions.transaction.model.Sale;
import ntnu.idatt2003.millions.transaction.model.Transaction;
import ntnu.idatt2003.millions.shared.util.Format;

/**
 * Slide-in panel that lists the player's committed transaction history.
 *
 * <p>Buy rows show: symbol, quantity, purchase price per share, week.
 * Sell rows additionally show the sale price per share and the realised
 * gain or loss for the position.
 *
 * <p>Rows are ordered most-recent first and paginated at
 * {@value #PAGE_SIZE} entries per page.
 */
public class TransactionsPanel extends VBox {

  /** Number of transaction rows shown per page. */
  public static final int PAGE_SIZE = 12;

  private final VBox rowContainer = new VBox(6);
  private final Label pageLabel = new Label("Page 1 of 1");
  private final Button prevButton = new Button("←");
  private final Button nextButton = new Button("→");

  private List<Transaction> allTransactions = Collections.emptyList();
  private int currentPage = 0;

  /**
   * Builds the transactions panel.
   */
  public TransactionsPanel() {
    Label title = new Label("Transactions");
    title.getStyleClass().addAll("font-white", "font-title");

    ScrollPane scroll = new ScrollPane(rowContainer);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.getStyleClass().add("transparent-scroll");
    VBox.setVgrow(scroll, Priority.ALWAYS);

    setSpacing(12);
    setPadding(new Insets(20));
    setPrefWidth(300);
    getStyleClass().add("content-dark");
    getChildren().addAll(title, buildPaginationRow(), scroll);

    wirePaginationButtons();
    renderPage();
  }

  /**
   * Replaces the full transaction list and re-renders the current page.
   *
   * <p>The current page index is preserved across calls and clamped to a
   * valid range so browsing older pages is not disrupted by new transactions.
   * Transactions are shown most-recent first.
   *
   * @param transactions the full transaction history to display
   */
  public void setTransactions(List<Transaction> transactions) {
    this.allTransactions = transactions;
    int pages = totalPages();
    if (currentPage >= pages) {
      currentPage = Math.max(0, pages - 1);
    }
    renderPage();
  }

  private int totalPages() {
    int size = allTransactions.size();
    return Math.max(1, (size + PAGE_SIZE - 1) / PAGE_SIZE);
  }

  private void renderPage() {
    rowContainer.getChildren().clear();

    int pages = totalPages();
    pageLabel.setText("Page " + (currentPage + 1) + " of " + pages);
    prevButton.setDisable(currentPage <= 0);
    nextButton.setDisable(currentPage >= pages - 1);

    if (allTransactions.isEmpty()) {
      Label empty = new Label("No transactions yet.");
      empty.getStyleClass().addAll("font-grey", "font-small");
      rowContainer.getChildren().add(empty);
      return;
    }

    // History is stored oldest-first; display newest-first by reversing the index.
    int lastIndex = allTransactions.size() - 1 - currentPage * PAGE_SIZE;
    int firstIndex = Math.max(lastIndex - PAGE_SIZE + 1, 0);
    for (int i = lastIndex; i >= firstIndex; i--) {
      rowContainer.getChildren().add(buildRow(allTransactions.get(i)));
    }
  }

  private void wirePaginationButtons() {
    prevButton.setOnAction(e -> {
      if (currentPage > 0) {
        currentPage--;
        renderPage();
      }
    });
    nextButton.setOnAction(e -> {
      if (currentPage < totalPages() - 1) {
        currentPage++;
        renderPage();
      }
    });
  }

  private HBox buildPaginationRow() {
    prevButton.getStyleClass().add("button-light-grey");
    nextButton.getStyleClass().add("button-light-grey");
    pageLabel.getStyleClass().addAll("font-white", "font-small");

    Region leftSpacer = new Region();
    Region rightSpacer = new Region();
    HBox.setHgrow(leftSpacer, Priority.ALWAYS);
    HBox.setHgrow(rightSpacer, Priority.ALWAYS);

    HBox row = new HBox(8, prevButton, leftSpacer, pageLabel, rightSpacer, nextButton);
    row.setAlignment(Pos.CENTER);
    return row;
  }

  private VBox buildRow(Transaction tx) {
    if (tx instanceof Dividend div) {
      return buildDividendRow(div);
    }
    if (tx instanceof Sale sale) {
      return buildSaleRow(sale);
    }
    return buildPurchaseRow((Purchase) tx);
  }

  private VBox buildPurchaseRow(Purchase tx) {
    Label typeLabel = typeLabel("Buy", true);

    Label detail = new Label(
        tx.getShare().stock().getSymbol()
            + "  ·  " + qty(tx)
            + " shares @ $" + Format.formatMoney(tx.getShare().purchasePrice()));
    detail.getStyleClass().addAll("font-white", "font-small");
    HBox.setHgrow(detail, Priority.ALWAYS);

    Label weekLabel = weekLabel(tx);

    HBox row = new HBox(8, typeLabel, detail, weekLabel);
    row.setAlignment(Pos.CENTER_LEFT);

    return card(row);
  }

  private VBox buildSaleRow(Sale tx) {
    Label typeLabel = typeLabel("Sell", false);

    Label detail = new Label(
        tx.getShare().stock().getSymbol()
            + "  ·  " + qty(tx)
            + " shares @ $" + Format.formatMoney(tx.getSalePrice()));
    detail.getStyleClass().addAll("font-white", "font-small");
    HBox.setHgrow(detail, Priority.ALWAYS);

    Label weekLabel = weekLabel(tx);

    HBox topRow = new HBox(8, typeLabel, detail, weekLabel);
    topRow.setAlignment(Pos.CENTER_LEFT);

    BigDecimal gain = tx.getRealisedGain();
    boolean positive = gain.signum() >= 0;
    String sign = positive ? "+" : "";
    Label gainLabel = new Label(
        "avg cost $" + Format.formatMoney(tx.getShare().purchasePrice())
            + "   realised " + sign + "$" + Format.formatMoney(gain.abs()));
    gainLabel.getStyleClass().add(positive ? "gain-positive" : "gain-negative");

    return card(topRow, gainLabel);
  }

  private VBox buildDividendRow(Dividend tx) {
    Label typeLabel = typeLabel("Div", true);
    typeLabel.getStyleClass().clear();
    typeLabel.getStyleClass().add("tx-type-dividend");
    typeLabel.setPrefWidth(32);
    typeLabel.setMinWidth(32);

    Label detail = new Label(
        tx.getShare().stock().getSymbol()
            + "  ·  " + qty(tx)
            + " shares @ $" + Format.formatMoney(tx.getDividendPerShare())
            + "/share");
    detail.getStyleClass().addAll("font-white", "font-small");
    HBox.setHgrow(detail, Priority.ALWAYS);

    Label amountLabel = new Label("+$" + Format.formatMoney(tx.getTotalPaid()));
    amountLabel.getStyleClass().add("gain-positive");

    Label weekLabel = weekLabel(tx);

    HBox topRow = new HBox(8, typeLabel, detail, weekLabel);
    topRow.setAlignment(Pos.CENTER_LEFT);

    return card(topRow, amountLabel);
  }

  private Label typeLabel(String text, boolean isBuy) {
    Label label = new Label(text);
    label.getStyleClass().add(isBuy ? "tx-type-buy" : "tx-type-sell");
    label.setPrefWidth(32);
    label.setMinWidth(32);
    return label;
  }

  private Label weekLabel(Transaction tx) {
    Label label = new Label("Wk " + tx.getWeek());
    label.getStyleClass().addAll("font-grey", "font-small");
    return label;
  }

  private String qty(Transaction tx) {
    return tx.getShare().quantity().stripTrailingZeros().toPlainString();
  }

  /**
   * Wraps the given nodes in a styled card VBox.
   *
   * @param nodes one or more nodes to include in the card
   * @return the assembled card
   */
  private VBox card(Node... nodes) {
    VBox card = new VBox(4);
    card.getChildren().addAll(nodes);
    card.setPadding(new Insets(8, 10, 8, 10));
    card.getStyleClass().add("tx-card");
    return card;
  }
}