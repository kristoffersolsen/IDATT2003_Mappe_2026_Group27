package ntnu.idatt2003.millions.market.model;

import java.util.Random;
import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.market.service.ExchangeService;

/**
 * Immutable bundle of everything {@link PriceModel} needs to compute
 * the next price for a single stock on a single tick.
 *
 * <p>A fresh instance is constructed per stock per tick by
 * {@link ExchangeService#tick()}, so {@code eventModifier} carries
 * the modifier for the specific stock this context was built for.
 * Placeholder fields default to zero.
 */
public record MarketContext(
    GameSettings settings,
    Random random,
    double netDemand,
    double eventModifier
) {
}
