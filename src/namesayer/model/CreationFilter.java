package namesayer.model;


import javafx.collections.ObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.InvalidationListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CreationFilter {
    /**
     * Setting up an enum for the only two possible sorting strategies being by NAME and by DATE, which are presented to
     * the user in the comboBox next to the filter.
     */
    public enum SortStrategy {
        DONT_SORT("Not sorted"),
        SORT_BY_NAME("Sort by name"),
        SORT_BY_DATE("Most recent first");

        private String _text;

        private SortStrategy(String text) {
            _text = text;
        }

        public String toString() {
            return _text;
        }
    }

    private CreationsList _filterResults;
    private CreationsList _intermediateList;

    private ObservableList<List<String>> _requestedNames;
    private CreationStore _creationStore;
    private ObjectProperty<SortStrategy> _sortStrategy =
        new SimpleObjectProperty<SortStrategy>(SortStrategy.DONT_SORT);
    private BooleanProperty _filterDisable = new SimpleBooleanProperty();
    private boolean _isPublishing = false;

    public CreationFilter(ObservableList<List<String>> requestedNames, CreationStore creationStore) {
        _requestedNames = requestedNames;
        _creationStore = creationStore;
        _filterResults = new CreationsList(creationStore);
        _intermediateList = new CreationsList(creationStore);

        // Adding listeners to ensure that whenever the text in the filter changes or the creation it self changes,
        // the filter is upadated.
        InvalidationListener filterUpdater = o -> updateFilter();
        _requestedNames.addListener(filterUpdater);
        _creationStore.addListener(filterUpdater);
        _sortStrategy.addListener(filterUpdater);
        _filterDisable.addListener(filterUpdater);
        _filterResults.addListener(filterUpdater);

        _filterDisable.addListener(o -> {
            if (_filterDisable.get()) {
                _sortStrategy.setValue(SortStrategy.SORT_BY_NAME);
            } else {
                _sortStrategy.setValue(SortStrategy.DONT_SORT);
            }
        });
    }

    public ObjectProperty<SortStrategy> sortStrategyProperty() {
        return _sortStrategy;
    }

    public BooleanProperty filterDisableProperty() {
        return _filterDisable;
    }

    public CreationsList getFilterResults() {
        return _filterResults;
    }

    public void shuffle() {
        _sortStrategy.setValue(SortStrategy.DONT_SORT);
        Collections.shuffle(_requestedNames);
    }

    private void updateFilter() {
        if (_isPublishing) {
            // We want to update our filter list for all events *except* for when we
            // are causing it via publishResults. Otherwise, this is an infinite loop.
            return;
        }

        _intermediateList.clear();

        if (_filterDisable.get()) {
            _intermediateList.addAllCreations();
        } else {
            for (List<String> names : _requestedNames) {
                _intermediateList.addWithNames(names);
            }
        }

        // After having the list, based on the sorting strategy modify the list and then publish the results.
        switch (_sortStrategy.getValue()) {
            case DONT_SORT:
                break;
            case SORT_BY_NAME:
                sortByName();
                break;
            case SORT_BY_DATE:
                sortByDate();
                break;
        }
        publishResults();
    }

    private void sortByName() {
        Collections.sort(_intermediateList, Comparator.comparing(CreationsListEntry::toString));
    }

    private void sortByDate() {
        Collections.sort(_intermediateList, (entry1, entry2) -> {
            return entry2.lastModified().compareTo(entry1.lastModified());
        });
    }

    private void publishResults() {
        _isPublishing = true;

        // Motivation: Don't want to operate on observable list (e.g. while sorting list).
        _filterResults.setAll(_intermediateList);

        _isPublishing = false;
    }

}
