package NameSayer.backend;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.beans.InvalidationListener;

public class Creation extends ObservableBase {

    private String _name;
    private final ObservableMap<Date,Recording> _versions = FXCollections.observableHashMap();
    private final ObservableMap<Date,Recording> _attempts = FXCollections.observableHashMap();

    Creation(String name) {
        InvalidationListener listener = o -> invalidate();
        _versions.addListener(listener);
        _attempts.addListener(listener);
        _name = name;
    }

    void addRecording(Recording recording) {
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

    public List<Recording> getVersions() {
        return new ArrayList<Recording>(_versions.values());
    }

    public List<Recording> getAttempts() {
        return new ArrayList<Recording>(_attempts.values());
    }

    public void debugDump() {
        System.out.println("    - name=" + _name);
        for (Date date : _versions.keySet()) {
            System.out.println("    - version<" + date + ">");
            _versions.get(date).debugDump();
        }
        for (Date date : _attempts.keySet()) {
            System.out.println("    - attempt<" + date + ">");
            _attempts.get(date).debugDump();
        }
    }

}
