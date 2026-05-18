package ntnu.idatt2003.millions.shared.config;

import java.util.List;
import java.util.Map;

/**
 * Immutable settings snapshot for a single game session.
 *
 * <p>Constructed once at game start via
 * {@link GameDefaults#forDifficulty(Difficulty)} and threaded through
 * the application via {@link ntnu.idatt2003.millions.app.config.GameContext}.
 * All subsystems that need configuration read from this record rather than
 * from ad-hoc constants.
 */
public record GameSettings(
    Difficulty difficulty,
    int aiCount,
    long randomSeed,
    double volatility,
    double driftBias,
    int hoursPerDay,
    int daysPerWeek,
    int weeksPerMonth,
    List<Double> weeklyEventProbabilities,
    Map<String, SeverityTuning> severityTuning
) {
}
