package ntnu.idatt2003.millions.shared.util;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Utility for formatting values.
 */
public class Format {

  private Format() {
  }

  /**
   * Formats a monetary value with thousands separators and two decimal places.
   *
   * @param money the value to format
   * @return formatted string, e.g. {@code "1,234.56"}
   */
  public static String formatMoney(BigDecimal money) {
    return String.format(Locale.US, "%,.2f", money);
  }
}
