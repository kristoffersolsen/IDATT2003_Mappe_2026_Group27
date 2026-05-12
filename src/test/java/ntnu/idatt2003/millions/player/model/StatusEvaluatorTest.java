package ntnu.idatt2003.millions.player.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import ntnu.idatt2003.millions.shared.config.Difficulty;
import ntnu.idatt2003.millions.shared.config.GameDefaults;
import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.shared.time.GameTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StatusEvaluator")
class StatusEvaluatorTest {

  private static final GameSettings SETTINGS = GameDefaults.forDifficulty(Difficulty.NORMAL);
  private static final long TICKS_PER_WEEK =
      (long) SETTINGS.hoursPerDay() * SETTINGS.daysPerWeek();

  private StatusEvaluator evaluator;

  @BeforeEach
  void setUp() {
    evaluator = new StatusEvaluator();
  }

  /** Creates a GameTime that reports the given week number (1-based). */
  private GameTime atWeek(int week) {
    return new GameTime(SETTINGS, (long) (week - 1) * TICKS_PER_WEEK);
  }

  @Nested
  @DisplayName("NOVICE")
  class NoviceStatus {

    @Test
    @DisplayName("returns NOVICE at week 1 with starting net worth")
    void noviceAtStart() {
      BigDecimal starting = BigDecimal.valueOf(1000);
      assertEquals(Status.NOVICE, evaluator.evaluate(starting, starting, atWeek(1)));
    }

    @Test
    @DisplayName("returns NOVICE when week < 10 even with high net worth")
    void noviceBeforeWeek10() {
      BigDecimal starting = BigDecimal.valueOf(1000);
      BigDecimal highWorth = BigDecimal.valueOf(5000);
      assertEquals(Status.NOVICE, evaluator.evaluate(starting, highWorth, atWeek(9)));
    }
  }

  @Nested
  @DisplayName("INVESTOR")
  class InvestorStatus {

    @Test
    @DisplayName("returns INVESTOR at week 10 with 20% growth")
    void investorAtWeek10() {
      BigDecimal starting = BigDecimal.valueOf(1000);
      BigDecimal worth = BigDecimal.valueOf(1200); // 1.2x
      assertEquals(Status.INVESTOR, evaluator.evaluate(starting, worth, atWeek(10)));
    }

    @Test
    @DisplayName("does not return INVESTOR before week 10 even with 20% growth")
    void notInvestorBeforeWeek10() {
      BigDecimal starting = BigDecimal.valueOf(1000);
      BigDecimal worth = BigDecimal.valueOf(1200);
      assertNotEquals(Status.INVESTOR, evaluator.evaluate(starting, worth, atWeek(9)));
    }

    @Test
    @DisplayName("does not return INVESTOR when growth is below 1.2x at week 10")
    void notInvestorWhenGrowthInsufficient() {
      BigDecimal starting = BigDecimal.valueOf(1000);
      BigDecimal worth = BigDecimal.valueOf(1199);
      assertNotEquals(Status.INVESTOR, evaluator.evaluate(starting, worth, atWeek(10)));
    }
  }

  @Nested
  @DisplayName("SPECULATOR")
  class SpeculatorStatus {

    @Test
    @DisplayName("returns SPECULATOR at week 20 with 200% net worth")
    void speculatorAtWeek20() {
      BigDecimal starting = BigDecimal.valueOf(1000);
      BigDecimal worth = BigDecimal.valueOf(2000); // 2x
      assertEquals(Status.SPECULATOR, evaluator.evaluate(starting, worth, atWeek(20)));
    }

    @Test
    @DisplayName("does not return SPECULATOR before week 20 even if wealthy")
    void notSpeculatorBeforeWeek20() {
      BigDecimal starting = BigDecimal.valueOf(1000);
      BigDecimal worth = BigDecimal.valueOf(10000);
      assertNotEquals(Status.SPECULATOR, evaluator.evaluate(starting, worth, atWeek(19)));
    }

    @Test
    @DisplayName("does not return SPECULATOR at week 20 when wealth below 2x")
    void notSpeculatorWhenWealthInsufficient() {
      BigDecimal starting = BigDecimal.valueOf(1000);
      BigDecimal worth = BigDecimal.valueOf(1999);
      assertNotEquals(Status.SPECULATOR, evaluator.evaluate(starting, worth, atWeek(20)));
    }
  }
}
