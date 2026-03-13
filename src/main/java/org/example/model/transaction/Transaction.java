package org.example.model.transaction;

import org.example.model.transaction.TransactionCalculator;
import org.example.model.Player;
import org.example.model.Share;

/**
 * Abstract class for all transactions
 */
public abstract class Transaction {
    protected Share share;
    protected int week;
    protected TransactionCalculator calculator;
    protected boolean committed = false;

    /**
     *
     * @param share The share that is transacted
     * @param week The week of the transaction
     * @param calculator The calculator to use
     */
    Transaction(Share share, int week, TransactionCalculator calculator) {
        this.share = share;
        this.week = week;
        this.calculator = calculator;
    }


    public Share getShare() {
        return share;
    }

    public int getWeek() {
        return week;
    }

    public TransactionCalculator getCalculator() {
        return calculator;
    }

    public boolean isCommitted() {
        return committed;
    }

    /**
     * Checks that transaction is valid and commits it
     * @param player The player to perform a transaction on
     */
    public void commit(Player player) {

    }
}
