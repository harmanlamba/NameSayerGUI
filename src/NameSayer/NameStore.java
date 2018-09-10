package NameSayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class NameStore {

    private final ObservableMap<String,Name> _names = FXCollections.observableHashMap();

    public Name get(String name) {
        return _names.get(name);
    }

    public Name add(String nameString) {
        assert !_names.containsKey(nameString);
        Name name = new Name(nameString);
        _names.put(nameString, name);
        return name;
    }
}
