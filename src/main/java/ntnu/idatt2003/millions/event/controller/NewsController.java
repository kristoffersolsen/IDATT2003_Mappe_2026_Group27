package ntnu.idatt2003.millions.event.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import ntnu.idatt2003.millions.event.model.MarketEvent;
import ntnu.idatt2003.millions.event.service.EventService;
import ntnu.idatt2003.millions.event.view.NewsPanel;
import ntnu.idatt2003.millions.event.view.NewsPanelView;

/**
 * Controller for the news panel.
 *
 * <p>Owns the pagination cursor and the set of viewed event IDs. Delegates all
 * rendering to the {@link NewsPanelView} it was given at construction.
 *
 * <p>The pagination size is defined by {@link NewsPanel#PAGE_SIZE}. Events are shown
 * newest-first, matching the order returned by
 * {@link EventService#getHistory()}.
 */
public class NewsController {

  private final NewsPanelView view;
  private final EventService eventService;
  private final Consumer<MarketEvent> onOpen;

  private final Set<String> viewedIds = new HashSet<>();
  private int currentPage = 0;

  /**
   * Builds the controller and wires pagination buttons via the view's callbacks.
   *
   * @param view         the news panel view (or a test stub)
   * @param eventService the event service supplying the history
   * @param onOpen       callback invoked when the player clicks an event card
   */
  public NewsController(NewsPanelView view, EventService eventService,
      Consumer<MarketEvent> onOpen) {
    this.view = view;
    this.eventService = eventService;
    this.onOpen = onOpen;

    view.setPrevAction(() -> {
      if (currentPage > 0) {
        currentPage--;
        refresh();
      }
    });
    view.setNextAction(() -> {
      if (currentPage < totalPages() - 1) {
        currentPage++;
        refresh();
      }
    });

    refresh();
  }

  /**
   * Marks all events currently in history as viewed, then refreshes the panel.
   *
   * <p>Called by the dashboard controller whenever the player switches to the news panel.
   */
  public void markPanelOpened() {
    for (MarketEvent event : eventService.getHistory()) {
      viewedIds.add(event.id());
    }
    refresh();
  }

  /**
   * Reloads the current page from the event history.
   *
   * <p>If the current page index is out of bounds (e.g. after the history was empty),
   * it is clamped to the last valid page.
   */
  public void refresh() {
    List<MarketEvent> history = eventService.getHistory();
    int pages = totalPages();
    if (currentPage >= pages) {
      currentPage = Math.max(0, pages - 1);
    }
    int from = currentPage * NewsPanel.PAGE_SIZE;
    int to = Math.min(from + NewsPanel.PAGE_SIZE, history.size());
    view.showPage(history.subList(from, to), viewedIds, currentPage, pages, onOpen);
  }

  /**
   * Returns an unmodifiable view of the set of event IDs already seen by the player.
   *
   * @return the viewed-ID set
   */
  public Set<String> getViewedIds() {
    return Collections.unmodifiableSet(viewedIds);
  }

  /**
   * Returns the current page index (0-based).
   *
   * @return current page
   */
  int getCurrentPage() {
    return currentPage;
  }

  /**
   * Returns the total number of pages for the current history size.
   *
   * @return total pages, always at least 1
   */
  int totalPages() {
    int size = eventService.getHistory().size();
    return Math.max(1, (size + NewsPanel.PAGE_SIZE - 1) / NewsPanel.PAGE_SIZE);
  }
}
