package NameSayer;

import ControllersAndFXML.CreationsListView;
import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;
import NameSayer.backend.Recording;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.StringProperty;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreationFilter {

    private ObservableList<Creation> _filterResults= FXCollections.observableArrayList();
    private List<Creation> _intermediateList = new ArrayList<>();
    private StringProperty _textProperty;
    private CreationStore _creationStore;

    public CreationFilter(StringProperty textProperty, CreationStore creationStore){
        _textProperty=textProperty;
        _creationStore=creationStore;
        textProperty.addListener(o -> updateFilter());
        creationStore.addListener(o -> updateFilter());
    }

    public void updateFilter(){
        _intermediateList.clear();
        List<Creation> creationList= _creationStore.getCreations();
        String filterText= _textProperty.getValue().toLowerCase();
        for(Creation counter: creationList){
            if(counter.getName().toLowerCase().startsWith(filterText)){
                _intermediateList.add(counter);
            }
        }
        sortByName();
        publishResults();
    }

    public void sortByName(){
        Collections.sort(_intermediateList, new Comparator<Creation>() {
            @Override
            public int compare(Creation o1, Creation o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public void sortByDate(){
        Collections.sort(_intermediateList, new Comparator<Creation>() {
            @Override
            public int compare(Creation o1, Creation o2) {
                List<Recording> recordings1 = o1.getAllRecordings();
                List<Recording> recordings2 = o2.getAllRecordings();
                Date latest1 = recordings1.get(0).getDate();
                Date latest2 = recordings2.get(0).getDate();
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
                return latest1.compareTo(latest2);
            }
        });
    }

    public void publishResults() {
        _filterResults.setAll(_intermediateList);
    }

    public ObservableList<Creation> getFilterResults() {
        return _filterResults;
    }

}
