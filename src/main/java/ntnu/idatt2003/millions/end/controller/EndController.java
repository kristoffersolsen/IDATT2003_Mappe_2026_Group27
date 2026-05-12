package ntnu.idatt2003.millions.end.controller;

import javafx.application.Platform;
import ntnu.idatt2003.millions.end.view.EndScreen;
import ntnu.idatt2003.millions.app.controller.AppController;

/**
 * Controller for the end screen.
 *
 * <p>Wires the {@link EndScreen} buttons to their actions.
 * Exiting the application and starting a new game are both
 * handled here rather than in the view.
 */
public class EndController {

  private final EndScreen view;
  private final AppController appController;

  /**
   * Creates the controller and wires all button actions.
   *
   * @param view          the end screen view
   * @param appController the app-level controller that handles screen transitions
   */
  public EndController(EndScreen view, AppController appController) {
    this.view = view;
    this.appController = appController;

    wireNewGameButton();
    wireExitButton();
  }

  /**
   * Wires the new game button to return to the start screen.
   */
  private void wireNewGameButton() {
    view.getNewGameButton().setOnAction(e -> appController.showStartScreen());
  }

  /**
   * Wires the exit button to close the application.
   */
  private void wireExitButton() {
    view.getExitButton().setOnAction(e -> Platform.exit());
  }
}