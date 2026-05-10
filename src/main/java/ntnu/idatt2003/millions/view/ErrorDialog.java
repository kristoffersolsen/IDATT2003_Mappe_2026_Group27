package ntnu.idatt2003.millions.view;

import javafx.scene.control.Alert;

/**
 * Shared helper for displaying unexpected errors to the user.
 *
 * <p>Use this for failures the user cannot prevent or fix through input
 * validation. For validation errors, use an inline error label instead.
 */
public final class ErrorDialog {

  private ErrorDialog() {
  }

  /**
   * Displays a modal error dialog with the given title and message.
   *
   * @param title   the dialog title
   * @param message the message to display
   */
  public static void show(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
