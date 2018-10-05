package NameSayer.backend;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

import java.util.List;

public class CreationsList extends SimpleListProperty<CreationsListEntry> {

    private CreationStore _creationStore;

    public CreationsList(CreationStore creationStore) {
        super(FXCollections.observableArrayList());
        _creationStore = creationStore;

        InvalidationListener entryChangeListener = o -> fireValueChangedEvent();

        ListChangeListener<CreationsListEntry> listChangeListener = change -> {
            while (change.next()) {
                for (CreationsListEntry entry : change.getRemoved()) {
                    entry.removeListener(entryChangeListener);
                }
                for (CreationsListEntry entry : change.getAddedSubList()) {
                    entry.addListener(entryChangeListener);
                }
            }
        };
        addListener(listChangeListener);
    }

    public void addWithName(String name) {
        add(new CreationsListEntry(name, _creationStore));
    }

    public void addWithNames(List<String> names) {
        add(new CreationsListEntry(names, _creationStore));
    }

    public void addAllCreations() {
        for (Creation creation : _creationStore.getCreations()) {

            // Don't add attempt-only creations.
            if (!creation.getVersions().isEmpty()) {
                addWithName(creation.getName());
            }

        }
    }

    public int indexOfCreation(Creation creation) {
        for (CreationsListEntry entry : this) {
            if (entry.has(creation)) {
                return indexOf(entry);
            }
        }
        return -1;
    }

}
