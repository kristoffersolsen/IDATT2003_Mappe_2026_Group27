package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The end screen shown after the player sells all holdings and exits.
 *
 * <p>Responsible only for constructing and arranging nodes.
 * All event handling is performed by {@link org.example.controller.EndController}.
 */
public class EndView {

  private final VBox root;

  private final Label nameValue = new Label();
  private final Label startingCapitalValue = new Label();
  private final Label endingCapitalValue = new Label();
  private final Label finalStatusValue = new Label();

  private final Button newGameButton = new Button("New Game");
  private final Button exitButton = new Button("Exit");

  /**
   * Builds the end screen layout.
   */
  public EndView() {
    Label title = new Label("Millions");
    title.setStyle("-fx-font-size: 64px;");

    GridPane summary = buildSummaryGrid();

    newGameButton.setPrefWidth(140);
    exitButton.setPrefWidth(140);

    HBox buttonRow = new HBox(12, newGameButton, exitButton);
    buttonRow.setAlignment(Pos.CENTER);

    VBox card = new VBox(24, summary, buttonRow);
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(40));
    card.setMaxWidth(440);
    card.setStyle(
        "-fx-background-color: #d9d9d9;"
            + "-fx-background-radius: 8px;");

    Region topSpacer = new Region();
    Region bottomSpacer = new Region();
    VBox.setVgrow(topSpacer, Priority.ALWAYS);
    VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

    root = new VBox(topSpacer, title, card, bottomSpacer);
    root.setAlignment(Pos.CENTER);
    root.setStyle("-fx-background-color: white;");
  }

  /**
   * Builds the summary grid displaying game results.
   *
   * @return the assembled grid
   */
  private GridPane buildSummaryGrid() {
    styleValueLabel(nameValue);
    styleValueLabel(startingCapitalValue);
    styleValueLabel(endingCapitalValue);
    styleValueLabel(finalStatusValue);

    GridPane grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(14);
    grid.setAlignment(Pos.CENTER);

    grid.add(new Label("Name"), 0, 0);
    grid.add(nameValue, 1, 0);

    grid.add(new Label("Starting capital"), 0, 1);
    grid.add(startingCapitalValue, 1, 1);

    grid.add(new Label("Ending capital"), 0, 2);
    grid.add(endingCapitalValue, 1, 2);

    grid.add(new Label("Final status"), 0, 3);
    grid.add(finalStatusValue, 1, 3);

    return grid;
  }

  /**
   * Applies shared styling to a summary value label.
   *
   * @param label the label to style
   */
  private void styleValueLabel(Label label) {
    label.setPrefWidth(180);
    label.setPadding(new Insets(4, 8, 4, 8));
    label.setStyle(
        "-fx-background-color: white;"
            + "-fx-background-radius: 4px;");
  }

  /**
   * Populates the summary fields with game results.
   *
   * <p>Called by the controller after the game ends.
   *
   * @param name            the player's name
   * @param startingCapital formatted starting capital string
   * @param endingCapital   formatted ending net worth string
   * @param status          the player's final status label
   */
  public void populate(
      String name,
      String startingCapital,
      String endingCapital,
      String status) {
    nameValue.setText(name);
    startingCapitalValue.setText(startingCapital);
    endingCapitalValue.setText(endingCapital);
    finalStatusValue.setText(status);
  }

  public VBox getRoot() {
    return root;
  }

  public Button getNewGameButton() {
    return newGameButton;
  }

  public Button getExitButton() {
    return exitButton;
  }
}