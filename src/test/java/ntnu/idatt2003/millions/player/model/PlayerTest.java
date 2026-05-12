package ntnu.idatt2003.millions.player.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import ntnu.idatt2003.millions.shared.config.Difficulty;
import ntnu.idatt2003.millions.shared.config.GameDefaults;
import ntnu.idatt2003.millions.shared.config.GameSettings;
import ntnu.idatt2003.millions.shared.time.GameTime;
import ntnu.idatt2003.millions.market.model.Stock;
import ntnu.idatt2003.millions.player.model.Share;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Player")
class PlayerTest {

  private static final GameSettings SETTINGS = GameDefaults.forDifficulty(Difficulty.NORMAL);

  private Player player;

  @BeforeEach
  void setUp() {
    player = new Player("Player", BigDecimal.valueOf(1000));
  }

  /** Creates a GameTime that reports the given week number (1-based). */
  private static GameTime atWeek(int week) {
    long ticksPerWeek = (long) SETTINGS.hoursPerDay() * SETTINGS.daysPerWeek();
    return new GameTime(SETTINGS, (long) (week - 1) * ticksPerWeek);
  }

  @Test
  @DisplayName("initial money equals starting money")
  void getMoneyInitial() {
    assertEquals(0, BigDecimal.valueOf(1000).compareTo(player.getMoney()));
  }

  @Test
  @DisplayName("portfolio is non-null and empty on creation")
  void portfolioIsEmptyOnCreation() {
    assertNotNull(player.getPortfolio());
    assertTrue(player.getPortfolio().getShares().isEmpty());
  }

  @Test
  @DisplayName("transactionArchive is non-null and empty on creation")
  void transactionArchiveIsEmptyOnCreation() {
    assertNotNull(player.getTransactionArchive());
    assertTrue(player.getTransactionArchive().isEmpty());
  }

  @Nested
  @DisplayName("addMoney")
  class AddMoney {

    @Test
    @DisplayName("adds the correct amount")
    void addsAmount() {
      player.addMoney(BigDecimal.valueOf(500));
      assertEquals(0, BigDecimal.valueOf(1500).compareTo(player.getMoney()));
    }

    @Test
    @DisplayName("adding zero leaves balance unchanged")
    void addingZeroNoChange() {
      player.addMoney(BigDecimal.ZERO);
      assertEquals(0, BigDecimal.valueOf(1000).compareTo(player.getMoney()));
    }

    @Test
    @DisplayName("multiple additions accumulate correctly")
    void multipleAdditions() {
      player.addMoney(BigDecimal.valueOf(100));
      player.addMoney(BigDecimal.valueOf(200));
      assertEquals(0, BigDecimal.valueOf(1300).compareTo(player.getMoney()));
    }
  }

  @Nested
  @DisplayName("withdrawMoney")
  class WithdrawMoney {

    @Test
    @DisplayName("withdraws the correct amount")
    void withdrawsAmount() {
      player.withdrawMoney(BigDecimal.valueOf(300));
      assertEquals(0, BigDecimal.valueOf(700).compareTo(player.getMoney()));
    }

    @Test
    @DisplayName("withdrawing zero leaves balance unchanged")
    void withdrawingZeroNoChange() {
      player.withdrawMoney(BigDecimal.ZERO);
      assertEquals(0, BigDecimal.valueOf(1000).compareTo(player.getMoney()));
    }

    @Test
    @DisplayName("balance can go negative (no guard in Player)")
    void balanceCanGoNegative() {
      player.withdrawMoney(BigDecimal.valueOf(2000));
      assertTrue(player.getMoney().compareTo(BigDecimal.ZERO) < 0);
    }
  }

  @Nested
  @DisplayName("getNetWorth")
  class NetWorth {

    @Test
    @DisplayName("net worth equals money when portfolio is empty")
    void netWorthEqualsCashWhenPortfolioEmpty() {
      assertEquals(0, player.getMoney().compareTo(player.getNetWorth()));
    }

    @Test
    @DisplayName("net worth includes portfolio value")
    void netWorthIncludesPortfolio() {
      Stock stock = new Stock("AAA", "Company A", BigDecimal.valueOf(100));
      Share share = new Share(stock, BigDecimal.valueOf(1), BigDecimal.valueOf(80));
      player.getPortfolio().addShare(share);
      assertTrue(player.getNetWorth().compareTo(player.getMoney()) > 0);
    }
  }

  @Nested
  @DisplayName("getStatus")
  class PlayerStatus {

    @Test
    @DisplayName("returns NOVICE/INVESTOR for early weeks")
    void earlyWeeksNotSpeculator() {
      Status status = player.getStatus(atWeek(1));
      assertNotEquals(Status.SPECULATOR, status);
    }

    @Test
    @DisplayName("returns SPECULATOR when week>=20 and net worth doubled")
    void speculatorWhenConditionsMet() {
      player.addMoney(BigDecimal.valueOf(1000));
      Status status = player.getStatus(atWeek(20));
      assertEquals(Status.SPECULATOR, status);
    }

    @Test
    @DisplayName("does not return SPECULATOR when wealth threshold not met at week 20")
    void notSpeculatorWhenWealthInsufficientAtWeek20() {
      Status status = player.getStatus(atWeek(20));
      assertNotEquals(Status.SPECULATOR, status);
    }

    @Test
    @DisplayName("does not return SPECULATOR before week 20 even if wealthy")
    void notSpeculatorBeforeWeek20() {
      player.addMoney(BigDecimal.valueOf(5000));
      Status status = player.getStatus(atWeek(19));
      assertNotEquals(Status.SPECULATOR, status);
    }
  }
}
