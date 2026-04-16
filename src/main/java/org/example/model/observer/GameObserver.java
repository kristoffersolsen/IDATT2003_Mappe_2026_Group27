package org.example.model.observer;

/**
 * Observer interface for model state changes.
 *
 * <p>Any class that wants to react to model changes — typically a JavaFX
 * controller or view — implements this interface and registers itself with
 * an {@link Observable} model object.
 *
 * <p>The {@code source} parameter lets a single observer distinguish
 * between multiple observed objects without needing separate listener
 * registrations.
 */
public interface GameObserver {

  /**
   * Called by an observable model object when its state has changed.
   *
   * @param source the object that fired the event
   * @param event  the type of change that occurred
   */
  void onEvent(Object source, GameEvent event);
}