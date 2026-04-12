package org.example.model;

import java.math.BigDecimal;
import org.example.service.TransactionArchive;

/**
 * Player class that reperesents a player.
 */
public class Player {
  private final String name;
  private final BigDecimal startingMoney;
  private BigDecimal money;
  private final Portfolio portfolio;
  private final TransactionArchive transactionArchive;
  private final Status status;

  /**
   * Default constructor.
   *
   * @param name          Name of player
   * @param startingMoney starting money of player
   */
  public Player(String name, BigDecimal startingMoney) {
    this.name = name;
    this.startingMoney = startingMoney;
    this.money = startingMoney;
    this.portfolio = new Portfolio();
    this.transactionArchive = new TransactionArchive();
    this.status = Status.NOVICE;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getMoney() {
    return money;
  }

  /**
   * Adds money to players account.
   *
   * @param amount amount to add
   */
  public void addMoney(BigDecimal amount) {
    this.money = this.money.add(amount);
  }

  /**
   * Withdraws money to players account.
   *
   * @param amount amount to withdraw
   */
  public void withdrawMoney(BigDecimal amount) {
    this.money = this.money.subtract(amount);
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
   * Checks the week and net worth growth to determine player status.
   *
   * @param week the week to check for.
   * @return Status of player
   */
  public Status getStatus(int week) {
    if (week >= 20 && getNetWorth().compareTo(startingMoney.multiply(BigDecimal.valueOf(2))) >= 0) {
      return Status.SPECULATOR;
    } else if (week >= 10
        && getNetWorth().compareTo(startingMoney.multiply(BigDecimal.valueOf(1.2))) >= 0) {
      return Status.INVESTOR;
    } else {
      return Status.NOVICE;
    }
  }
}

