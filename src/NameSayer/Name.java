package NameSayer;

import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class Name {

    private String _name;
    private ObservableMap<Date,Recording> _versions = FXCollections.observableHashMap();
    private ObservableMap<Date,Recording> _attempts = FXCollections.observableHashMap();

    public Name(String name) {
        _name = name;
    }

    public void addRecording(Recording recording) {
        Date date = recording.getDate();
        Recording.Type type = recording.getType();

        switch (recording.getType()) {
            case VERSION:
                assert !_versions.containsKey(date);
                _versions.put(date, recording);
                break;
            case ATTEMPT:
                assert !_attempts.containsKey(date);
                _attempts.put(date, recording);
                break;
        }
    }

}
