package namesayer.model;


import namesayer.model.CreationStore;
import namesayer.model.CreationsList;
import javafx.collections.ObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.InvalidationListener;

import java.util.Collections;
import java.util.List;


public class CreationFilter {
     /*
     Setting up an enum for the only two possible sorting strategies being by NAME and by DATE, which are presented to
     the user in the comboBox next to the filter.
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
    };

    private CreationsList _filterResults;
    private CreationsList _intermediateList;

    private ObservableList<List<String>> _requestedNames;
    private CreationStore _creationStore;
    private ObjectProperty<SortStrategy> _sortStrategy =
        new SimpleObjectProperty<SortStrategy>(SortStrategy.DONT_SORT);
    private BooleanProperty _filterDisable = new SimpleBooleanProperty();

    public CreationFilter(ObservableList<List<String>> requestedNames, CreationStore creationStore) {
        _requestedNames = requestedNames;
        _creationStore = creationStore;
        _filterResults = new CreationsList(creationStore);
        _intermediateList = new CreationsList(creationStore);
        //Adding listeners to ensure that whenever the text in the filter changes or the creation it self changes,
        //the filter is upadated.
        InvalidationListener filterUpdater = o -> updateFilter();
        _requestedNames.addListener(filterUpdater);
        _creationStore.addListener(filterUpdater);
        _sortStrategy.addListener(filterUpdater);
        _filterDisable.addListener(filterUpdater);
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

    private void updateFilter() {
        _intermediateList.clear();

        if (_filterDisable.get()) {
            _intermediateList.addAllCreations();
        } else {
            for (List<String> names : _requestedNames) {
                _intermediateList.addWithNames(names);
            }
        }

        //After having the list, based on the sorting strategy modify the list and then publish the results
        switch(_sortStrategy.getValue()) {
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

    private void sortByName(){
        Collections.sort(_intermediateList, (entry1, entry2) -> {
            return entry1.toString().compareTo(entry2.toString());
        });
    }

    private void sortByDate(){
        Collections.sort(_intermediateList, (entry1, entry2) -> {
            return entry2.lastModified().compareTo(entry1.lastModified());
        });
    }

    private void publishResults() {
        // Motivation: Don't want to operate on observable list (e.g. while sorting list).
        _filterResults.setAll(_intermediateList);
    }

}
