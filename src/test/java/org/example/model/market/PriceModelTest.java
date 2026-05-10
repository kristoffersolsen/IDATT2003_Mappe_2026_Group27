package org.example.model.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Random;
import org.example.config.Difficulty;
import org.example.config.GameSettings;
import org.example.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PriceModel")
class PriceModelTest {

  private static final long SEED = 42L;
  private static final double NORMAL_VOLATILITY = 0.008;
  private static final double ZERO_VOLATILITY = 0.0;
  private static final double ZERO_DRIFT = 0.0;
  private static final double POSITIVE_DRIFT = 0.05;
  private static final double NEGATIVE_DRIFT = -0.05;
  private static final BigDecimal INITIAL_PRICE = BigDecimal.valueOf(100);
  private static final int SAMPLE_SIZE = 1000;
  private static final double VARIANCE_TOLERANCE = 0.003;
  private static final double MEAN_TOLERANCE = 0.005;

  private PriceModel model;
  private Stock stock;

  @BeforeEach
  void setUp() {
    model = new PriceModel();
    stock = new Stock("TEST", "Test Co", INITIAL_PRICE);
  }

  // ------------- determinism -------------

  @Nested
  @DisplayName("determinism")
  class Determinism {

    @Test
    @DisplayName("same seed produces identical next price")
    void nextPrice_sameSeedGivesSameResult() {
      GameSettings settings = new GameSettings(
          Difficulty.NORMAL, 0, SEED, NORMAL_VOLATILITY, ZERO_DRIFT, 8, 5, 4);
      MarketContext ctx1 = new MarketContext(settings, new Random(SEED), 0.0, 0.0);
      MarketContext ctx2 = new MarketContext(settings, new Random(SEED), 0.0, 0.0);

      assertEquals(model.nextPrice(stock, ctx1), model.nextPrice(stock, ctx2));
    }
  }

  // ------------- drift bias -------------

  @Nested
  @DisplayName("drift bias")
  class DriftBias {

    @Test
    @DisplayName("positive drift always raises price when volatility is zero")
    void nextPrice_positiveDriftRaisesPrice() {
      GameSettings settings = new GameSettings(
          Difficulty.EASY, 0, SEED, ZERO_VOLATILITY, POSITIVE_DRIFT, 8, 5, 4);
      MarketContext ctx = new MarketContext(settings, new Random(SEED), 0.0, 0.0);

      BigDecimal newPrice = model.nextPrice(stock, ctx);

      assertTrue(newPrice.compareTo(INITIAL_PRICE) > 0,
          "Positive drift with zero volatility must raise the price");
    }

    @Test
    @DisplayName("negative drift always lowers price when volatility is zero")
    void nextPrice_negativeDriftLowersPrice() {
      GameSettings settings = new GameSettings(
          Difficulty.HARD, 0, SEED, ZERO_VOLATILITY, NEGATIVE_DRIFT, 8, 5, 4);
      MarketContext ctx = new MarketContext(settings, new Random(SEED), 0.0, 0.0);

      BigDecimal newPrice = model.nextPrice(stock, ctx);

      assertTrue(newPrice.compareTo(INITIAL_PRICE) < 0,
          "Negative drift with zero volatility must lower the price");
    }

    @Test
    @DisplayName("positive drift shifts sample mean above zero")
    void nextPrice_positiveDriftShiftsMeanUp() {
      GameSettings settings = new GameSettings(
          Difficulty.EASY, 0, SEED, NORMAL_VOLATILITY, POSITIVE_DRIFT, 8, 5, 4);
      MarketContext ctx = new MarketContext(settings, new Random(SEED), 0.0, 0.0);
      double sumReturns = 0.0;

      for (int i = 0; i < SAMPLE_SIZE; i++) {
        BigDecimal newPrice = model.nextPrice(stock, ctx);
        sumReturns += newPrice.doubleValue() / INITIAL_PRICE.doubleValue() - 1.0;
      }

      double meanReturn = sumReturns / SAMPLE_SIZE;
      assertTrue(meanReturn > 0,
          "Mean return should be positive with positive drift bias");
    }
  }

