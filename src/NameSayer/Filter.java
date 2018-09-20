package NameSayer;

import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

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

    public ObservableList getfilterResults() {
        return _filterResults;
    }
}
