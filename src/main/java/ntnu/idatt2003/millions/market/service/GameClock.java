package ntnu.idatt2003.millions.market.service;

import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.shared.time.GameTime;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.shared.observer.GameEvent;

/**
 * Tracks the current simulation tick count and drives hour-by-hour advancement.
 *
 * <p>Advancement is synchronous and single-threaded. Calling {@link #advanceBy}
 * loops over the requested number of hours, calling
 * {@link ExchangeService#tick()} once per hour, then fires
 * {@link GameEvent#SKIP_COMPLETED} on the exchange so observers can refresh
 * after the full skip rather than after each individual tick.
 */
public class GameClock {

  private final ExchangeService exchangeService;
  private final GameSettings settings;

  /**
   * Constructs a clock backed by the given exchange service.
   *
   * @param exchangeService the exchange service to advance on each tick
   * @param settings        the game settings supplying calendar constants
   */
  public GameClock(ExchangeService exchangeService, GameSettings settings) {
    this.exchangeService = exchangeService;
    this.settings = settings;
  }

  /**
   * Advances the simulation by the given number of hours.
   *
   * <p>Before the tick loop begins, {@link Stock#markSkipStart()} is called on
   * every listed stock so that {@link Stock#getSkipPriceChange()} reflects the
   * full price movement across the skip. After the loop,
   * {@link GameEvent#SKIP_COMPLETED} is fired once on the exchange.
   *
   * @param hours the number of simulated hours to advance (must be positive)
   */
  public void advanceBy(int hours) {
    for (Stock stock : exchangeService.getExchange().getStocks()) {
      stock.markSkipStart();
    }
    for (int i = 0; i < hours; i++) {
      exchangeService.tick();
    }
    exchangeService.getExchange().notifyObservers(GameEvent.SKIP_COMPLETED);
  }

  /**
   * Returns the total number of simulated hours elapsed since game start.
   *
   * <p>Delegates to {@link Exchange#getTickCount()} which is the authoritative
   * source of the simulation tick.
   *
   * @return current tick count
   */
  public long getTickCount() {
    return exchangeService.getExchange().getTickCount();
  }

  /**
   * Returns a {@link GameTime} snapshot for the current tick.
   *
   * @return current game time
   */
  public GameTime currentTime() {
    return new GameTime(settings, exchangeService.getExchange().getTickCount());
  }
}
