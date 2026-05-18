package ntnu.idatt2003.millions.app.config;

import ntnu.idatt2003.millions.event.service.EventService;
import ntnu.idatt2003.millions.market.service.GameClock;
import ntnu.idatt2003.millions.order.service.OrderService;
import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.transaction.service.DividendService;

/**
 * Immutable context threaded through the application for a single game session.
 *
 * <p>Constructed once in StartController and
 * passed down to every subsystem that needs configuration or shared services.
 */
public record GameContext(
    GameSettings settings,
    GameClock gameClock,
    OrderService orderService,
    DividendService dividendService,
    EventService eventService
) {}
