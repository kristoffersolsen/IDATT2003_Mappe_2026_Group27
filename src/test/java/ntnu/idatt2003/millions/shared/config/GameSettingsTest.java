package ntnu.idatt2003.millions.shared.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GameSettings")
class GameSettingsTest {

  private static final long SEED = 42L;
  private static final double VOLATILITY = 0.008;
  private static final double DRIFT_BIAS = 0.0;
  private static final int HOURS_PER_DAY = 8;
  private static final int DAYS_PER_WEEK = 5;
  private static final int WEEKS_PER_MONTH = 4;

  private static final List<Double> DEFAULT_PROBS = List.of(0.10, 0.25, 0.30, 0.25, 0.10);
  private static final Map<String, SeverityTuning> DEFAULT_TUNING = Map.of(
      "MINOR", new SeverityTuning(0.70, 0.01, 0.03, 4, 12),
      "MAJOR", new SeverityTuning(0.25, 0.04, 0.08, 24, 72),
      "CRISIS", new SeverityTuning(0.05, 0.10, 0.20, 48, 168));

  @Nested
  @DisplayName("fields")
  class Fields {

    @Test
    @DisplayName("records all components correctly")
    void fields_roundTrip() {
      GameSettings settings = new GameSettings(
          Difficulty.NORMAL, 0, SEED, VOLATILITY, DRIFT_BIAS,
          HOURS_PER_DAY, DAYS_PER_WEEK, WEEKS_PER_MONTH,
          DEFAULT_PROBS, DEFAULT_TUNING);

      assertEquals(Difficulty.NORMAL, settings.difficulty());
      assertEquals(0, settings.aiCount());
      assertEquals(SEED, settings.randomSeed());
      assertEquals(VOLATILITY, settings.volatility());
      assertEquals(DRIFT_BIAS, settings.driftBias());
      assertEquals(HOURS_PER_DAY, settings.hoursPerDay());
      assertEquals(DAYS_PER_WEEK, settings.daysPerWeek());
      assertEquals(WEEKS_PER_MONTH, settings.weeksPerMonth());
    }
  }

  @Nested
  @DisplayName("equality")
  class Equality {

    @Test
    @DisplayName("equal when all components match")
    void equals_sameComponents() {
      GameSettings a = new GameSettings(
          Difficulty.EASY, 0, SEED, 0.005, 0.0002,
          HOURS_PER_DAY, DAYS_PER_WEEK, WEEKS_PER_MONTH,
          DEFAULT_PROBS, DEFAULT_TUNING);
      GameSettings b = new GameSettings(
          Difficulty.EASY, 0, SEED, 0.005, 0.0002,
          HOURS_PER_DAY, DAYS_PER_WEEK, WEEKS_PER_MONTH,
          DEFAULT_PROBS, DEFAULT_TUNING);

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("not equal when difficulty differs")
    void equals_differentDifficulty() {
      GameSettings easy = new GameSettings(
          Difficulty.EASY, 0, SEED, 0.005, 0.0002,
          HOURS_PER_DAY, DAYS_PER_WEEK, WEEKS_PER_MONTH,
          DEFAULT_PROBS, DEFAULT_TUNING);
      GameSettings hard = new GameSettings(
          Difficulty.HARD, 0, SEED, 0.012, -0.0001,
          HOURS_PER_DAY, DAYS_PER_WEEK, WEEKS_PER_MONTH,
          DEFAULT_PROBS, DEFAULT_TUNING);

      assertNotEquals(easy, hard);
    }
  }

  @Nested
  @DisplayName("difficulty constants")
  class DifficultyConstants {

    @Test
    @DisplayName("all three difficulty levels are available")
    void difficulty_allValues() {
      assertEquals(3, Difficulty.values().length);
    }

    @Test
    @DisplayName("EASY toString is human-readable")
    void difficulty_easyToString() {
      assertEquals("Easy", Difficulty.EASY.toString());
    }

    @Test
    @DisplayName("NORMAL toString is human-readable")
    void difficulty_normalToString() {
      assertEquals("Normal", Difficulty.NORMAL.toString());
    }

    @Test
    @DisplayName("HARD toString is human-readable")
    void difficulty_hardToString() {
      assertEquals("Hard", Difficulty.HARD.toString());
    }
  }
}