  // ------------- variance -------------

  @Nested
  @DisplayName("variance")
  class Variance {

    @Test
    @DisplayName("sample standard deviation is close to configured volatility")
    void nextPrice_varianceMatchesVolatility() {
      double testVolatility = 0.02;
      GameSettings settings = new GameSettings(
          Difficulty.NORMAL, 0, SEED, testVolatility, ZERO_DRIFT, 8, 5, 4);
      MarketContext ctx = new MarketContext(settings, new Random(SEED), 0.0, 0.0);
      double[] returns = new double[SAMPLE_SIZE];

      for (int i = 0; i < SAMPLE_SIZE; i++) {
        BigDecimal newPrice = model.nextPrice(stock, ctx);
        returns[i] = newPrice.doubleValue() / INITIAL_PRICE.doubleValue() - 1.0;
      }

      double mean = computeMean(returns);
      double stdDev = computeStdDev(returns, mean);
      assertEquals(testVolatility, stdDev, VARIANCE_TOLERANCE,
          "Sample std dev should be close to configured volatility");
    }

    @Test
    @DisplayName("zero drift bias keeps sample mean near zero")
    void nextPrice_zeroDriftKeepsMeanNearZero() {
      GameSettings settings = new GameSettings(
          Difficulty.NORMAL, 0, SEED, NORMAL_VOLATILITY, ZERO_DRIFT, 8, 5, 4);
      MarketContext ctx = new MarketContext(settings, new Random(SEED), 0.0, 0.0);
      double sumReturns = 0.0;

      for (int i = 0; i < SAMPLE_SIZE; i++) {
        BigDecimal newPrice = model.nextPrice(stock, ctx);
        sumReturns += newPrice.doubleValue() / INITIAL_PRICE.doubleValue() - 1.0;
      }

      double mean = sumReturns / SAMPLE_SIZE;
      assertEquals(0.0, mean, MEAN_TOLERANCE,
          "Mean return should be near zero with zero drift bias");
    }
  }

  // ------------- placeholders -------------

  @Nested
  @DisplayName("placeholder fields")
  class Placeholders {

    @Test
    @DisplayName("non-zero net demand shifts price in same direction as drift")
    void nextPrice_netDemandContributesToFactor() {
      double demand = 0.1;
      GameSettings settings = new GameSettings(
          Difficulty.NORMAL, 0, SEED, ZERO_VOLATILITY, ZERO_DRIFT, 8, 5, 4);
      MarketContext ctx = new MarketContext(settings, new Random(SEED), demand, 0.0);

      BigDecimal newPrice = model.nextPrice(stock, ctx);

      assertTrue(newPrice.compareTo(INITIAL_PRICE) > 0,
          "Positive net demand with zero noise must raise the price");
    }

    @Test
    @DisplayName("non-zero event modifier shifts price in same direction")
    void nextPrice_eventModifierContributesToFactor() {
      double modifier = -0.1;
      GameSettings settings = new GameSettings(
          Difficulty.NORMAL, 0, SEED, ZERO_VOLATILITY, ZERO_DRIFT, 8, 5, 4);
      MarketContext ctx = new MarketContext(settings, new Random(SEED), 0.0, modifier);

      BigDecimal newPrice = model.nextPrice(stock, ctx);

      assertTrue(newPrice.compareTo(INITIAL_PRICE) < 0,
          "Negative event modifier with zero noise must lower the price");
    }
  }

  // ------------- helpers -------------

  private static double computeMean(double[] values) {
    double sum = 0.0;
    for (double v : values) {
      sum += v;
    }
    return sum / values.length;
  }

  private static double computeStdDev(double[] values, double mean) {
    double sumSq = 0.0;
    for (double v : values) {
      double diff = v - mean;
      sumSq += diff * diff;
    }
    return Math.sqrt(sumSq / values.length);
  }
}
