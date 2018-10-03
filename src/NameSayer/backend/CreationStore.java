package NameSayer.backend;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.beans.InvalidationListener;

/**
 * Represents a centralised collection of all creations.
 * Invalidates whenever creations are created/removed.
 */
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

        // Auto-cleanup of creations that no longer have any recordings associated.
        creation.addListener(o -> {
            if (creation.getRecordingCount() == 0) {
                _creations.remove(name);
            }
        });

        _creations.put(name, creation);
        return creation;
    }

    public List<Creation> getCreations() {
        // Note: this is a clone of the list, and is therefore not synchronised with the
        // creation store after returning. Refetch list upon invalidation.
        return new ArrayList<Creation>(_creations.values());
    }

    public void clear(){
        _creations.clear();
    }

    public void debugDump() {
        System.out.println("Creations Store Dump:");
        for (String name : _creations.keySet()) {
            System.out.println(" - Creation<" + name + ">");
            _creations.get(name).debugDump();
        }
    }

}
