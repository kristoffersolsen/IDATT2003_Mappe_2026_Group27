package org.example.model;

import java.math.BigDecimal;
import org.example.model.observer.GameEvent;
import org.example.model.observer.Observable;
import org.example.model.transaction.TransactionArchive;

/**
 * Player class that represents a player.
 */
public class Player extends Observable {
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
   * Checks the week and net-worth growth to determine player status.
   *
   * @param week the current week
   * @return the highest {@link Status} tier the player qualifies for
   */
  public Status getStatus(int week) {
    BigDecimal netWorth = getNetWorth();
    Status[] values = Status.values();
    for (int i = values.length - 1; i > 0; i--) {
      if (values[i].qualifies(week, startingMoney, netWorth)) {
        return values[i];
      }
    }
    return Status.NOVICE;
  }
}
