package ntnu.idatt2003.millions.event.model;

import java.util.Set;
import ntnu.idatt2003.millions.market.model.Sector;

/**
 * Immutable blueprint for a category of market event.
 *
 * <p>Loaded from {@code data/events/events.json} by
 * {@link ntnu.idatt2003.millions.event.service.EventTemplateLoader}.
 * When a template fires, a concrete {@link MarketEvent} is constructed
 * with severity, impact, and duration rolled at fire time.
 *
 * @param id       unique identifier used for deduplication and history
 * @param headline short headline shown in the news panel
 * @param body     longer description shown in the event card
 * @param sectors  market sectors affected when this template fires
 * @param severity severity tier ({@code MINOR}, {@code MAJOR}, or {@code CRISIS})
 * @param positive {@code true} if the event drives prices up, {@code false} for down
 */
public record EventTemplate(
    String id,
    String headline,
    String body,
    Set<Sector> sectors,
    EventSeverity severity,
    boolean positive
) {}
