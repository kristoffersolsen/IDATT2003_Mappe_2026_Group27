package org.example.model.observer;

/**
 * Typed events that model classes can fire to their observers.
 *
 * <p>Using a typed enum instead of raw strings gives us compile-time safety
 * and makes it easy to add new event categories without breaking existing listeners.
 */
public enum GameEvent {

  /** The exchange has advanced to a new week and stock prices have changed. */
  WEEK_ADVANCED,

  /** A stock was purchased. */
  STOCK_PURCHASED,

  /** A stock was sold. */
  STOCK_SOLD,

  /** The player's cash balance has changed. */
  BALANCE_CHANGED,

  /** The player's portfolio composition has changed. */
  PORTFOLIO_CHANGED
}