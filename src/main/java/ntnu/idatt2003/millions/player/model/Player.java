package ntnu.idatt2003.millions.player.model;

import java.math.BigDecimal;
import ntnu.idatt2003.millions.shared.observer.GameEvent;
import ntnu.idatt2003.millions.shared.observer.Observable;
import ntnu.idatt2003.millions.shared.time.GameTime;
import ntnu.idatt2003.millions.transaction.model.TransactionArchive;

/**
 * Player class that represents a player.
 */
public class Player extends Observable {

  private static final StatusEvaluator STATUS_EVALUATOR = new StatusEvaluator();

  private final String name;
  private final BigDecimal startingMoney;
  private BigDecimal money;
  private final Portfolio portfolio;
  private final TransactionArchive transactionArchive;

  /**
   * Default constructor.
   *
   * @param name          name of player
   * @param startingMoney starting money of player
   */
  public Player(String name, BigDecimal startingMoney) {
    this.name = name;
    this.startingMoney = startingMoney;
    this.money = startingMoney;
    this.portfolio = new Portfolio();
    this.transactionArchive = new TransactionArchive();
  }

  public String getName() {
    return name;
  }

  public BigDecimal getMoney() {
    return money;
  }

  /**
   * Returns the starting money used to initialize this player.
   *
   * @return the starting money
   */
  public BigDecimal getStartingMoney() {
    return startingMoney;
  }

  /**
   * Adds money to the player's account.
   *
   * @param amount amount to add
   */
  public void addMoney(BigDecimal amount) {
    this.money = this.money.add(amount);
    notifyObservers(GameEvent.BALANCE_CHANGED);
  }

  /**
   * Withdraws money from the player's account.
   *
   * @param amount amount to withdraw
   */
  public void withdrawMoney(BigDecimal amount) {
    this.money = this.money.subtract(amount);
    notifyObservers(GameEvent.BALANCE_CHANGED);
  }

  public Portfolio getPortfolio() {
    return portfolio;
  }

  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

  public BigDecimal getNetWorth() {
    return this.portfolio.getNetWorth().add(this.money);
  }

  /**
   * Determines the player's current status tier for the given game time.
   *
   * <p>Delegates to {@link StatusEvaluator} so that the same logic can be
   * reused for non-player entities in future versions.
   *
   * @param time the current game time
   * @return the highest {@link Status} tier the player qualifies for
   */
  public Status getStatus(GameTime time) {
    return STATUS_EVALUATOR.evaluate(startingMoney, getNetWorth(), time);
  }
}
