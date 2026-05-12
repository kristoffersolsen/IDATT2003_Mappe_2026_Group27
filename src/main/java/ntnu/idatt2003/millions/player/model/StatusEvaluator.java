package ntnu.idatt2003.millions.player.model;

import java.math.BigDecimal;
import ntnu.idatt2003.millions.shared.time.GameTime;

/**
 * Pure-function evaluator that determines a player's {@link Status} tier.
 *
 * <p>Extracted from {@link Player} so that non-player entities (e.g., AI traders
 * in a future version) can be ranked without inheriting player state.
 */
public class StatusEvaluator {

  /**
   * Returns the highest {@link Status} tier that the given net worth and game
   * time satisfy.
   *
   * @param startingMoney the player's starting capital
   * @param netWorth      the player's current net worth
   * @param time          the current game time (provides the week number)
   * @return the player's current status
   */
  public Status evaluate(BigDecimal startingMoney, BigDecimal netWorth, GameTime time) {
    int week = time.getWeek();
    Status[] values = Status.values();
    for (int i = values.length - 1; i > 0; i--) {
      if (values[i].qualifies(week, startingMoney, netWorth)) {
        return values[i];
      }
    }
    return Status.NOVICE;
  }
}
