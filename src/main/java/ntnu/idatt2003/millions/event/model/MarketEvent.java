package ntnu.idatt2003.millions.event.model;

import java.util.Set;
import ntnu.idatt2003.millions.market.model.Sector;

/**
 * An active market event produced when an {@link EventTemplate} fires.
 *
 * <p>Immutable value object. Stored in
 * {@link ntnu.idatt2003.millions.event.service.EventService}'s active list and
 * replaced each tick with a decremented {@code remainingTicks} value. Removed
 * when {@code remainingTicks} reaches zero.
 *
 * @param id             template identifier this event was instantiated from
 * @param headline       short headline shown in the news panel
 * @param body           longer description shown in the event card
 * @param severity       severity tier of this event instance
 * @param sectors        market sectors affected by this event
 * @param signedImpact   signed price modifier already multiplied by the random
 *                       {@code [0.5, 1.5]} per-event factor and direction sign
 * @param remainingTicks how many more ticks this event remains active
 * @param tickFired      the tick at which this event was created
 */
public record MarketEvent(
    String id,
    String headline,
    String body,
    EventSeverity severity,
    Set<Sector> sectors,
    double signedImpact,
    long remainingTicks,
    long tickFired
) {}
