package ntnu.idatt2003.millions.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.millions.controller.DashboardController;

/**
 * The main dashboard shown during gameplay.
 *
 * <p>Uses a {@link BorderPane} as the root:
 * <ul>
 *   <li>top    — week indicator and game controls</li>
 *   <li>left   — market sidebar (stock list, search, filters)</li>
 *   <li>center — stock detail, chart, buy/sell form (set via
 *                {@link #setCenterPanel(Node)})</li>
 *   <li>bottom — player info, asset summary, panel toggle buttons</li>
 *   <li>right  — slide-in portfolio or transactions panel (toggled by
 *                controller)</li>
 * </ul>
 *
 * <p>Responsible only for constructing and arranging nodes.
 * All event handling is performed by
 * {@link DashboardController}.
 */
public class DashboardView {

  private final BorderPane root;

  private final Label weekLabel = new Label("Week 1");
  private final Button advanceButton = new Button("Advance");
  private final Button endGameButton = new Button("End Game");

  private final Label playerNameLabel = new Label();
  private final Label statusLabel = new Label();
  private final Label netWorthLabel = new Label();
  private final Label cashLabel = new Label();
  private final Label portfolioValueLabel = new Label();

  private final Button portfolioButton = new Button("Portfolio Details");
  private final Button transactionsButton = new Button("Recent Transactions");

  private final MarketView marketView = new MarketView();

  /**
   * Builds the dashboard layout.
   */
  public DashboardView() {
    root = new BorderPane();
    root.getStyleClass().add("root");

    root.setTop(buildTopBar());
    root.setLeft(marketView);
    root.setBottom(buildBottomBar());
    // Center is set later by DashboardController via setCenterPanel()
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
    root.setCenter(panel);
  }

  /**
   * Shows or hides a panel in the right region.
   *
   * @param panel the node to show, or null to close the right region
   */
  public void setRightPanel(VBox panel) {
    root.setRight(panel);
  }

  /**
   * Updates the week label in the top bar.
   *
   * @param week the current week number
   */
  public void setWeek(int week) {
    weekLabel.setText("Week " + week);
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
    weekLabel.getStyleClass().addAll("font-white", "font-title");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox topBar = new HBox(12, weekLabel, advanceButton, endGameButton, spacer);
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
    return new VBox(8, portfolioButton, transactionsButton);
  }

  public BorderPane getRoot() {
    return root;
  }

  public Button getAdvanceButton() {
    return advanceButton;
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

  public MarketView getMarketView() {
    return marketView;
  }
}