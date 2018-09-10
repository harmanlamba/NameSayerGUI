package NameSayer;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import javafx.collections.ObservableSet;
import javafx.collections.ObservableMap;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.beans.InvalidationListener;

/**
 * @Deprecated
 */
public class MapBackedObservableSet<K,V> extends HashSet<V> implements ObservableSet<V> {

    protected final ObservableMap<K,V> _map = FXCollections.observableHashMap();
    private final List<InvalidationListener> _invalidationListeners = new ArrayList<>();
    private final List<SetChangeListener<? super V>> _setChangeListeners = new ArrayList<>();

    public MapBackedObservableSet() {
        MapChangeListener<K,V> mapListener = change -> {
            V added = change.getValueAdded();
            V removed = change.getValueRemoved();
            if (added != null) add(added);
            if (removed != null) remove(removed);
            SetChangeListener.Change<V> setChange = new SetChangeListener.Change<V>(this) {
                @Override public V getElementAdded() { return added; }
                @Override public V getElementRemoved() { return removed; }
                @Override public boolean wasAdded() { return added != null; }
                @Override public boolean wasRemoved() { return removed != null; }
            };
            for (InvalidationListener listener : _invalidationListeners) {
                listener.invalidated(this);
            }
            for (SetChangeListener<? super V> listener : _setChangeListeners) {
                listener.onChanged(setChange);
            }
        };
        _map.addListener(mapListener);
    }

    public void addListener(InvalidationListener listener) {
        _invalidationListeners.add(listener);
    }

    public void removeListener(InvalidationListener listener) {
        _invalidationListeners.remove(listener);
    }

    public void addListener(SetChangeListener<? super V> listener) {
        _setChangeListeners.add(listener);
    }

    public void removeListener(SetChangeListener<? super V> listener) {
        _setChangeListeners.remove(listener);
    }

    public boolean add(V element) {
        throw new UnsupportedOperationException();
    }

}
