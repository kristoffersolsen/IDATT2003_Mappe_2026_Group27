package org.example.controller;

import java.util.List;
import java.util.function.Consumer;
import org.example.model.Stock;
import org.example.service.Exchange;
import org.example.view.MarketView;
import org.example.view.MarketView.Filter;

/**
 * Controller for the market sidebar.
 *
 * <p>Wires the {@link MarketView} search field, filter toggles, and list
 * selection to the {@link Exchange} query methods. Notifies a parent
 * controller when the user selects a stock via the {@code onStockSelected}
 * callback.
 */
public class MarketController {

  private final MarketView view;
  private final Exchange exchange;
  private final Consumer<Stock> onStockSelected;

  private Filter activeFilter = Filter.ALL;
  private String activeQuery = "";

  /**
   * Creates the controller and wires all interactions.
   *
   * @param view            the market sidebar view
   * @param exchange        the exchange to query
   * @param onStockSelected callback invoked when the user clicks a stock row
   */
  public MarketController(
      MarketView view,
      Exchange exchange,
      Consumer<Stock> onStockSelected) {
    this.view = view;
    this.exchange = exchange;
    this.onStockSelected = onStockSelected;

    wireSearch();
    wireFilterButtons();
    wireListSelection();

    refresh();
  }

  /**
   * Re-queries the exchange and updates the list.
   *
   * <p>Call this after the exchange advances a week so prices and
   * gainer/loser rankings stay current.
   */
  public void refresh() {
    List<Stock> stocks = switch (activeFilter) {
      case GAINERS -> exchange.getGainers(Integer.MAX_VALUE);
      case LOSERS -> exchange.getLosers(Integer.MAX_VALUE);
      case ALL -> activeQuery.isBlank()
          ? exchange.getStocks()
          : exchange.findStocks(activeQuery);
    };

    // Apply the search filter on top of gainers/losers when a query is active
    if (!activeQuery.isBlank() && activeFilter != Filter.ALL) {
      String q = activeQuery.toLowerCase();
      stocks = stocks.stream()
          .filter(s -> s.getCompany().toLowerCase().contains(q)
              || s.getSymbol().toLowerCase().contains(q))
          .toList();
    }

    view.setStocks(stocks);
  }

  private void wireSearch() {
    view.getSearchField().textProperty().addListener((obs, oldVal, newVal) -> {
      activeQuery = newVal == null ? "" : newVal.trim();
      refresh();
    });
  }

  private void wireFilterButtons() {
    view.getAllButton().setOnAction(e -> {
      activeFilter = Filter.ALL;
      refresh();
    });
    view.getGainersButton().setOnAction(e -> {
      activeFilter = Filter.GAINERS;
      refresh();
    });
    view.getLosersButton().setOnAction(e -> {
      activeFilter = Filter.LOSERS;
      refresh();
    });
  }

  private void wireListSelection() {
    view.getStockList().getSelectionModel().selectedItemProperty()
        .addListener((obs, oldStock, newStock) -> {
          if (newStock != null) {
            onStockSelected.accept(newStock);
          }
        });
  }
}