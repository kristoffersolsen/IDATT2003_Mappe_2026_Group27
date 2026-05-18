package ntnu.idatt2003.millions.market.model;

/**
 * Sector classification for a stock.
 *
 * <p>A stock may belong to multiple sectors with equal weighting.
 * {@link #UNCATEGORIZED} is the fallback when no sector is assigned.
 */
public enum Sector {
  TECH,
  FINANCE,
  ENERGY,
  HEALTHCARE,
  RETAIL,
  ENTERTAINMENT,
  INDUSTRIAL,
  UNCATEGORIZED
}
