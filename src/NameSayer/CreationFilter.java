package NameSayer;


import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;
import NameSayer.backend.Recording;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;


public class CreationFilter {
     /*
     Setting up an enum for the only two possible sorting strategies being by NAME and by DATE, which are presented to
     the user in the comboBox next to the filter.
      */
    public enum SortStrategy {
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

    private ObservableList<Creation> _filterResults= FXCollections.observableArrayList();
    private List<Creation> _intermediateList = new ArrayList<>();
    private StringProperty _textProperty;
    private CreationStore _creationStore;
    private ObjectProperty<SortStrategy> _sortStrategy =
        new SimpleObjectProperty<SortStrategy>(SortStrategy.SORT_BY_NAME);

    public CreationFilter(StringProperty textProperty, CreationStore creationStore){
        _textProperty=textProperty;
        _creationStore=creationStore;
        //Adding listeners to ensure that whenever the text in the filter changes or the creation it self changes,
        //the filter is upadated.
        textProperty.addListener(o -> updateFilter());
        creationStore.addListener(o -> updateFilter());
        _sortStrategy.addListener(o -> updateFilter());
    }

    public ObjectProperty<SortStrategy> sortStrategyProperty() {
        return _sortStrategy;
    }

    public ObservableList<Creation> getFilterResults() {
        return _filterResults;
    }

    private void updateFilter(){
        _intermediateList.clear();
        List<Creation> creationList= _creationStore.getCreations();
        String filterText= _textProperty.getValue().toLowerCase();
        //Searching for a match in the filter text and the creation name, if the creation name starts with the filter
        //text input add the creation to a separate list.
        for(Creation counter: creationList){
            if(counter.getName().toLowerCase().startsWith(filterText)){
                _intermediateList.add(counter);
            }
        }
        //After having the list, based on the sorting strategy modify the list and then publish the results
        switch(_sortStrategy.getValue()) {
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
        Collections.sort(_intermediateList, new Comparator<Creation>() {
            @Override
            public int compare(Creation o1, Creation o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    //This method implementation sorts creations by the date of the latest recording
    private void sortByDate(){
        Collections.sort(_intermediateList, new Comparator<Creation>() {
            @Override
            public int compare(Creation o1, Creation o2) {
                List<Recording> recordings1 = o1.getAllRecordings();
                List<Recording> recordings2 = o2.getAllRecordings();
                Date latest1 = recordings1.get(0).getDate();
                Date latest2 = recordings2.get(0).getDate();
                //Iterating through and finding the latest recording which then gets returned
                for (Recording recording : recordings1) {
                    if (recording.getDate().compareTo(latest1) > 0) {
                        latest1 = recording.getDate();
                    }
                }
                for (Recording recording : recordings2) {
                    if (recording.getDate().compareTo(latest2) > 0) {
                        latest2 = recording.getDate();
                    }
                }
                return latest2.compareTo(latest1);
            }
        });
    }

    private void publishResults() {
        _filterResults.setAll(_intermediateList);
    }

}
