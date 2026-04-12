package org.example.model;

import org.example.model.transaction.SaleCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * The portfolio of a player.
 */
public class Portfolio {

  private final ArrayList<Share> shares;

  /**
   * Default constructor.
   */
  public Portfolio() {
    shares = new ArrayList<>();
  }

  /**
   * Adds a share to the portfolio if it does not already exist.
   *
   * @param share share to add
   * @return true on success, false on failure
   */
  public boolean addShare(Share share) {
    if (!this.shares.contains(share)) {
      this.shares.add(share);
      return true;
    }
    return false;
  }

  /**
   * Removes a share from the portfolio if it exists.
   *
   * @param share share to remove
   * @return true if share exists, false otherwise
   */
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
      if (share.stock().getCompany().equals(symbol)) {
        returnList.add(share);
      }
    }

    return returnList;
  }

  /**
   * Checks if the Portfolio contains the share.
   *
   * @param shareToCheckFor The share to check for
   * @return True or False
   */
  public boolean contains(Share shareToCheckFor) {
    for (Share share : shares) {
      if (share.stock().equals(shareToCheckFor.stock())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sums up the value of all shares in the portfolio.
   *
   * @return the net worth of the profile
   */
  public BigDecimal getNetWorth() {
    return shares.stream()
        .map(share -> new SaleCalculator(share).calculateTotal())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

}
