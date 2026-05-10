package ntnu.idatt2003.millions.config;

/**
 * Difficulty levels available when starting a new game.
 *
 * <p>Each level adjusts price volatility and drift bias via
 * {@link GameDefaults}.
 */
public enum Difficulty {

  /**
   * Low volatility, slight positive drift — forgiving for new players.
   */
  EASY("Easy"),

  /**
   * Moderate volatility, no drift bias — balanced play.
   */
  NORMAL("Normal"),

  /**
   * High volatility, slight negative drift — requires active stock-picking.
   */
  HARD("Hard");

  private final String displayName;

  Difficulty(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
