package NameSayer.backend;

import java.util.List;
import java.util.ArrayList;

import javafx.beans.Observable;
import javafx.beans.InvalidationListener;
import javafx.application.Platform;

public class ObservableBase implements Observable {

    private final List<InvalidationListener> _listeners = new ArrayList<>();

    public void addListener(InvalidationListener listener) {
        _listeners.add(listener);
    } 

    public void removeListener(InvalidationListener listener) {
        _listeners.remove(listener);
    } 

    protected void invalidate() {
        for (InvalidationListener listener : _listeners) {
            Platform.runLater(() -> listener.invalidated(this));
        }
    }

}
