package namesayer.model;

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
            addWithName(creation.getName());
        }
    }

    public boolean hasRecording(Recording recording) {
        for (CreationsListEntry entry : this) {
            if (entry.getRecordings().contains(recording)) {
                return true;
            }
        }
        return false;
    }

    public int firstIndexInSelection(List<Recording> selectedRecordings) {
        for (int i = 0; i < size(); i++) {
            CreationsListEntry entry = get(i);
            if (entry.includedInRecordings(selectedRecordings)) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexInSelection(List<Recording> selectedRecordings) {
        for (int i = size() - 1; i >= 0; i--) {
            CreationsListEntry entry = get(i);
            if (entry.includedInRecordings(selectedRecordings)) {
                return i;
            }
        }
        return -1;
    }

}
