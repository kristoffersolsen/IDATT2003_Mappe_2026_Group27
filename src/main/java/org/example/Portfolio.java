package org.example;

import java.util.ArrayList;

public class Portfolio {

  private ArrayList<Share> shares;

  Portfolio() {
    shares = new ArrayList<>();
  }

  boolean addShare(Share share) {
    if (!this.shares.contains(share)) {
      this.shares.add(share);
      return true;
    }
    return false;
  }

  boolean removeShare(Share share) {
    if (this.shares.contains(share)) {
      this.shares.remove(share);
      return true;
    }
    return false;
  }

  ArrayList<Share> getShares() {
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

  boolean contains(Share shareToCheckFor) {
    for (Share share : shares) {
      if (share.getStock().equals(shareToCheckFor.getStock())) {
        return true;
      }
    }
    return false;
  }

}
