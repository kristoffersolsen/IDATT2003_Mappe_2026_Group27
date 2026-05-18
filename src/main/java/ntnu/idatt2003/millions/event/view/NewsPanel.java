package ntnu.idatt2003.millions.event.view;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.millions.event.model.MarketEvent;

/**
 * Right-panel view for the news feed.
 *
 * <p>Paginates event cards at {@value #PAGE_SIZE} per page. Pagination state and
 * viewed-state tracking are owned by
 * {@link ntnu.idatt2003.millions.event.controller.NewsController}.
 * This view only renders what it receives.
 */
public class NewsPanel extends VBox implements NewsPanelView {

  /** Number of event cards shown per page. */
  public static final int PAGE_SIZE = 4;

  private final VBox cardContainer = new VBox(10);
  private final Label pageLabel = new Label("Page 1 of 1");
  private final Button prevButton = new Button("←");
  private final Button nextButton = new Button("→");

  /**
   * Builds the news panel layout.
   */
  public NewsPanel() {
    Label title = new Label("News Feed");
    title.getStyleClass().addAll("font-white", "font-title");

    VBox content = new VBox(12, title, buildPaginationRow(), cardContainer);
    content.setPadding(new Insets(20));
    VBox.setVgrow(cardContainer, Priority.ALWAYS);

    getChildren().add(content);
    getStyleClass().add("content-dark");
    setPrefWidth(300);
    VBox.setVgrow(this, Priority.ALWAYS);
  }

  @Override
  public void setPrevAction(Runnable action) {
    prevButton.setOnAction(e -> action.run());
  }

  @Override
  public void setNextAction(Runnable action) {
    nextButton.setOnAction(e -> action.run());
  }

  @Override
  public void showPage(List<MarketEvent> events, Set<String> viewedIds,
      int page, int totalPages, Consumer<MarketEvent> onOpen) {
    cardContainer.getChildren().clear();
    pageLabel.setText("Page " + (page + 1) + " of " + Math.max(1, totalPages));
    prevButton.setDisable(page <= 0);
    nextButton.setDisable(page >= totalPages - 1);

    if (events.isEmpty()) {
      Label empty = new Label("No news yet.");
      empty.getStyleClass().addAll("font-grey", "font-small");
      cardContainer.getChildren().add(empty);
      return;
    }

    for (MarketEvent event : events) {
      cardContainer.getChildren().add(
          new NewsEventCard(event, !viewedIds.contains(event.id()), onOpen));
    }
  }

  private HBox buildPaginationRow() {
    prevButton.getStyleClass().add("button-light-grey");
    nextButton.getStyleClass().add("button-light-grey");
    pageLabel.getStyleClass().addAll("font-white", "font-small");

    Region leftSpacer = new Region();
    Region rightSpacer = new Region();
    HBox.setHgrow(leftSpacer, Priority.ALWAYS);
    HBox.setHgrow(rightSpacer, Priority.ALWAYS);

    HBox row = new HBox(8, prevButton, leftSpacer, pageLabel, rightSpacer, nextButton);
    row.setAlignment(Pos.CENTER);
    return row;
  }
}
