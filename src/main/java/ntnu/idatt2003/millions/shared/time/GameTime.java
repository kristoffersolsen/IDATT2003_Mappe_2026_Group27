package ntnu.idatt2003.millions.shared.time;

import ntnu.idatt2003.millions.shared.config.GameSettings;

/**
 * Immutable snapshot of in-game time, derived from a simulation tick count.
 *
 * <p>All calendar units (hour, day, week, month) are computed on demand from
 * {@code tickCount} and the calendar constants in {@link GameSettings}. The
 * trading day starts at 08:00.
 */
public class GameTime {

  private static final int TRADING_DAY_START_HOUR = 8;

  private static final String[] DAY_NAMES = {"Mon", "Tue", "Wed", "Thu", "Fri"};

  private final GameSettings settings;
  private final long tickCount;

  /**
   * Constructs a game-time snapshot for the given tick.
   *
   * @param settings  the game settings supplying calendar constants
   * @param tickCount the simulation tick count (hours elapsed since game start)
   */
  public GameTime(GameSettings settings, long tickCount) {
    this.settings = settings;
    this.tickCount = tickCount;
  }

  /**
   * Returns the hour within the current trading day (0 = first hour, hoursPerDay-1 = last).
   *
   * @return hour of trading day
   */
  public int getHour() {
    return (int) (tickCount % settings.hoursPerDay());
  }

  /**
   * Returns the day within the current trading week (1-based).
   *
   * @return trading day (1 = Monday, daysPerWeek = Friday for a 5-day week)
   */
  public int getDay() {
    return (int) ((tickCount / settings.hoursPerDay()) % settings.daysPerWeek()) + 1;
  }

  /**
   * Returns the current trading week number (1-based).
   *
   * <p>Used by {@link ntnu.idatt2003.millions.player.model.StatusEvaluator} to check
   * week-based promotion thresholds.
   *
   * @return week number
   */
  public int getWeek() {
    long ticksPerWeek = (long) settings.hoursPerDay() * settings.daysPerWeek();
    return (int) (tickCount / ticksPerWeek) + 1;
  }

  /**
   * Returns the current trading month number (1-based).
   *
   * @return month number
   */
  public int getMonth() {
    long ticksPerMonth =
        (long) settings.hoursPerDay() * settings.daysPerWeek() * settings.weeksPerMonth();
    return (int) (tickCount / ticksPerMonth) + 1;
  }

  /**
   * Returns a human-readable label for the current game time.
   *
   * <p>Format: {@code "Wk N · Mon · HH:00"}, e.g. {@code "Wk 1 · Mon · 08:00"}.
   *
   * @return formatted time string
   */
  public String format() {
    String dayName = DAY_NAMES[(getDay() - 1) % DAY_NAMES.length];
    int clockHour = TRADING_DAY_START_HOUR + getHour();
    return String.format("Wk %d · %s · %02d:00", getWeek(), dayName, clockHour);
  }

  /**
   * Returns a short human-readable label showing only week and day.
   *
   * <p>Format: {@code "Wk N · Mon"}, e.g. {@code "Wk 3 · Wed"}.
   *
   * @return short date string
   */
  public String formatDate() {
    String dayName = DAY_NAMES[(getDay() - 1) % DAY_NAMES.length];
    return String.format("Wk %d · %s", getWeek(), dayName);
  }
}
