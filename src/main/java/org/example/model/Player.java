package org.example.model;

import org.example.service.TransactionArchive;

import java.math.BigDecimal;

public class Player {
    private String name;
    private BigDecimal startingMoney;
    private BigDecimal money;
    private Portfolio portfolio;
    private TransactionArchive transactionArchive;
    private Status status;

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

    public void addMoney(BigDecimal amount) {
        this.money = this.money.add(amount);
    }

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
     * @param week
     * @return
     */
    public Status getStatus(int week) {
        if (week >= 20 && getNetWorth().compareTo(startingMoney.multiply(BigDecimal.valueOf(2))) >= 0) {
            return Status.SPECULATOR;
        } else if (week >= 10 && getNetWorth().compareTo(startingMoney.multiply(BigDecimal.valueOf(1.2))) >= 0) {
            return Status.INVESTOR;
        } else {
            return Status.INVESTOR;
        }
    }
}

