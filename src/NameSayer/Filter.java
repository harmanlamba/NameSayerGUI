package NameSayer;

import ControllersAndFXML.CreationsListView;
import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;
import NameSayer.backend.Recording;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {

    private ObservableList<Creation> _filterResults= FXCollections.observableArrayList();
    private TextInputControl _textProperty;
    private CreationStore _creation;

    public Filter(TextInputControl textProperty, CreationStore creation){
        _textProperty=textProperty;
        _creation=creation;
    }

    public void updateFilter(){
        List<Creation> creationList= _creation.getCreations();
        String filterText= _textProperty.getText();
        //Setting up the Reggex
        Pattern p= Pattern.compile(filterText.toLowerCase());
        for(Creation counter: creationList){
            Matcher m= p.matcher(counter.getName().toLowerCase());
            boolean isMatch=m.matches();
            if(isMatch){
                _filterResults.add(counter);
            }
        }
    }

    public void sortByName(){
        List<Creation> creationList =_creation.getCreations();
        _filterResults.clear();
        for(Creation counter:creationList){
            _filterResults.add(counter);
        }
        _filterResults.sorted(new Comparator<Creation>() {
            @Override
            public int compare(Creation o1, Creation o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public void sortByDate(){
        List<Creation> creationList= _creation.getCreations();
        List<Recording> recordingList= creationList.get(0).getAllRecordings();
        recordingList.sort(new Comparator<Recording>() {
            @Override
            public int compare(Recording o1, Recording o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        _filterResults.clear();
        Collections.reverse(recordingList);
        for(Recording recordingCounter: recordingList){
            _filterResults.add(recordingCounter.getCreation());
        }

    }

    public ObservableList getfilterResults() {
        return _filterResults;
    }

}
