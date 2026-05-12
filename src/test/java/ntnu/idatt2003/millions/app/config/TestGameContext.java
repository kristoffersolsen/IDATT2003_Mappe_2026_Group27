package ntnu.idatt2003.millions.app.config;

import java.util.Random;
import ntnu.idatt2003.millions.shared.config.Difficulty;
import ntnu.idatt2003.millions.shared.config.GameDefaults;
import ntnu.idatt2003.millions.shared.config.GameSettings;

/**
 * Factory helper for constructing a {@link GameContext} in tests.
 *
 * <p>Use {@link #defaults()} instead of copy-pasting context construction
 * across test classes. The returned context uses {@link Difficulty#NORMAL}
 * settings loaded from the bundled properties file.
 */
public final class TestGameContext {

  private TestGameContext() {
  }

  /**
   * Returns a {@link GameContext} with {@link Difficulty#NORMAL} settings,
   * suitable as a default for tests that need a context but don't test
   * difficulty-specific behaviour.
   *
   * @return a context with normal-difficulty settings and a seeded random
   */
  public static GameContext defaults() {
    GameSettings settings = GameDefaults.forDifficulty(Difficulty.NORMAL);
    return new GameContext(
        settings,
        new Random(settings.randomSeed()),
        null,
        null,
        null,
        null
    );
  }
}
