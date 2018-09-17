package NameSayer.backend;

import java.util.List;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.beans.InvalidationListener;

public class CreationStore extends ObservableBase {

    private final ObservableMap<String,Creation> _creations = FXCollections.observableHashMap();

    public CreationStore() {
        InvalidationListener listener = o -> invalidate();
        _creations.addListener(listener);
    }

    public Creation get(String name) {
        return _creations.get(name);
    }

    public Creation add(String name) {
        assert !_creations.containsKey(name);
        Creation creation = new Creation(name);
        _creations.put(name, creation);
        return creation;
    }

    public List<Creation> getCreations() {
        return new ArrayList<Creation>(_creations.values());
    }

    public void debugDump() {
        System.out.println("Creations Store Dump:");
        for (String name : _creations.keySet()) {
            System.out.println(" - Creation<" + name + ">");
            _creations.get(name).debugDump();
        }
    }

}
