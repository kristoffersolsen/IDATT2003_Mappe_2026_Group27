package ntnu.idatt2003.millions.order.model;

import java.math.BigDecimal;

/**
 * An immutable limit order placed by a player.
 *
 * <p>A {@link OrderType#LIMIT_BUY} executes when the stock price falls to or
 * below {@code triggerPrice}. A {@link OrderType#LIMIT_SELL} executes when it
 * rises to or above. For limit buys, cash equal to
 * {@code quantity × triggerPrice + commission} is reserved on placement and
 * released on cancellation or converted to shares on execution.
 *
 * @param stockSymbol  the symbol of the stock this order targets
 * @param type         {@link OrderType#LIMIT_BUY} or {@link OrderType#LIMIT_SELL}
 * @param quantity     the number of shares to trade (must be positive)
 * @param triggerPrice the price at which this order activates
 * @param placedAtTick the simulation tick when the order was placed
 */
public record LimitOrder(
    String stockSymbol,
    OrderType type,
    BigDecimal quantity,
    BigDecimal triggerPrice,
    long placedAtTick
) {
}
