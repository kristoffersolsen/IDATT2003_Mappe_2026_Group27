package org.example.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Utility for formatting values
 */
public class Format {

  /**
   * Static class -> private constructor.
   */
  private Format() {}

  public static String formatMoney(BigDecimal money){
    DecimalFormat df = new DecimalFormat("#,###.00");
    return df.format(money);
  }
}
