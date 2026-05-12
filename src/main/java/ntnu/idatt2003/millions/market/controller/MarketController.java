package ntnu.idatt2003.millions.market.controller;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.market.service.ExchangeService;
import ntnu.idatt2003.millions.market.view.MarketPanel;
import ntnu.idatt2003.millions.market.view.MarketPanel.Filter;

/**
 * Controller for the market sidebar.
 *
 * <p>Delegates all stock queries to {@link ExchangeService} and notifies
 * the parent controller when the user selects a stock.
 */
public class MarketController {

  private final MarketPanel view;
  private final ExchangeService exchangeService;
  private final Consumer<Stock> onStockSelected;

  private Filter activeFilter = Filter.ALL;
  private String activeQuery = "";

  /**
   * Creates the controller and wires all interactions.
   *
   * @param view            the market sidebar view
   * @param exchangeService the exchange service to query
   * @param onStockSelected callback invoked when the user clicks a stock row
   */
  public MarketController(
      MarketPanel view,
      ExchangeService exchangeService,
      Consumer<Stock> onStockSelected) {
    this.view = view;
    this.exchangeService = exchangeService;
    this.onStockSelected = onStockSelected;

    wireSearch();
    wireFilterButtons();
    wireListSelection();

    refresh();
  }

  /**
   * Re-queries the exchange and updates the list.
   *
   * <p>Called by {@link DashboardController} when a {@code WEEK_ADVANCED} event arrives.
   */
  public void refresh() {
    List<Stock> stocks = switch (activeFilter) {
      case GAINERS -> exchangeService.getGainers(Integer.MAX_VALUE);
      case LOSERS -> exchangeService.getLosers(Integer.MAX_VALUE);
      case ALL -> exchangeService.getExchange().getStocks();
    };

    if (!activeQuery.isBlank()) {
      String q = activeQuery.toLowerCase(Locale.ROOT);
      stocks = stocks.stream()
          .filter(s -> s.getCompany().toLowerCase(Locale.ROOT).contains(q)
              || s.getSymbol().toLowerCase(Locale.ROOT).contains(q))
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
