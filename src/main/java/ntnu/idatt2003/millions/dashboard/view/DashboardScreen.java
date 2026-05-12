package ntnu.idatt2003.millions.dashboard.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.millions.dashboard.controller.DashboardController;
import ntnu.idatt2003.millions.market.view.MarketPanel;

/**
 * The main dashboard shown during gameplay.
 *
 * <p>Uses a {@link BorderPane} as the root:
 * <ul>
 *   <li>top    — game time and skip controls</li>
 *   <li>left   — market sidebar (stock list, search, filters)</li>
 *   <li>center — stock detail, chart, buy/sell form (set via
 *                {@link #setCenterPanel(Node)})</li>
 *   <li>bottom — player info, asset summary, panel toggle buttons</li>
 *   <li>right  — slide-in portfolio or transactions panel (toggled by
 *                controller)</li>
 * </ul>
 *
 * <p>Responsible only for constructing and arranging nodes.
 * All event handling is performed by {@link DashboardController}.
 */
public class DashboardScreen {

  private final BorderPane root;

  private final Label timeLabel = new Label("Wk 1 · Mon · 08:00");
  private final Button skip1hButton = new Button("+1h");
  private final Button skip5hButton = new Button("+5h");
  private final Button skip1dButton = new Button("+1d");
  private final Button skip1wButton = new Button("+1w");
  private final Button endGameButton = new Button("End Game");

  private final Label playerNameLabel = new Label();
  private final Label statusLabel = new Label();
  private final Label netWorthLabel = new Label();
  private final Label cashLabel = new Label();
  private final Label portfolioValueLabel = new Label();

  private final Button portfolioButton = new Button("Portfolio Details");
  private final Button transactionsButton = new Button("Recent Transactions");
  private final Button ordersButton = new Button("Pending Orders");

  private final MarketPanel marketView = new MarketPanel();
  private final SplitPane mainSplit = new SplitPane();

  /**
   * Builds the dashboard layout.
   */
  public DashboardScreen() {
    root = new BorderPane();
    root.getStyleClass().add("root");

    marketView.setMinWidth(120);
    mainSplit.getItems().add(marketView);
    SplitPane.setResizableWithParent(marketView, false);
    mainSplit.getStyleClass().add("main-split");

    root.setTop(buildTopBar());
    root.setCenter(mainSplit);
    root.setBottom(buildBottomBar());
  }

  /**
   * Replaces the center region with the given node.
   *
   * <p>Called once by {@link DashboardController}
   * after it has constructed the {@link StockDetailView}.
   *
   * @param panel the node to display in the center
   */
  public void setCenterPanel(Node panel) {
    if (mainSplit.getItems().size() >= 2) {
      mainSplit.getItems().set(1, panel);
    } else {
      mainSplit.getItems().add(panel);
      mainSplit.setDividerPositions(0.22);
    }
  }

  /**
   * Shows or hides a panel in the right region.
   *
   * @param panel the node to show, or null to close the right region
   */
  public void setRightPanel(VBox panel) {
    var items = mainSplit.getItems();
    if (panel == null) {
      if (items.size() == 3) {
        items.remove(2);
      }
    } else {
      panel.setMinWidth(180);
      SplitPane.setResizableWithParent(panel, false);
      if (items.size() == 3) {
        items.set(2, panel);
      } else {
        items.add(panel);
        mainSplit.setDividerPositions(mainSplit.getDividerPositions()[0], 0.75);
      }
    }
  }

  /**
   * Updates the time label in the top bar.
   *
   * @param label the formatted time string to display
   */
  public void setTimeLabel(String label) {
    timeLabel.setText(label);
  }

  /**
   * Updates the player info displayed in the bottom bar.
   *
   * @param name           the player's name
   * @param status         the player's current status label
   * @param netWorth       formatted net worth string
   * @param cash           formatted cash string
   * @param portfolioValue formatted portfolio value string
   */
  public void updatePlayerInfo(
      String name,
      String status,
      String netWorth,
      String cash,
      String portfolioValue) {
    playerNameLabel.setText(name);
    statusLabel.setText(status);
    netWorthLabel.setText(netWorth);
    cashLabel.setText(cash);
    portfolioValueLabel.setText(portfolioValue);
  }

  private HBox buildTopBar() {
    timeLabel.getStyleClass().addAll("font-white", "font-title");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox topBar = new HBox(
        12, timeLabel, spacer, skip1hButton, skip5hButton, skip1dButton, skip1wButton,
        endGameButton);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(12, 16, 12, 16));
    topBar.getStyleClass().add("content-dark");
    return topBar;
  }

  private HBox buildBottomBar() {
    VBox playerSection = buildPlayerSection();
    VBox assetsSection = buildAssetsSection();
    VBox buttonsSection = buildButtonsSection();

    Region leftSpacer = new Region();
    Region rightSpacer = new Region();
    HBox.setHgrow(leftSpacer, Priority.ALWAYS);
    HBox.setHgrow(rightSpacer, Priority.ALWAYS);

    HBox bottomBar = new HBox(
        24, playerSection, leftSpacer, assetsSection, rightSpacer, buttonsSection);
    bottomBar.setAlignment(Pos.CENTER_LEFT);
    bottomBar.setPadding(new Insets(12, 16, 12, 16));
    bottomBar.getStyleClass().add("content-dark");
    return bottomBar;
  }

  private VBox buildPlayerSection() {
    Label playerLabel = new Label("Player:");
    playerLabel.getStyleClass().addAll("font-white", "font-sub-title");
    playerNameLabel.getStyleClass().addAll("font-white", "font-sub-title");

    HBox nameRow = new HBox(8, playerLabel, playerNameLabel);
    nameRow.setAlignment(Pos.CENTER_LEFT);

    statusLabel.getStyleClass().add("button-light-grey");
    return new VBox(6, nameRow, statusLabel);
  }

  private VBox buildAssetsSection() {
    Label title = new Label("Assets:");
    title.getStyleClass().addAll("font-white", "font-content");

    HBox netWorthRow = buildAssetRow("Net worth:", netWorthLabel);
    HBox cashRow = buildAssetRow("Cash:", cashLabel);
    HBox portfolioRow = buildAssetRow("Portfolio:", portfolioValueLabel);

    return new VBox(4, title, netWorthRow, cashRow, portfolioRow);
  }

  private HBox buildAssetRow(String labelText, Label valueLabel) {
    Label label = new Label(labelText);
    label.getStyleClass().addAll("font-white", "font-small");
    label.setPrefWidth(90);
    valueLabel.getStyleClass().addAll("font-white", "font-small");

    HBox row = new HBox(8, label, valueLabel);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
  }

  private VBox buildButtonsSection() {
    portfolioButton.setPrefWidth(180);
    transactionsButton.setPrefWidth(180);
    ordersButton.setPrefWidth(180);
    return new VBox(8, portfolioButton, transactionsButton, ordersButton);
  }

  public BorderPane getRoot() {
    return root;
  }

  public Button getSkip1hButton() {
    return skip1hButton;
  }

  public Button getSkip5hButton() {
    return skip5hButton;
  }

  public Button getSkip1dButton() {
    return skip1dButton;
  }

  public Button getSkip1wButton() {
    return skip1wButton;
  }

  public Button getEndGameButton() {
    return endGameButton;
  }

  public Button getPortfolioButton() {
    return portfolioButton;
  }

  public Button getTransactionsButton() {
    return transactionsButton;
  }

  public Button getOrdersButton() {
    return ordersButton;
  }

  public MarketPanel getMarketView() {
    return marketView;
  }
}
