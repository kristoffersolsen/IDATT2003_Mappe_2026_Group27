package ntnu.idatt2003.millions.player.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import ntnu.idatt2003.millions.transaction.model.SaleCalculator;

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
   * Adds a share to the portfolio.
   *
   * <p>If a position for the same stock already exists the quantities are
   * merged into a single {@link Share} with a weighted-average purchase price,
   * keeping one row per stock in its original list position. A new position is
   * created when none exists.
   *
   * @param share share to add
   * @return true if a new position was created, false if an existing position was merged
   */
  public boolean addShare(Share share) {
    Optional<Share> existing = findBySymbol(share.stock().getSymbol());

    if (existing.isPresent()) {
      Share old = existing.get();
      BigDecimal newQty = old.quantity().add(share.quantity());

      BigDecimal oldCost = old.purchasePrice().multiply(old.quantity());
      BigDecimal newCost = share.purchasePrice().multiply(share.quantity());
      BigDecimal avgPrice = oldCost.add(newCost)
          .divide(newQty, 10, RoundingMode.HALF_UP);

      shares.set(shares.indexOf(old), new Share(share.stock(), newQty, avgPrice));
      return false;
    } else {
      shares.add(share);
      return true;
    }
  }

  /**
   * Reduces the held quantity of a position by the given amount.
   *
   * <p>If {@code quantityToSell} equals the held quantity the position is
   * removed entirely. Otherwise the position is updated in-place with the
   * remaining quantity, keeping the same weighted-average purchase price.
   *
   * @param share          share whose stock should be partially or fully sold
   * @param quantityToSell the number of shares being sold (must be positive)
   * @return true if the position existed and was updated, false if not found
   * @throws IllegalArgumentException if {@code quantityToSell} exceeds the held quantity
   */
  public boolean removeShare(Share share, BigDecimal quantityToSell) {
    Optional<Share> match = findBySymbol(share.stock().getSymbol());
    if (match.isEmpty()) {
      return false;
    }

    Share held = match.get();
    int cmp = quantityToSell.compareTo(held.quantity());

    if (cmp > 0) {
      throw new IllegalArgumentException(
          "Cannot sell " + quantityToSell + " shares of " + share.stock().getSymbol()
              + " — only " + held.quantity() + " held.");
    }

    shares.remove(held);

    if (cmp < 0) {
      BigDecimal remaining = held.quantity().subtract(quantityToSell);
      shares.add(new Share(held.stock(), remaining, held.purchasePrice()));
    }

    return true;
  }

  /**
   * Removes the entire position for the given share's stock.
   *
   * @param share share whose stock should be fully removed
   * @return true if found and removed, false otherwise
   */
  public boolean removeShare(Share share) {
    Optional<Share> match = findBySymbol(share.stock().getSymbol());
    if (match.isPresent()) {
      shares.remove(match.get());
      return true;
    }
    return false;
  }

  /**
   * Checks if the portfolio contains a position for the same stock.
   *
   * @param shareToCheckFor the share to check for
   * @return true if a position for that stock is held
   */
  public boolean contains(Share shareToCheckFor) {
    return findBySymbol(shareToCheckFor.stock().getSymbol()).isPresent();
  }

  /**
   * Returns the stored share for a given stock symbol, if one exists.
   *
   * @param symbol the stock symbol
   * @return an Optional containing the held share, or empty if not owned
   */
  public Optional<Share> getShareBySymbol(String symbol) {
    return findBySymbol(symbol);
  }

  /**
   * Returns an unmodifiable view of all current positions.
   *
   * @return live unmodifiable view of shares
   */
  public List<Share> getShares() {
    return Collections.unmodifiableList(shares);
  }

  /**
   * Sums up the net sale value of all positions in the portfolio.
   *
   * @return the net worth of the portfolio
   */
  public BigDecimal getNetWorth() {
    return shares.stream()
        .map(share -> new SaleCalculator(share).calculateTotal())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Optional<Share> findBySymbol(String symbol) {
    return shares.stream()
        .filter(s -> s.stock().getSymbol().equals(symbol))
        .findFirst();
  }
}
