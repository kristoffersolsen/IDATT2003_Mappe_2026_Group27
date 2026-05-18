package ntnu.idatt2003.millions.shared.config;

/**
 * Tuning parameters for a single event severity level.
 *
 * <p>Loaded from {@code config/defaults.properties} via {@link GameDefaults}
 * and stored in {@link GameSettings#severityTuning()}, keyed by the severity
 * name (e.g. {@code "MINOR"}).
 */
public record SeverityTuning(
    double probability,
    double impactMin,
    double impactMax,
    int durationMinHours,
    int durationMaxHours
) {}
