package ntnu.idatt2003.millions.model;

import java.math.BigDecimal;

/**
 * Player status tiers based on weeks played and net-worth growth.
 *
 * <p>Each constant carries the thresholds required to qualify for that tier,
 * which are used by both {@link Player#getStatus} and {@link #explainStatus}.
 */
public enum Status {
  NOVICE("Novice", 0, BigDecimal.ONE),
  INVESTOR("Investor", 10, new BigDecimal("1.2")),
  SPECULATOR("Speculator", 20, new BigDecimal("2.0"));

  private final String label;
  private final int requiredWeeks;
  private final BigDecimal requiredGrowth;

  Status(String label, int requiredWeeks, BigDecimal requiredGrowth) {
    this.label = label;
    this.requiredWeeks = requiredWeeks;
    this.requiredGrowth = requiredGrowth;
  }

  public String getStatus() {
    return label;
  }

  /**
   * Returns whether the given week and net worth satisfy this tier's requirements.
   *
   * @param week          current game week
   * @param startingMoney player's starting money
   * @param netWorth      player's current net worth
   * @return true if requirements are met
   */
  public boolean qualifies(int week, BigDecimal startingMoney, BigDecimal netWorth) {
    return week >= requiredWeeks
        && netWorth.compareTo(startingMoney.multiply(requiredGrowth)) >= 0;
  }

  /**
   * Returns a human-readable description of the requirements for this status.
   *
   * @return explanation string
   */
  public String explainStatus() {
    if (this == NOVICE) {
      return label + ": No requirements";
    }
    BigDecimal pct = requiredGrowth.subtract(BigDecimal.ONE)
        .multiply(BigDecimal.valueOf(100))
        .stripTrailingZeros();
    return label + ": Requires " + requiredWeeks
        + " weeks of trading and " + pct.toPlainString() + "% growth of net worth";
  }
}
