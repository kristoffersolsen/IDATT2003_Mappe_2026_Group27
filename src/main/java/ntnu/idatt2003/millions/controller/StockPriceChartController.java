package ntnu.idatt2003.millions.controller;

import ntnu.idatt2003.millions.model.Stock;
import ntnu.idatt2003.millions.service.ExchangeService;
import ntnu.idatt2003.millions.view.StockPriceChartView;

/**
 * Controller for {@link StockPriceChartView}.
 *
 * <p>Computes the display week offset from the exchange tick count and
 * forwards the update to the chart view.
 */
public class StockPriceChartController {

  private final StockPriceChartView view;
  private final ExchangeService exchangeService;

  /**
   * @param view            the chart view to manage
   * @param exchangeService used to derive the current tick for week offsets
   */
  public StockPriceChartController(StockPriceChartView view, ExchangeService exchangeService) {
    this.view = view;
    this.exchangeService = exchangeService;
  }

  /**
   * Refreshes the chart with the latest price history for the given stock.
   *
   * @param stock the stock to display
   */
  public void refresh(Stock stock) {
    int historySize = stock.getHistoricalPrices().size();
    long currentTick = exchangeService.getExchange().getTickCount();
    int startWeek = (int) Math.max(1, currentTick - historySize + 1);
    view.update(stock, startWeek);
  }
}
