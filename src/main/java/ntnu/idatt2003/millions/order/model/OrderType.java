package ntnu.idatt2003.millions.order.model;

/**
 * The type of a limit order.
 *
 * <p>A {@link #LIMIT_BUY} triggers when the stock price falls to or below
 * the trigger price. A {@link #LIMIT_SELL} triggers when it rises to or above.
 */
public enum OrderType {

  /**
   * Buy when the market price reaches or falls below the trigger price.
   */
  LIMIT_BUY,

  /**
   * Sell when the market price reaches or rises above the trigger price.
   */
  LIMIT_SELL
}
