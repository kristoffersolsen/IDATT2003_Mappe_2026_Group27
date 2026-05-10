package ntnu.idatt2003.millions.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Builds a vertically-centered card layout used by {@link StartView} and {@link EndView}.
 *
 * <p>Renders a title above a card containing the given content, with
 * spacers above and below that push everything to vertical center.
 */
public final class CenteredCardLayout {

  private CenteredCardLayout() {
  }

  /**
   * Builds the layout.
   *
   * @param title the large title shown above the card
   * @param card  the card content
   * @return the assembled root VBox
   */
  public static VBox build(Node title, Node card) {
    Region topSpacer = new Region();
    Region bottomSpacer = new Region();
    VBox.setVgrow(topSpacer, Priority.ALWAYS);
    VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

    VBox root = new VBox(topSpacer, title, card, bottomSpacer);
    root.setAlignment(Pos.CENTER);
    root.getStyleClass().add("screen-root");
    return root;
  }
}
