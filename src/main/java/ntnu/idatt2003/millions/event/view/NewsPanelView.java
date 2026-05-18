package ntnu.idatt2003.millions.event.view;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import ntnu.idatt2003.millions.event.model.MarketEvent;

/**
 * View contract for the news panel.
 *
 * <p>Decouples {@link ntnu.idatt2003.millions.event.controller.NewsController} from the
 * JavaFX implementation so the controller's pagination and viewed-state logic can be
 * tested without a running JavaFX platform.
 */
public interface NewsPanelView {

  /**
   * Registers the action to invoke when the player clicks "previous page."
   *
   * @param action the callback to run
   */
  void setPrevAction(Runnable action);

  /**
   * Registers the action to invoke when the player clicks "next page."
   *
   * @param action the callback to run
   */
  void setNextAction(Runnable action);

  /**
   * Renders one page of events.
   *
   * @param events     the events for this page (up to 4)
   * @param viewedIds  IDs of events that have already been viewed
   * @param page       current page index (0-based)
   * @param totalPages total number of pages
   * @param onOpen     callback invoked with the event when the player clicks a card
   */
  void showPage(List<MarketEvent> events, Set<String> viewedIds,
      int page, int totalPages, Consumer<MarketEvent> onOpen);
}
