package org.example.config;

import java.util.Random;
import org.example.model.time.GameClock;
import org.example.model.time.GameTime;
import org.example.service.DividendService;
import org.example.service.OrderService;

/**
 * Immutable context threaded through the application for a single game session.
 *
 * <p>Constructed once in {@link org.example.controller.StartController} and
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
) {}
