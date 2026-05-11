package ntnu.idatt2003.millions.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.util.StringConverter;
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
 *
 * <p>Each zoom level produces a {@link ChartConfig} that controls which data
 * points are loaded, how the x-axis is labelled, and whether symbols are shown:
 * <ul>
 *   <li><b>Day</b> — hourly prices, one label per hour ("08:00"…"15:00").</li>
 *   <li><b>Week</b> — hourly prices, one tick per day ("Mon"…"Fri")</li>
 *   <li><b>Month</b> — daily closing prices only, one tick per week ("Wk N").</li>
 *   <li><b>All</b> — automatically picks Day / Week / Month based on history size.</li>
 * </ul>
 */
public class StockPriceChartController {

  private enum ZoomLevel { DAY, WEEK, MONTH, ALL }

  private static final int TRADING_START_HOUR = 8;
  private static final String[] DAY_NAMES = {"Mon", "Tue", "Wed", "Thu", "Fri"};

  private record ChartConfig(
      List<BigDecimal> prices,
      double tickUnit,
      StringConverter<Number> formatter,
      boolean showSymbols
  ) {
  }

  private final StockPriceChartView view;
  private final ExchangeService exchangeService;
  private final GameSettings settings;

  private Stock currentStock;
  private int viewEnd;
  private ZoomLevel zoom = ZoomLevel.ALL;
  private boolean pinToEnd = true;

  /**
   * @param view            the chart view to manage
   * @param exchangeService used to derive the current tick for time labels
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
   * <p>When pinned to the live end the view advances automatically to include
   * new ticks; otherwise {@code viewEnd} is clamped to the new history size.
   *
   * @param stock the stock to display
   */
  public void refresh(Stock stock) {
    this.currentStock = stock;
    int historySize = stock.getHistoricalPrices().size();
    if (historySize == 0) {
      return;
    }
    viewEnd = pinToEnd ? historySize - 1 : Math.min(viewEnd, historySize - 1);
    renderCurrent();
  }

  // ---- rendering ----

  private void renderCurrent() {
    List<BigDecimal> allPrices = currentStock.getHistoricalPrices();
    int historySize = allPrices.size();
    if (historySize == 0) {
      return;
    }

    long currentTick = exchangeService.getExchange().getTickCount();
    long absoluteFirstTick = currentTick - historySize + 1;

    int visibleCount = (zoom == ZoomLevel.ALL) ? historySize : zoomSizeFor(zoom);
    int viewStart = Math.max(0, viewEnd - visibleCount + 1);
    List<BigDecimal> slice = allPrices.subList(viewStart, viewEnd + 1);
    long sliceStartTick = absoluteFirstTick + viewStart;

    ZoomLevel effective = (zoom == ZoomLevel.ALL) ? autoZoom(historySize) : zoom;
    ChartConfig cfg = buildConfig(effective, slice, sliceStartTick);
    view.update(cfg.prices(), cfg.tickUnit(), cfg.formatter(), cfg.showSymbols());

    view.setContext(new GameTime(settings, absoluteFirstTick + viewEnd).formatDate());
    view.setBackEnabled(zoom != ZoomLevel.ALL && viewStart > 0);
    view.setForwardEnabled(zoom != ZoomLevel.ALL && viewEnd < historySize - 1);
  }

  /**
   * Chooses the most appropriate zoom level for All mode based on history size.
   */
  private ZoomLevel autoZoom(int historySize) {
    int hoursPerDay = settings.hoursPerDay();
    int hoursPerWeek = hoursPerDay * settings.daysPerWeek();
    if (historySize <= hoursPerDay) {
      return ZoomLevel.DAY;
    }
    if (historySize <= hoursPerWeek) {
      return ZoomLevel.WEEK;
    }
    return ZoomLevel.MONTH;
  }

  private ChartConfig buildConfig(ZoomLevel level, List<BigDecimal> slice, long startTick) {
    return switch (level) {
      case DAY -> buildDayConfig(slice, startTick);
      case WEEK -> buildWeekConfig(slice, startTick);
      default -> buildMonthConfig(slice, startTick);
    };
  }

  /**
   * Day: hourly prices, one label per hour ("08:00" … "15:00").
   */
  private ChartConfig buildDayConfig(List<BigDecimal> slice, long startTick) {
    int hoursPerDay = settings.hoursPerDay();
    StringConverter<Number> fmt = new StringConverter<>() {
      @Override
      public String toString(Number x) {
        int idx = (int) Math.round(x.doubleValue());
        int hourOfDay = (int) ((startTick + idx) % hoursPerDay);
        return String.format("%02d:00", TRADING_START_HOUR + hourOfDay);
      }

      @Override
      public Number fromString(String s) {
        return 0;
      }
    };
    return new ChartConfig(slice, 1, fmt, true);
  }

  /**
   * Week: hourly prices, one tick per day ("Mon" … "Fri"), no symbols.
   */
  private ChartConfig buildWeekConfig(List<BigDecimal> slice, long startTick) {
    int hoursPerDay = settings.hoursPerDay();
    int daysPerWeek = settings.daysPerWeek();
    StringConverter<Number> fmt = new StringConverter<>() {
      @Override
      public String toString(Number x) {
        int idx = (int) Math.round(x.doubleValue());
        if (idx % hoursPerDay != 0) {
          return "";
        }
        int dayOfWeek = (int) ((startTick + idx) / hoursPerDay % daysPerWeek);
        return DAY_NAMES[dayOfWeek];
      }

      @Override
      public Number fromString(String s) {
        return 0;
      }
    };
    return new ChartConfig(slice, hoursPerDay, fmt, false);
  }

  /**
   * Month: one daily closing price per day, one tick per week ("Wk N").
   *
   * <p>The closing price is the last hourly price of each trading day.
   * If the slice starts mid-day the first partial day is skipped so every
   * displayed point is a genuine end-of-day price.
   */
  private ChartConfig buildMonthConfig(List<BigDecimal> slice, long startTick) {
    int hoursPerDay = settings.hoursPerDay();
    int daysPerWeek = settings.daysPerWeek();

    int startHourInDay = (int) (startTick % hoursPerDay);
    int firstCloseOffset = hoursPerDay - 1 - startHourInDay;

    List<BigDecimal> daily = new ArrayList<>();
    for (int i = firstCloseOffset; i < slice.size(); i += hoursPerDay) {
      daily.add(slice.get(i));
    }

    if (daily.isEmpty()) {
      return buildDayConfig(slice, startTick);
    }

    long firstCloseTick = startTick + firstCloseOffset;

    StringConverter<Number> fmt = new StringConverter<>() {
      @Override
      public String toString(Number x) {
        int dayIdx = (int) Math.round(x.doubleValue());
        if (dayIdx % daysPerWeek != 0) {
          return "";
        }
        long tick = firstCloseTick + (long) dayIdx * hoursPerDay;
        return "Wk " + new GameTime(settings, tick).getWeek();
      }

      @Override
      public Number fromString(String s) {
        return 0;
      }
    };
    return new ChartConfig(daily, daysPerWeek, fmt, true);
  }

  // ---- button wiring ----

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

  private void onZoom(ZoomLevel level, Button button) {
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
