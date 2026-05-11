package ntnu.idatt2003.millions.model.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ntnu.idatt2003.millions.config.Difficulty;
import ntnu.idatt2003.millions.config.GameDefaults;
import ntnu.idatt2003.millions.config.GameSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GameTime")
class GameTimeTest {

  // Calendar: 8 h/day, 5 days/week, 4 weeks/month → 40 ticks/week, 160 ticks/month
  private static final int HOURS_PER_DAY = 8;
  private static final int DAYS_PER_WEEK = 5;
  private static final long TICKS_PER_DAY = HOURS_PER_DAY;
  private static final long TICKS_PER_WEEK = (long) HOURS_PER_DAY * DAYS_PER_WEEK;
  private static final long TICKS_PER_MONTH = TICKS_PER_WEEK * 4;

  private GameSettings settings;

  @BeforeEach
  void setUp() {
    settings = GameDefaults.forDifficulty(Difficulty.NORMAL);
  }

  private GameTime at(long tick) {
    return new GameTime(settings, tick);
  }

  @Nested
  @DisplayName("getHour")
  class GetHour {

    @Test
    @DisplayName("tick 0 is hour 0 (first hour of day)")
    void tick0IsHour0() {
      assertEquals(0, at(0L).getHour());
    }

    @Test
    @DisplayName("tick 7 is hour 7 (last hour of day)")
    void tick7IsLastHour() {
      assertEquals(7, at(7L).getHour());
    }

    @Test
    @DisplayName("tick 8 wraps to hour 0 of next day")
    void tick8WrapsToHour0() {
      assertEquals(0, at(TICKS_PER_DAY).getHour());
    }
  }

  @Nested
  @DisplayName("getDay")
  class GetDay {

    @Test
    @DisplayName("tick 0 is day 1")
    void tick0IsDay1() {
      assertEquals(1, at(0L).getDay());
    }

    @Test
    @DisplayName("first tick of second trading day is day 2")
    void secondDay() {
      assertEquals(2, at(TICKS_PER_DAY).getDay());
    }

    @Test
    @DisplayName("first tick of new week wraps back to day 1")
    void newWeekWrapsToDay1() {
      assertEquals(1, at(TICKS_PER_WEEK).getDay());
    }
  }

  @Nested
  @DisplayName("getWeek")
  class GetWeek {

    @Test
    @DisplayName("tick 0 is week 1")
    void tick0IsWeek1() {
      assertEquals(1, at(0L).getWeek());
    }

    @Test
    @DisplayName("tick at start of second week is week 2")
    void secondWeek() {
      assertEquals(2, at(TICKS_PER_WEEK).getWeek());
    }

    @Test
    @DisplayName("last tick of first week is still week 1")
    void lastTickOfFirstWeekIsWeek1() {
      assertEquals(1, at(TICKS_PER_WEEK - 1).getWeek());
    }
  }

  @Nested
  @DisplayName("getMonth")
  class GetMonth {

    @Test
    @DisplayName("tick 0 is month 1")
    void tick0IsMonth1() {
      assertEquals(1, at(0L).getMonth());
    }

    @Test
    @DisplayName("tick at start of second month is month 2")
    void secondMonth() {
      assertEquals(2, at(TICKS_PER_MONTH).getMonth());
    }
  }

  @Nested
  @DisplayName("format")
  class Format {

    @Test
    @DisplayName("format includes week number")
    void formatIncludesWeek() {
      String label = at(0L).format();
      assertTrue(label.contains("Wk 1"), "Expected 'Wk 1' in: " + label);
    }

    @Test
    @DisplayName("format includes day name Mon for first day")
    void formatIncludesDayName() {
      String label = at(0L).format();
      assertTrue(label.contains("Mon"), "Expected 'Mon' in: " + label);
    }

    @Test
    @DisplayName("format includes 08:00 for first hour")
    void formatIncludesHour() {
      String label = at(0L).format();
      assertTrue(label.contains("08:00"), "Expected '08:00' in: " + label);
    }
  }
}
