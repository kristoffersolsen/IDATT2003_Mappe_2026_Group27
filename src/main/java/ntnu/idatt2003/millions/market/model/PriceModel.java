package ntnu.idatt2003.millions.market.model;

import java.math.BigDecimal;
import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.market.model.Stock;

/**
 * Computes the next price for a stock using a geometric random walk
 * with Gaussian noise.
 *
 * <p>The formula is:
 * <pre>
 *   nextPrice = currentPrice * (1 + driftBias + netDemand + eventModifier
 *                                 + volatility * gaussian())
 * </pre>
 *
 * <p>{@code volatility} and {@code driftBias} come from
 * {@link GameSettings}. At hard difficulty, a small
 * negative {@code driftBias} means the median outcome is a loss, forcing
 * active stock-picking to win.
 *
 * <p>{@code PriceModel} is stateless. All per-tick state lives in the
 * {@link MarketContext} passed by the caller.
 */
public class PriceModel {

  /**
   * Computes the next sales price for the given stock.
   *
   * @param stock   the stock whose current price is the base
   * @param context market context providing settings and random source
   * @return the new price
   */
  public BigDecimal nextPrice(Stock stock, MarketContext context) {
    double gaussian = context.random().nextGaussian();
    double factor = 1.0
        + context.settings().driftBias()
        + context.netDemand()
        + context.eventModifier()
        + context.settings().volatility() * gaussian;
    return stock.getSalesPrice().multiply(BigDecimal.valueOf(factor));
  }
}
