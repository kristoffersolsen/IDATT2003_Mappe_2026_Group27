package ntnu.idatt2003.millions.model.observer;

/**
 * Typed events that model classes can fire to their observers.
 *
 * <p>Using a typed enum instead of raw strings gives us compile-time safety
 * and makes it easy to add new event categories without breaking existing listeners.
 */
public enum GameEvent {

  /**
   * The exchange has advanced by one simulated hour and stock prices have changed.
   */
  HOUR_ADVANCED,

  /**
   * A skip (one or more hours) has completed; all price updates for that skip are done.
   */
  SKIP_COMPLETED,

  /**
   * A stock was purchased.
   */
  STOCK_PURCHASED,

  /**
   * A stock was sold.
   */
  STOCK_SOLD,

  /**
   * The player's cash balance has changed.
   */
  BALANCE_CHANGED,

  /**
   * The player's portfolio composition has changed.
   */
  PORTFOLIO_CHANGED,

  /**
   * A limit order was placed and its cash (for buys) reserved.
   */
  LIMIT_ORDER_PLACED,

  /**
   * A pending limit order triggered and was executed against the market.
   */
  LIMIT_ORDER_EXECUTED,

  /**
   * A pending limit order was cancelled and its reserved cash refunded.
   */
  LIMIT_ORDER_CANCELLED
}