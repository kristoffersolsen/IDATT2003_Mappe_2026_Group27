package ntnu.idatt2003.millions.event.model;

/**
 * Severity level of a fired {@link MarketEvent}.
 *
 * <p>Rolled at fire time from the probability distribution in
 * {@link ntnu.idatt2003.millions.shared.config.GameSettings#severityTuning()}.
 */
public enum EventSeverity {

  /** Short-lived, small-impact event. 70% of fires by default. */
  MINOR,

  /** Medium-duration, noticeable-impact event. 25% of fires by default. */
  MAJOR,

  /** Long-duration, large-impact event. 5% of fires by default. */
  CRISIS
}
