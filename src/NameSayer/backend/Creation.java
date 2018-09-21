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

    void removeRecording(Recording recording) {
        Date date = recording.getDate();

        switch (recording.getType()) {
            case VERSION:
                assert _versions.containsKey(date);
                _versions.remove(date);
                break;
            case ATTEMPT:
                assert _attempts.containsKey(date);
                _attempts.remove(date);
                break;
        }
    }

    public String getName() {
        return _name;
    }

    public boolean has(Recording recording) {
        return _versions.containsValue(recording) || _attempts.containsValue(recording);
    }

    public List<Recording> getVersions() {
        return new ArrayList<Recording>(_versions.values());
    }

    public List<Recording> getAttempts() {
        return new ArrayList<Recording>(_attempts.values());
    }

    public List<Recording> getAllRecordings() {
        List<Recording> recordings = getVersions();
        recordings.addAll(getAttempts());
        return recordings;
    }

    public int getRecordingCount() {
        return _versions.size() + _attempts.size();
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
