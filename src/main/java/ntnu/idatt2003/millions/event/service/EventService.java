package ntnu.idatt2003.millions.event.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import ntnu.idatt2003.millions.event.model.EventSeverity;
import ntnu.idatt2003.millions.event.model.EventTemplate;
import ntnu.idatt2003.millions.event.model.MarketEvent;
import ntnu.idatt2003.millions.market.model.Sector;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.shared.config.SeverityTuning;
import ntnu.idatt2003.millions.shared.observer.GameEvent;
import ntnu.idatt2003.millions.shared.observer.Observable;

/**
 * Central state holder for active {@link MarketEvent}s and the weekly fire schedule.
 *
 * <p>Called once per tick by
 * {@link ntnu.idatt2003.millions.market.service.ExchangeService#tick()} before
 * per-stock price updates. The service:
 * <ol>
 *   <li>Rolls a new {@link WeekBudget} at the first tick of each calendar week.</li>
 *   <li>Decrements and expires active events.</li>
 *   <li>Fires any events scheduled for the current tick, notifying observers
 *       with {@link GameEvent#NEWS_EVENT_FIRED}.</li>
 * </ol>
 *
 * <p>{@link #modifierFor(Stock)} returns the net signed price modifier for a
 * stock, summed across all active events weighted by the stock's sector membership.
 */
public class EventService extends Observable {

  // Random multiplier applied to each event's declared impact: uniform on [0.5, 1.5].
  private static final double RANDOM_FACTOR_MIN = 0.5;
  private static final double RANDOM_FACTOR_RANGE = 1.0;

  private final List<EventTemplate> templates;
  private final List<MarketEvent> activeEvents = new ArrayList<>();
  private final List<MarketEvent> history = new ArrayList<>();
  private WeekBudget currentBudget;

  /**
   * Constructs a service backed by the given templates.
   *
   * @param templates list of available event templates; must contain at least one
   *                  template per severity level
   */
  public EventService(List<EventTemplate> templates) {
    this.templates = List.copyOf(templates);
  }

  /**
   * Advances the event system by one tick.
   *
   * <p>At the start of each calendar week (when
   * {@code currentTick % ticksPerWeek == 0}), a new weekly budget is rolled.
   * Active events are decremented and expired, then scheduled fire ticks
   * for the current tick are processed.
   *
   * @param currentTick the tick count before the exchange increments it
   * @param settings    game settings supplying the calendar and event tuning
   * @param random      the shared random source for reproducible simulation
   */
  public void tick(long currentTick, GameSettings settings, Random random) {
    long ticksPerWeek = (long) settings.hoursPerDay() * settings.daysPerWeek();

    if (currentTick % ticksPerWeek == 0) {
      rollNewWeekBudget(currentTick, settings, random, ticksPerWeek);
    }

    // Decrement remaining ticks and remove expired events.
    activeEvents.replaceAll(e -> new MarketEvent(
        e.id(), e.headline(), e.body(), e.severity(), e.sectors(),
        e.signedImpact(), e.remainingTicks() - 1, e.tickFired()));
    activeEvents.removeIf(e -> e.remainingTicks() <= 0);

    // Fire events scheduled for this tick.
    while (currentBudget != null
        && !currentBudget.fireTicks().isEmpty()
        && currentBudget.fireTicks().peek().equals(currentTick)) {
      currentBudget.fireTicks().poll();
      MarketEvent event = fireEvent(currentTick, settings, random);
      activeEvents.add(event);
      // History is newest-first.
      history.add(0, event);
      notifyObservers(GameEvent.NEWS_EVENT_FIRED);
    }
  }

  /**
   * Returns the net signed price modifier for the given stock.
   *
   * <p>Sums {@code event.signedImpact × stock.sectorWeight(sector)} across all
   * active events and all sectors they affect. A multi-sector stock receives a
   * proportionally smaller share of each event's impact.
   *
   * @param stock the stock to compute a modifier for
   * @return net modifier; positive means upward pressure, negative means downward
   */
  public double modifierFor(Stock stock) {
    double total = 0.0;
    for (MarketEvent event : activeEvents) {
      for (Sector sector : event.sectors()) {
        total += event.signedImpact() * stock.sectorWeight(sector);
      }
    }
    return total;
  }

  /**
   * Returns an unmodifiable view of currently active events.
   *
   * @return active events, in the order they were added
   */
  public List<MarketEvent> getActiveEvents() {
    return Collections.unmodifiableList(activeEvents);
  }

  /**
   * Returns an unmodifiable view of all events ever fired, newest first.
   *
   * @return full event history
   */
  public List<MarketEvent> getHistory() {
    return Collections.unmodifiableList(history);
  }

  // ------------- private helpers -------------

  private void rollNewWeekBudget(long weekStartTick, GameSettings settings,
      Random random, long ticksPerWeek) {
    List<Double> probs = settings.weeklyEventProbabilities();
    int count = rollFromDistribution(probs, random);

    Set<Long> chosen = new HashSet<>();
    while (chosen.size() < count) {
      long tick = weekStartTick + (long) (random.nextDouble() * ticksPerWeek);
      chosen.add(tick);
    }

    // Sort ascending into a FIFO deque.
    Deque<Long> fireTicks = new ArrayDeque<>(new TreeSet<>(chosen));
    currentBudget = new WeekBudget(weekStartTick, fireTicks);
  }

  private int rollFromDistribution(List<Double> probs, Random random) {
    double r = random.nextDouble();
    double cumulative = 0.0;
    for (int i = 0; i < probs.size(); i++) {
      cumulative += probs.get(i);
      if (r < cumulative) {
        return i;
      }
    }
    return probs.size() - 1;
  }

  private MarketEvent fireEvent(long currentTick, GameSettings settings, Random random) {
    EventSeverity severity = rollSeverity(settings, random);
    SeverityTuning tuning = settings.severityTuning().get(severity.name());

    List<EventTemplate> matching = templates.stream()
        .filter(t -> t.severity() == severity)
        .toList();
    EventTemplate template = matching.get(random.nextInt(matching.size()));

    double baseImpact = tuning.impactMin()
        + random.nextDouble() * (tuning.impactMax() - tuning.impactMin());
    double randomFactor = RANDOM_FACTOR_MIN + random.nextDouble() * RANDOM_FACTOR_RANGE;
    double signedImpact = (template.positive() ? 1.0 : -1.0) * baseImpact * randomFactor;

    int durationRange = tuning.durationMaxHours() - tuning.durationMinHours();
    int duration = tuning.durationMinHours()
        + (durationRange > 0 ? random.nextInt(durationRange + 1) : 0);

    return new MarketEvent(
        template.id(),
        template.headline(),
        template.body(),
        severity,
        template.sectors(),
        signedImpact,
        duration,
        currentTick
    );
  }

  private EventSeverity rollSeverity(GameSettings settings, Random random) {
    double r = random.nextDouble();
    double cumulative = 0.0;
    for (EventSeverity severity : EventSeverity.values()) {
      cumulative += settings.severityTuning().get(severity.name()).probability();
      if (r < cumulative) {
        return severity;
      }
    }
    return EventSeverity.MINOR;
  }

  /**
   * Weekly fire schedule: how many events are pre-scheduled and at which ticks.
   *
   * <p>{@code fireTicks} is a sorted FIFO of remaining tick offsets within the
   * week. Severity is not baked in — it is rolled at fire time.
   */
  private record WeekBudget(long weekStartTick, Deque<Long> fireTicks) {}
}
