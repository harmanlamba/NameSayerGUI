package namesayer.model;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

import java.util.List;

/**
 * List of requested names to display and practice.
 * Each CreationsListEntry in this list captures the list of name words that are needed to
 * build the requested full name.
 */
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

    /**
     * Add a single name word, or an unparsed full name, to the list.
     */
    public void addWithName(String name) {
        add(new CreationsListEntry(name, _creationStore));
    }

    /**
     * Add the parsed list of name words needed to make a full name, to the list.
     */
    public void addWithNames(List<String> names) {
        add(new CreationsListEntry(names, _creationStore));
    }

    /**
     * Populate this list with all names in the creation store.
     */
    public void addAllCreations() {
        for (Creation creation : _creationStore.getCreations()) {
            addWithName(creation.getName());
        }
    }

    /**
     * @param recording
     * @return Whether this recording is included by any Creation in this list's entries.
     */
    public boolean hasRecording(Recording recording) {
        for (CreationsListEntry entry : this) {
            if (entry.getRecordings().contains(recording)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param selectedRecordings
     * @return The index of the first CreationsListEntry in this list that is found to be included
     *         in the given selectedRecordings list.
     * @see #lastIndexInSelection(List<Recording>)
     */
    public int firstIndexInSelection(List<Recording> selectedRecordings) {
        for (int i = 0; i < size(); i++) {
            CreationsListEntry entry = get(i);
            if (entry.includedInRecordings(selectedRecordings)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param selectedRecordings
     * @return The index of the last CreationsListEntry in this list that is found to be included
     *         in the given selectedRecordings list.
     * @see #firstIndexInSelection(List<Recording>)
     */
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
