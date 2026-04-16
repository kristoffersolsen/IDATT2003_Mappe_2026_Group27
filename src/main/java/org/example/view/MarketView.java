package org.example.view;

import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.model.Stock;
import org.example.util.Format;

/**
 * Left-sidebar view that displays a searchable, filterable list of stocks.
 *
 * <p>Responsible only for constructing and arranging nodes.
 * All event handling is performed by {@link org.example.controller.MarketController}.
 */
public class MarketView extends VBox {

  /** Filter modes matching the wireframe toggle buttons. */
  public enum Filter {
    ALL, GAINERS, LOSERS
  }

  private final TextField searchField = new TextField();

  private final ToggleButton allButton = new ToggleButton("All");
  private final ToggleButton gainersButton = new ToggleButton("Gainers");
  private final ToggleButton losersButton = new ToggleButton("Losers");
  private final ToggleGroup filterGroup = new ToggleGroup();

  private final ListView<Stock> stockList = new ListView<>();

  /**
   * Builds the market sidebar layout.
   */
  public MarketView() {
    Label title = new Label("The market");
    title.getStyleClass().addAll("font-white", "font-content");

    searchField.setPromptText("Search...");
    searchField.getStyleClass().add("market-search");
    searchField.setMaxWidth(Double.MAX_VALUE);

    allButton.setToggleGroup(filterGroup);
    gainersButton.setToggleGroup(filterGroup);
    losersButton.setToggleGroup(filterGroup);
    allButton.setSelected(true);

    allButton.getStyleClass().add("toggle-filter");
    gainersButton.getStyleClass().add("toggle-filter");
    losersButton.getStyleClass().add("toggle-filter");

    HBox filterBar = new HBox(6, allButton, gainersButton, losersButton);
    filterBar.setAlignment(Pos.CENTER_LEFT);

    stockList.setCellFactory(lv -> new StockCell(lv));
    stockList.getStyleClass().add("market-list");
    stockList.setPlaceholder(new Label("No stocks found"));

    stockList.skinProperty().addListener((obs, oldSkin, newSkin) -> {
      if (newSkin == null) {
        return;
      }
      stockList.lookupAll(".scroll-bar").forEach(node -> {
        if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.HORIZONTAL) {
          sb.setVisible(false);
          sb.setManaged(false);
        }
      });
    });

    VBox.setVgrow(stockList, Priority.ALWAYS);

    setSpacing(10);
    setPadding(new Insets(12));
    getStyleClass().add("content-grey");
    setPrefWidth(240);
    VBox.setVgrow(this, Priority.ALWAYS);

    getChildren().addAll(title, searchField, filterBar, stockList);
  }

  /**
   * Replaces the displayed list with the given stocks.
   *
   * @param stocks the stocks to display
   */
  public void setStocks(List<Stock> stocks) {
    stockList.getItems().setAll(stocks);
  }

  public TextField getSearchField() {
    return searchField;
  }

  public ToggleButton getAllButton() {
    return allButton;
  }

  public ToggleButton getGainersButton() {
    return gainersButton;
  }

  public ToggleButton getLosersButton() {
    return losersButton;
  }

  public ListView<Stock> getStockList() {
    return stockList;
  }

  // ------------------------------------------------------------------ //
  //  Cell factory                                                         //
  // ------------------------------------------------------------------ //

  /**
   * Custom list cell that lays out symbol, company, price, and change
   * using an {@link HBox} so content always fits the available cell width.
   */
  private static final class StockCell extends ListCell<Stock> {

    private final Label symbolLabel = new Label();
    private final Label companyLabel = new Label();
    private final Label priceLabel = new Label();
    private final Label changeLabel = new Label();
    private final HBox row;

    StockCell(ListView<Stock> owner) {
      symbolLabel.getStyleClass().addAll("font-white", "font-small");
      symbolLabel.setPrefWidth(44);
      symbolLabel.setMinWidth(44);

      companyLabel.getStyleClass().addAll("font-white", "font-small");
      HBox.setHgrow(companyLabel, Priority.ALWAYS);
      companyLabel.setMaxWidth(Double.MAX_VALUE);

      priceLabel.getStyleClass().addAll("font-white", "font-small");
      priceLabel.setAlignment(Pos.CENTER_RIGHT);
      priceLabel.setPrefWidth(52);
      priceLabel.setMinWidth(52);

      changeLabel.getStyleClass().add("market-cell-change");
      changeLabel.setAlignment(Pos.CENTER_RIGHT);
      changeLabel.setPrefWidth(38);
      changeLabel.setMinWidth(38);

      row = new HBox(4, symbolLabel, companyLabel, priceLabel, changeLabel);
      row.setAlignment(Pos.CENTER_LEFT);
      row.setPadding(new Insets(3, 6, 3, 4));

      // Bind row width to list width so cells never cause a horizontal scrollbar
      row.prefWidthProperty().bind(owner.widthProperty().subtract(18));
      row.maxWidthProperty().bind(owner.widthProperty().subtract(18));

      setText(null);
    }

    @Override
    protected void updateItem(Stock stock, boolean empty) {
      super.updateItem(stock, empty);
      if (empty || stock == null) {
        setGraphic(null);
        return;
      }

      symbolLabel.setText(stock.getSymbol());
      companyLabel.setText(stock.getCompany());
      priceLabel.setText("$" + Format.formatMoney(stock.getSalesPrice()));

      BigDecimal change = stock.getLatestPriceChange();
      String sign = change.signum() >= 0 ? "+" : "";
      changeLabel.setText(sign + Format.formatMoney(change));

      changeLabel.getStyleClass().removeAll("price-up", "price-down", "price-flat");
      if (change.signum() > 0) {
        changeLabel.getStyleClass().add("price-up");
      } else if (change.signum() < 0) {
        changeLabel.getStyleClass().add("price-down");
      } else {
        changeLabel.getStyleClass().add("price-flat");
      }

      setGraphic(row);
    }
  }
}