package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * The start screen shown when the application launches.
 *
 * <p>Responsible only for constructing and arranging nodes.
 * All event handling and validation is performed by
 * {@link org.example.controller.StartController}.
 */
public class StartView {

  private final VBox root;

  private final TextField nameField = new TextField();
  private final TextField capitalField = new TextField();
  private final TextField fileNameField = new TextField();
  private final Button browseButton = new Button("Browse...");
  private final Button startButton = new Button("Start");
  private final Label errorLabel = new Label();

  /**
   * Builds the start screen layout.
   */
  public StartView() {
    Label title = new Label("Millions");
    title.setStyle("-fx-font-size: 96px;");

    GridPane form = buildForm();

    errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 13px;");
    errorLabel.setVisible(false);

    startButton.setPrefWidth(160);
    startButton.setDefaultButton(true);

    VBox card = new VBox(20, form, errorLabel, startButton);
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(40));
    card.setMaxWidth(420);
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
   * Builds the input form grid.
   *
   * @return the assembled form
   */
  private GridPane buildForm() {
    nameField.setPromptText("Your name");
    capitalField.setPromptText("e.g. 10000");

    fileNameField.setPromptText("No file selected");
    fileNameField.setEditable(false);
    fileNameField.setPrefWidth(180);

    HBox fileRow = new HBox(8, fileNameField, browseButton);
    fileRow.setAlignment(Pos.CENTER_LEFT);

    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(14);
    grid.setAlignment(Pos.CENTER);

    grid.add(new Label("Name"), 0, 0);
    grid.add(nameField, 1, 0);

    grid.add(new Label("Starting capital"), 0, 1);
    grid.add(capitalField, 1, 1);

    grid.add(new Label("Stock data"), 0, 2);
    grid.add(fileRow, 1, 2);

    return grid;
  }

  /**
   * Opens a file chooser dialog and returns the selected file.
   *
   * <p>The view opens the dialog on behalf of the controller, but the
   * controller decides what to do with the result.
   *
   * @param stage the owner window for the dialog
   * @return the selected file, or null if the user cancelled
   */
  public File showFileChooser(Stage stage) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Select stock data file");
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("CSV files", "*.csv"));
    return chooser.showOpenDialog(stage);
  }

  /**
   * Updates the file name field to reflect a chosen file.
   *
   * @param fileName the file name to display
   */
  public void setFileName(String fileName) {
    fileNameField.setText(fileName);
  }

  /**
   * Displays an error message below the form.
   *
   * @param message the message to show
   */
  public void showError(String message) {
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }

  /**
   * Hides the error message.
   */
  public void hideError() {
    errorLabel.setVisible(false);
  }

  public VBox getRoot() {
    return root;
  }

  public Button getStartButton() {
    return startButton;
  }

  public Button getBrowseButton() {
    return browseButton;
  }

  public String getPlayerName() {
    return nameField.getText().trim();
  }

  public String getStartingCapital() {
    return capitalField.getText().trim();
  }
}