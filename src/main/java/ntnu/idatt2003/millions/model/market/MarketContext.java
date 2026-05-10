package ntnu.idatt2003.millions.model.market;

import java.util.Random;
import ntnu.idatt2003.millions.config.GameSettings;
import ntnu.idatt2003.millions.service.ExchangeService;

/**
 * Immutable bundle of everything {@link PriceModel} needs to compute
 * the next price for a stock.
 *
 * <p>Held by {@link ExchangeService} and passed to
 * {@link PriceModel#nextPrice} on every tick. Placeholder fields are
 * present for forward compatibility and default to zero until the
 * checkpoints that introduce them:
 * <ul>
 *   <li>{@code netDemand} — populated in checkpoint 4 (limit orders)</li>
 *   <li>{@code eventModifier} — populated in checkpoint 3 (random events)</li>
 * </ul>
 */
public record MarketContext(
    GameSettings settings,
    Random random,
    double netDemand,
    double eventModifier
) {
}
