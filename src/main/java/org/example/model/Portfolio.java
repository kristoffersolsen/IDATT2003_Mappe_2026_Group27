package org.example.model;

import org.example.model.transaction.SaleCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Portfolio {

  private ArrayList<Share> shares;

  public Portfolio() {
    shares = new ArrayList<>();
  }

  public boolean addShare(Share share) {
    if (!this.shares.contains(share)) {
      this.shares.add(share);
      return true;
    }
    return false;
  }

  public boolean removeShare(Share share) {
    if (this.shares.contains(share)) {
      this.shares.remove(share);
      return true;
    }
    return false;
  }

  public ArrayList<Share> getShares() {
    return this.shares;
  }

  ArrayList<Share> getShares(String symbol) {
    ArrayList<Share> returnList = new ArrayList<>();

    for (Share share : shares) {
      if (share.getStock().getCompany().equals(symbol)) {
        returnList.add(share);
      }
    }

    return returnList;
  }

  /**
   * Checks if the Portfolio contains the share
   * @param shareToCheckFor The share to check for
   * @return True or False
   */
  public boolean contains(Share shareToCheckFor) {
    for (Share share : shares) {
      if (share.getStock().equals(shareToCheckFor.getStock())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sums up the value of all shares in the portfolio.
   * @return
   */
  public BigDecimal getNetWorth() {
    return shares.stream()
            .map(share -> new SaleCalculator(share).calculateTotal())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

}
