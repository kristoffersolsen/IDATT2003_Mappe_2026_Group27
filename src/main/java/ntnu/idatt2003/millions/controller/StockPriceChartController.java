package ntnu.idatt2003.millions.controller;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.millions.config.GameSettings;
import ntnu.idatt2003.millions.model.Stock;
import ntnu.idatt2003.millions.model.time.GameTime;
import ntnu.idatt2003.millions.service.ExchangeService;
import ntnu.idatt2003.millions.view.StockPriceChartView;

/**
 * Controller for {@link StockPriceChartView}.
 *
 * <p>Owns the zoom/scroll state and wires the toolbar buttons.
 * The visible window is defined by {@code viewEnd} (inclusive index into the
 * stock's price history) and the current {@link ZoomLevel}.
 */
public class StockPriceChartController {

  private enum ZoomLevel { DAY, WEEK, MONTH, ALL }

  private final StockPriceChartView view;
  private final ExchangeService exchangeService;
  private final GameSettings settings;

  private Stock currentStock;
  private int viewEnd;
  private ZoomLevel zoom = ZoomLevel.ALL;
  private boolean pinToEnd = true;

  /**
   * @param view            the chart view to manage
   * @param exchangeService used to derive the current tick for x-axis offsets
   * @param settings        game settings supplying calendar constants for zoom sizes
   */
  public StockPriceChartController(
      StockPriceChartView view,
      ExchangeService exchangeService,
      GameSettings settings) {
    this.view = view;
    this.exchangeService = exchangeService;
    this.settings = settings;
    wireButtons();
  }

  /**
   * Switches the chart to display the given stock and resets to the All view.
   *
   * @param stock the stock to display
   */
  public void showStock(Stock stock) {
    this.currentStock = stock;
    this.zoom = ZoomLevel.ALL;
    this.pinToEnd = true;
    this.viewEnd = stock.getHistoricalPrices().size() - 1;
    view.setActiveZoomButton(view.getAllButton());
    renderCurrent();
  }

  /**
   * Refreshes the chart with the latest price data for the current stock.
   *
   * <p>If the chart was pinned to the live end (user has not scrolled back),
   * the view end advances to include new ticks automatically.
   *
   * @param stock the stock to display
   */
  public void refresh(Stock stock) {
    this.currentStock = stock;
    int historySize = stock.getHistoricalPrices().size();
    if (historySize == 0) {
      return;
    }
    if (pinToEnd) {
      viewEnd = historySize - 1;
    } else {
      viewEnd = Math.min(viewEnd, historySize - 1);
    }
    renderCurrent();
  }

  private void renderCurrent() {
    List<BigDecimal> allPrices = currentStock.getHistoricalPrices();
    int historySize = allPrices.size();
    if (historySize == 0) {
      return;
    }

    long currentTick = exchangeService.getExchange().getTickCount();
    int firstTick = (int) Math.max(1, currentTick - historySize + 1);

    int visibleCount = (zoom == ZoomLevel.ALL) ? historySize : zoomSizeFor(zoom);
    int viewStart = Math.max(0, viewEnd - visibleCount + 1);

    List<BigDecimal> slice = allPrices.subList(viewStart, viewEnd + 1);
    view.update(slice, firstTick + viewStart);

    long tickAtViewEnd = firstTick + viewEnd;
    view.setContext(new GameTime(settings, tickAtViewEnd).formatDate());

    view.setBackEnabled(zoom != ZoomLevel.ALL && viewStart > 0);
    view.setForwardEnabled(zoom != ZoomLevel.ALL && viewEnd < historySize - 1);
  }

  private void wireButtons() {
    view.getBackButton().setOnAction(e -> onBack());
    view.getForwardButton().setOnAction(e -> onForward());
    view.getDayButton().setOnAction(e -> onZoom(ZoomLevel.DAY, view.getDayButton()));
    view.getWeekButton().setOnAction(e -> onZoom(ZoomLevel.WEEK, view.getWeekButton()));
    view.getMonthButton().setOnAction(e -> onZoom(ZoomLevel.MONTH, view.getMonthButton()));
    view.getAllButton().setOnAction(e -> onZoom(ZoomLevel.ALL, view.getAllButton()));
  }

  private void onBack() {
    if (currentStock == null) {
      return;
    }
    int step = zoomSizeFor(zoom);
    viewEnd = Math.max(step - 1, viewEnd - step);
    pinToEnd = false;
    renderCurrent();
  }

  private void onForward() {
    if (currentStock == null) {
      return;
    }
    int historySize = currentStock.getHistoricalPrices().size();
    int step = zoomSizeFor(zoom);
    viewEnd = Math.min(historySize - 1, viewEnd + step);
    pinToEnd = (viewEnd == historySize - 1);
    renderCurrent();
  }

  private void onZoom(ZoomLevel level, javafx.scene.control.Button button) {
    if (currentStock == null) {
      return;
    }
    zoom = level;
    view.setActiveZoomButton(button);
    if (level == ZoomLevel.ALL) {
      viewEnd = currentStock.getHistoricalPrices().size() - 1;
      pinToEnd = true;
    }
    renderCurrent();
  }

  private int zoomSizeFor(ZoomLevel level) {
    int hoursPerDay = settings.hoursPerDay();
    int hoursPerWeek = hoursPerDay * settings.daysPerWeek();
    return switch (level) {
      case DAY -> hoursPerDay;
      case WEEK -> hoursPerWeek;
      case MONTH -> hoursPerWeek * settings.weeksPerMonth();
      case ALL -> Integer.MAX_VALUE / 2;
    };
  }
}
