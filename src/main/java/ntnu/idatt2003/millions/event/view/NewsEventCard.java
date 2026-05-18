package ntnu.idatt2003.millions.event.view;

import java.util.function.Consumer;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.millions.event.model.EventSeverity;
import ntnu.idatt2003.millions.event.model.MarketEvent;

/**
 * A single-event card displayed in the news panel.
 *
 * <p>Visual severity is communicated via CSS classes
 * ({@code .minor}, {@code .major}, {@code .crisis}) applied to the
 * {@code .news-event-card} container. The {@code .new-event} class is present
 * until the player opens the panel, at which point the controller rebuilds cards
 * without the class.
 */
public class NewsEventCard extends VBox {

  /**
   * Builds a card for the given event.
   *
   * @param event  the market event to display
   * @param isNew  {@code true} if the event has not yet been viewed
   * @param onOpen callback invoked with the event when the player clicks the card
   */
  public NewsEventCard(MarketEvent event, boolean isNew, Consumer<MarketEvent> onOpen) {
    Label severityLabel = new Label(event.severity().name());
    severityLabel.getStyleClass().addAll("font-small", severityBadgeClass(event.severity()));

    Label headlineLabel = new Label(event.headline());
    headlineLabel.getStyleClass().addAll("font-white", "font-content", "font-bold");
    headlineLabel.setWrapText(true);

    Label bodyLabel = new Label(event.body());
    bodyLabel.getStyleClass().addAll("font-grey", "font-small");
    bodyLabel.setWrapText(true);

    getChildren().addAll(severityLabel, headlineLabel, bodyLabel);
    getStyleClass().addAll("news-event-card", severityCssClass(event.severity()));
    setSpacing(6);

    if (isNew) {
      getStyleClass().add("new-event");
    }

    setCursor(Cursor.HAND);
    setOnMouseClicked(e -> onOpen.accept(event));
  }

  private static String severityCssClass(EventSeverity severity) {
    return switch (severity) {
      case MINOR -> "minor";
      case MAJOR -> "major";
      case CRISIS -> "crisis";
    };
  }

  private static String severityBadgeClass(EventSeverity severity) {
    return switch (severity) {
      case MINOR -> "severity-minor";
      case MAJOR -> "severity-major";
      case CRISIS -> "severity-crisis";
    };
  }
}
