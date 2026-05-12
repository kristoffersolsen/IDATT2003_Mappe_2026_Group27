package ntnu.idatt2003.millions.app.config;

import java.util.Random;
import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.shared.time.GameTime;
import ntnu.idatt2003.millions.market.service.GameClock;
import ntnu.idatt2003.millions.transaction.service.DividendService;
import ntnu.idatt2003.millions.order.service.OrderService;

/**
 * Immutable context threaded through the application for a single game session.
 *
 * <p>Constructed once in StartController and
 * passed down to every subsystem that needs configuration or shared services.
 * Fields introduced in later checkpoints are {@code null} until their
 * checkpoint populates them.
 */
public record GameContext(
    GameSettings settings,
    Random random,
    GameClock gameClock,
    GameTime gameTime,
    OrderService orderService,
    DividendService dividendService
) {
}
