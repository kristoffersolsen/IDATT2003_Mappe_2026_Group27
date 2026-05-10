package ntnu.idatt2003.millions.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GameDefaults")
class GameDefaultsTest {

  private static final double DELTA = 1e-10;

  // ------------- From properties file -------------

  @Nested
  @DisplayName("properties file")
  class PropertiesFile {

    @Test
    @DisplayName("loads EASY volatility from file")
    void forDifficulty_easyVolatility() {
      GameSettings settings = GameDefaults.forDifficulty(Difficulty.EASY);
      assertEquals(0.005, settings.volatility(), DELTA);
    }

    @Test
    @DisplayName("loads NORMAL volatility from file")
    void forDifficulty_normalVolatility() {
      GameSettings settings = GameDefaults.forDifficulty(Difficulty.NORMAL);
      assertEquals(0.008, settings.volatility(), DELTA);
    }

    @Test
    @DisplayName("loads HARD volatility from file")
    void forDifficulty_hardVolatility() {
      GameSettings settings = GameDefaults.forDifficulty(Difficulty.HARD);
      assertEquals(0.012, settings.volatility(), DELTA);
    }

    @Test
    @DisplayName("loads EASY drift bias from file")
    void forDifficulty_easyDriftBias() {
      GameSettings settings = GameDefaults.forDifficulty(Difficulty.EASY);
      assertEquals(0.0002, settings.driftBias(), DELTA);
    }

    @Test
    @DisplayName("loads NORMAL drift bias from file")
    void forDifficulty_normalDriftBias() {
      GameSettings settings = GameDefaults.forDifficulty(Difficulty.NORMAL);
      assertEquals(0.0, settings.driftBias(), DELTA);
    }

    @Test
    @DisplayName("loads HARD drift bias from file")
    void forDifficulty_hardDriftBias() {
      GameSettings settings = GameDefaults.forDifficulty(Difficulty.HARD);
      assertEquals(-0.0001, settings.driftBias(), DELTA);
    }

    @Test
    @DisplayName("loads simulation constants from file")
    void forDifficulty_simulationConstants() {
      GameSettings settings = GameDefaults.forDifficulty(Difficulty.NORMAL);
      assertEquals(8, settings.hoursPerDay());
      assertEquals(5, settings.daysPerWeek());
      assertEquals(4, settings.weeksPerMonth());
    }

    @Test
    @DisplayName("returns zero AI count")
    void forDifficulty_aiCountIsZero() {
      GameSettings settings = GameDefaults.forDifficulty(Difficulty.NORMAL);
      assertEquals(0, settings.aiCount());
    }

    @Test
    @DisplayName("returns the requested difficulty")
    void forDifficulty_difficultyMatches() {
      assertEquals(Difficulty.HARD, GameDefaults.forDifficulty(Difficulty.HARD).difficulty());
    }
  }

  // ------------- Fallback behaviour -------------

  @Nested
  @DisplayName("fallback on missing properties")
  class Fallback {

    @Test
    @DisplayName("falls back to NORMAL volatility when properties empty")
    void settingsFor_normalVolatilityFallback() {
      GameDefaults defaults = new GameDefaults(new Properties());
      GameSettings settings = defaults.settingsFor(Difficulty.NORMAL);
      assertEquals(0.008, settings.volatility(), DELTA);
    }

    @Test
    @DisplayName("falls back to HARD volatility when properties empty")
    void settingsFor_hardVolatilityFallback() {
      GameDefaults defaults = new GameDefaults(new Properties());
      GameSettings settings = defaults.settingsFor(Difficulty.HARD);
      assertEquals(0.012, settings.volatility(), DELTA);
    }

    @Test
    @DisplayName("falls back to EASY drift bias when properties empty")
    void settingsFor_easyDriftBiasFallback() {
      GameDefaults defaults = new GameDefaults(new Properties());
      GameSettings settings = defaults.settingsFor(Difficulty.EASY);
      assertEquals(0.0002, settings.driftBias(), DELTA);
    }

    @Test
    @DisplayName("falls back to simulation constants when properties empty")
    void settingsFor_simulationConstantsFallback() {
      GameDefaults defaults = new GameDefaults(new Properties());
      GameSettings settings = defaults.settingsFor(Difficulty.NORMAL);
      assertEquals(8, settings.hoursPerDay());
      assertEquals(5, settings.daysPerWeek());
      assertEquals(4, settings.weeksPerMonth());
    }

    @Test
    @DisplayName("ignores unparseable double and uses fallback")
    void settingsFor_invalidDoubleUsesDefault() {
      Properties props = new Properties();
      props.setProperty("difficulty.normal.volatility", "not-a-number");
      GameDefaults defaults = new GameDefaults(props);
      GameSettings settings = defaults.settingsFor(Difficulty.NORMAL);
      assertEquals(0.008, settings.volatility(), DELTA);
    }

    @Test
    @DisplayName("ignores unparseable int and uses fallback")
    void settingsFor_invalidIntUsesDefault() {
      Properties props = new Properties();
      props.setProperty("simulation.hoursPerDay", "eight");
      GameDefaults defaults = new GameDefaults(props);
      GameSettings settings = defaults.settingsFor(Difficulty.NORMAL);
      assertEquals(8, settings.hoursPerDay());
    }

    @Test
    @DisplayName("returns a non-null settings instance when properties empty")
    void settingsFor_returnsNonNull() {
      GameDefaults defaults = new GameDefaults(new Properties());
      assertNotNull(defaults.settingsFor(Difficulty.NORMAL));
    }
  }
}
