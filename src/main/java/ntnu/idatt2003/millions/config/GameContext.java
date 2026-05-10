package ntnu.idatt2003.millions.config;

import java.util.Random;
import ntnu.idatt2003.millions.controller.StartController;
import ntnu.idatt2003.millions.model.time.GameClock;
import ntnu.idatt2003.millions.model.time.GameTime;
import ntnu.idatt2003.millions.service.DividendService;
import ntnu.idatt2003.millions.service.OrderService;

/**
 * Immutable context threaded through the application for a single game session.
 *
 * <p>Constructed once in {@link StartController} and
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
