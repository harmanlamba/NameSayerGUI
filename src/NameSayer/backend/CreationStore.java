package NameSayer.backend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class CreationStore {

    private final ObservableMap<String,Creation> _creations = FXCollections.observableHashMap();

    public Creation get(String name) {
        return _creations.get(name);
    }

    public Creation add(String name) {
        assert !_creations.containsKey(name);
        Creation creation = new Creation(name);
        _creations.put(name, creation);
        return creation;
    }

    public void debugDump() {
        System.out.println("Creations Store Dump:");
        for (String name : _creations.keySet()) {
            System.out.println(" - Creation<" + name + ">");
            _creations.get(name).debugDump();
        }
    }

}