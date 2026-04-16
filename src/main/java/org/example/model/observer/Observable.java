package org.example.model.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for observable model objects.
 *
 * <p>Subclasses inherit observer registration and notification for free,
 * and only need to call {@code notifyObservers(event)} at the right moments.
 */
public abstract class Observable {

  private final List<GameObserver> observers = new ArrayList<>();

  public void addObserver(GameObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(GameObserver observer) {
    observers.remove(observer);
  }

  public void notifyObservers(GameEvent event) {
    for (GameObserver observer : observers) {
      observer.onEvent(this, event);
    }
  }
}