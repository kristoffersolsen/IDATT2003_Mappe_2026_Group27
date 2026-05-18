package ntnu.idatt2003.millions.event.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import ntnu.idatt2003.millions.event.model.EventSeverity;
import ntnu.idatt2003.millions.event.model.EventTemplate;
import ntnu.idatt2003.millions.event.model.MarketEvent;
import ntnu.idatt2003.millions.event.service.EventService;
import ntnu.idatt2003.millions.event.view.NewsPanelView;
import ntnu.idatt2003.millions.market.model.Sector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("NewsController")
class NewsControllerTest {

  // -------- stub view (no JavaFX) --------

  static class StubView implements NewsPanelView {

    Runnable prevAction;
    Runnable nextAction;
    int lastPage;
    int lastTotalPages;
    List<MarketEvent> lastEvents = List.of();

    @Override
    public void setPrevAction(Runnable action) {
      this.prevAction = action;
    }

    @Override
    public void setNextAction(Runnable action) {
      this.nextAction = action;
    }

    @Override
    public void showPage(List<MarketEvent> events, Set<String> viewedIds,
        int page, int totalPages, Consumer<MarketEvent> onOpen) {
      this.lastEvents = List.copyOf(events);
      this.lastPage = page;
      this.lastTotalPages = totalPages;
    }
  }

  // -------- stub EventService --------

  static class StubEventService extends EventService {

    private final List<MarketEvent> history;

    StubEventService(List<MarketEvent> history) {
      super(List.of(new EventTemplate("t", "T", "B",
          Set.of(Sector.TECH), EventSeverity.MINOR, true)));
      this.history = history;
    }

    @Override
    public List<MarketEvent> getHistory() {
      return Collections.unmodifiableList(history);
    }
  }

  // -------- factory helpers --------

  private static final Consumer<MarketEvent> NO_OP = event -> {};

  private static MarketEvent makeEvent(String id) {
    return new MarketEvent(id, "Headline " + id, "Body", EventSeverity.MINOR,
        Set.of(Sector.TECH), 0.01, 4, 0L);
  }

  private static List<MarketEvent> makeHistory(int count) {
    List<MarketEvent> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      list.add(makeEvent("e" + i));
    }
    return list;
  }

  private static NewsController controller(StubView stub, List<MarketEvent> history) {
    return new NewsController(stub, new StubEventService(history), NO_OP);
  }

  // -------- tests --------

  @Nested
  @DisplayName("pagination math")
  class PaginationMath {

    @Test
    @DisplayName("empty history gives 1 page")
    void totalPages_emptyHistory_isOne() {
      StubView stub = new StubView();
      NewsController ctrl = controller(stub, List.of());
      assertEquals(1, ctrl.totalPages());
    }

    @Test
    @DisplayName("4 events gives 1 page")
    void totalPages_fourEvents_isOne() {
      StubView stub = new StubView();
      NewsController ctrl = controller(stub, makeHistory(4));
      assertEquals(1, ctrl.totalPages());
    }

    @Test
    @DisplayName("5 events gives 2 pages")
    void totalPages_fiveEvents_isTwo() {
      StubView stub = new StubView();
      NewsController ctrl = controller(stub, makeHistory(5));
      assertEquals(2, ctrl.totalPages());
    }

    @Test
    @DisplayName("12 events gives 3 pages")
    void totalPages_twelveEvents_isThree() {
      StubView stub = new StubView();
      NewsController ctrl = controller(stub, makeHistory(12));
      assertEquals(3, ctrl.totalPages());
    }

    @Test
    @DisplayName("page 0 shows first 4 events")
    void refresh_pageZero_showsFirstFour() {
      StubView stub = new StubView();
      List<MarketEvent> history = makeHistory(12);
      controller(stub, history);
      assertEquals(0, stub.lastPage);
      assertEquals(4, stub.lastEvents.size());
      assertEquals(history.get(0).id(), stub.lastEvents.get(0).id());
    }

    @Test
    @DisplayName("navigating to next page shows correct slice")
    void nextPage_showsSecondSlice() {
      StubView stub = new StubView();
      List<MarketEvent> history = makeHistory(12);
      controller(stub, history);
      stub.nextAction.run();
      assertEquals(1, stub.lastPage);
      assertEquals(4, stub.lastEvents.size());
      assertEquals(history.get(4).id(), stub.lastEvents.get(0).id());
    }

    @Test
    @DisplayName("prev is disabled on first page (page stays at 0)")
    void prevPage_atFirstPage_doesNotNavigate() {
      StubView stub = new StubView();
      NewsController ctrl = controller(stub, makeHistory(12));
      stub.prevAction.run();
      assertEquals(0, ctrl.getCurrentPage());
    }

    @Test
    @DisplayName("next is disabled on last page (page stays at max)")
    void nextPage_atLastPage_doesNotNavigate() {
      StubView stub = new StubView();
      NewsController ctrl = controller(stub, makeHistory(4));
      stub.nextAction.run();
      assertEquals(0, ctrl.getCurrentPage());
    }
  }

  @Nested
  @DisplayName("viewed state")
  class ViewedState {

    @Test
    @DisplayName("no events are viewed initially")
    void viewedIds_initial_isEmpty() {
      StubView stub = new StubView();
      NewsController ctrl = controller(stub, makeHistory(3));
      assertTrue(ctrl.getViewedIds().isEmpty());
    }

    @Test
    @DisplayName("markPanelOpened marks all current events as viewed")
    void markPanelOpened_marksAllViewed() {
      StubView stub = new StubView();
      List<MarketEvent> history = makeHistory(3);
      NewsController ctrl = controller(stub, history);
      ctrl.markPanelOpened();
      for (MarketEvent event : history) {
        assertTrue(ctrl.getViewedIds().contains(event.id()),
            "Expected " + event.id() + " to be marked viewed");
      }
    }

    @Test
    @DisplayName("events added after markPanelOpened are not viewed")
    void markPanelOpened_futureEventsNotViewed() {
      StubView stub = new StubView();
      List<MarketEvent> history = new ArrayList<>(makeHistory(2));
      NewsController ctrl = new NewsController(stub, new StubEventService(history), NO_OP);
      ctrl.markPanelOpened();

      history.add(0, makeEvent("future"));

      assertFalse(ctrl.getViewedIds().contains("future"));
    }

    @Test
    @DisplayName("getViewedIds returns an unmodifiable set")
    void getViewedIds_isUnmodifiable() {
      StubView stub = new StubView();
      NewsController ctrl = controller(stub, makeHistory(1));
      Set<String> ids = ctrl.getViewedIds();
      assertThrows(UnsupportedOperationException.class, () -> ids.add("x"));
    }
  }

  @Nested
  @DisplayName("refresh")
  class Refresh {

    @Test
    @DisplayName("refresh clamps current page if history shrinks")
    void refresh_clampsToBounds() {
      StubView stub = new StubView();
      List<MarketEvent> history = new ArrayList<>(makeHistory(8));
      NewsController ctrl = new NewsController(stub, new StubEventService(history), NO_OP);
      stub.nextAction.run();
      assertEquals(1, ctrl.getCurrentPage());

      history.clear();
      ctrl.refresh();
      assertEquals(0, ctrl.getCurrentPage());
    }

    @Test
    @DisplayName("refresh updates the view with correct total pages")
    void refresh_updatesTotalPages() {
      StubView stub = new StubView();
      controller(stub, makeHistory(9));
      assertEquals(3, stub.lastTotalPages);
    }
  }
}
