package ntnu.idatt2003.millions.shared.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads designer-tunable defaults from {@code config/defaults.properties}
 * and constructs a {@link GameSettings} for the requested {@link Difficulty}.
 *
 * <p>Call {@link #forDifficulty(Difficulty)} for the standard production path.
 * Tests can inject a custom {@link Properties} via the package-private
 * constructor to exercise the fallback behaviour without touching the file.
 */
public final class GameDefaults {

  private static final Logger log = LoggerFactory.getLogger(GameDefaults.class);

  private static final String PROPERTIES_PATH = "/config/defaults.properties";

  // ------------- Hardcoded fallback values -----------------------------------

  private static final int DEFAULT_AI_COUNT = 0;
  private static final double EASY_VOLATILITY = 0.005;
  private static final double NORMAL_VOLATILITY = 0.008;
  private static final double HARD_VOLATILITY = 0.012;
  private static final double EASY_DRIFT_BIAS = 0.0002;
  private static final double NORMAL_DRIFT_BIAS = 0.0;
  private static final double HARD_DRIFT_BIAS = -0.0001;
  private static final int DEFAULT_HOURS_PER_DAY = 8;
  private static final int DEFAULT_DAYS_PER_WEEK = 5;
  private static final int DEFAULT_WEEKS_PER_MONTH = 4;

  // Default weekly event probability distribution {0, 1, 2, 3, 4 events}.
  private static final List<Double> DEFAULT_WEEKLY_PROBS =
      List.of(0.10, 0.25, 0.30, 0.25, 0.10);

  private static final SeverityTuning DEFAULT_MINOR =
      new SeverityTuning(0.70, 0.01, 0.03, 4, 12);
  private static final SeverityTuning DEFAULT_MAJOR =
      new SeverityTuning(0.25, 0.04, 0.08, 24, 72);
  private static final SeverityTuning DEFAULT_CRISIS =
      new SeverityTuning(0.05, 0.10, 0.20, 48, 168);

  private final Properties properties;

  /**
   * Package-private constructor used in tests to inject a custom
   * {@link Properties} instance and exercise fallback behaviour.
   *
   * @param properties the properties to read configuration from
   */
  GameDefaults(Properties properties) {
    this.properties = properties;
  }

  /**
   * Returns a {@link GameSettings} for the given difficulty using the
   * bundled {@code config/defaults.properties} file.
   *
   * <p>Any missing or unparseable property falls back to a hardcoded
   * default so the game can always start.
   *
   * @param difficulty the difficulty level chosen by the player
   * @return a freshly constructed settings object
   */
  public static GameSettings forDifficulty(Difficulty difficulty) {
    return new GameDefaults(loadProperties()).settingsFor(difficulty);
  }

  /**
   * Returns a {@link GameSettings} for the given difficulty using this
   * instance's properties.
   */
  GameSettings settingsFor(Difficulty difficulty) {
    String key = difficulty.name().toLowerCase();
    double volatility = parseDouble(
        "difficulty." + key + ".volatility", defaultVolatility(difficulty));
    double driftBias = parseDouble(
        "difficulty." + key + ".driftBias", defaultDriftBias(difficulty));
    int hoursPerDay = parseInt("simulation.hoursPerDay", DEFAULT_HOURS_PER_DAY);
    int daysPerWeek = parseInt("simulation.daysPerWeek", DEFAULT_DAYS_PER_WEEK);
    int weeksPerMonth = parseInt("simulation.weeksPerMonth", DEFAULT_WEEKS_PER_MONTH);
    long seed = System.nanoTime();

    return new GameSettings(
        difficulty,
        DEFAULT_AI_COUNT,
        seed,
        volatility,
        driftBias,
        hoursPerDay,
        daysPerWeek,
        weeksPerMonth,
        loadWeeklyProbabilities(),
        loadSeverityTuning()
    );
  }

  private List<Double> loadWeeklyProbabilities() {
    double[] defaults = {0.10, 0.25, 0.30, 0.25, 0.10};
    double[] loaded = new double[defaults.length];
    for (int i = 0; i < defaults.length; i++) {
      loaded[i] = parseDouble("event.weekly.probability." + i, defaults[i]);
    }
    return List.of(loaded[0], loaded[1], loaded[2], loaded[3], loaded[4]);
  }

  private Map<String, SeverityTuning> loadSeverityTuning() {
    Map<String, SeverityTuning> map = new LinkedHashMap<>();
    map.put("MINOR", loadOneSeverity("minor", DEFAULT_MINOR));
    map.put("MAJOR", loadOneSeverity("major", DEFAULT_MAJOR));
    map.put("CRISIS", loadOneSeverity("crisis", DEFAULT_CRISIS));
    return Map.copyOf(map);
  }

  private SeverityTuning loadOneSeverity(String name, SeverityTuning fallback) {
    String prefix = "event.severity." + name + ".";
    double probability = parseDouble(prefix + "probability", fallback.probability());
    double impactMin = parseDouble(prefix + "impactMin", fallback.impactMin());
    double impactMax = parseDouble(prefix + "impactMax", fallback.impactMax());
    int durationMin = parseInt(prefix + "durationMinHours", fallback.durationMinHours());
    int durationMax = parseInt(prefix + "durationMaxHours", fallback.durationMaxHours());
    return new SeverityTuning(probability, impactMin, impactMax, durationMin, durationMax);
  }

  private static Properties loadProperties() {
    Properties props = new Properties();
    try (InputStream in = GameDefaults.class.getResourceAsStream(PROPERTIES_PATH)) {
      if (in == null) {
        log.warn("defaults.properties not found on classpath, using hardcoded defaults");
        return props;
      }
      props.load(in);
    } catch (IOException e) {
      log.error("Failed to load defaults.properties", e);
    }
    return props;
  }

  private double parseDouble(String propKey, double fallback) {
    String value = properties.getProperty(propKey);
    if (value == null) {
      return fallback;
    }
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      log.warn("Invalid value for '{}': '{}', using default {}", propKey, value, fallback);
      return fallback;
    }
  }

  private int parseInt(String propKey, int fallback) {
    String value = properties.getProperty(propKey);
    if (value == null) {
      return fallback;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      log.warn("Invalid value for '{}': '{}', using default {}", propKey, value, fallback);
      return fallback;
    }
  }

  private static double defaultVolatility(Difficulty difficulty) {
    return switch (difficulty) {
      case EASY -> EASY_VOLATILITY;
      case NORMAL -> NORMAL_VOLATILITY;
      case HARD -> HARD_VOLATILITY;
    };
  }

  private static double defaultDriftBias(Difficulty difficulty) {
    return switch (difficulty) {
      case EASY -> EASY_DRIFT_BIAS;
      case NORMAL -> NORMAL_DRIFT_BIAS;
      case HARD -> HARD_DRIFT_BIAS;
    };
  }
}
