package NameSayer.backend;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.InvalidationListener;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import NameSayer.backend.CreationStore;

public class CreationsListEntry extends ObservableBase {

    private List<String> _names;
    private Map<String, Creation> _creations = new HashMap<>();
    private CreationStore _creationStore;
    private IntegerProperty _streaks = new SimpleIntegerProperty();
    private InvalidationListener _storeChangeHandler = o -> refresh();
    private InvalidationListener _creationChangeHandler = o -> invalidate();
    private InvalidationListener _streakChangeHandler = o -> updateStreaks();

    CreationsListEntry(String name, CreationStore creationStore) {
        this(Collections.singletonList(name), creationStore);
        assert !name.contains(" ") && !name.contains("-");
    }

    CreationsListEntry(List<String> names, CreationStore creationStore) {
        assert names.size() > 0;
        assert creationStore != null;
        _names = names;
        _creationStore = creationStore;

        refresh();
    }

    public boolean isSingleRecording() {
        if (_names.size() > 1) return false;
        if (_creations.size() > 1) return false;
        if (_creations.isEmpty()) return false;

        Creation theOnlyCreation = getCreations().get(0);
        if (theOnlyCreation.getRecordingCount() > 1) return false;
        assert !theOnlyCreation.getVersions().isEmpty();

        return true;
    }

    public List<String> getNames() {
        return new ArrayList<String>(_names);
    }

    public List<Creation> getCreations() {
        return new ArrayList<Creation>(_creations.values());
    }

    public Creation getCreation(String name) {
        return _creations.get(name);
    }

    public Creation getOverallAttemptsCreation() {
        if (_names.size() == 1) {
            return null;
        } else {
            return _creationStore.get(toString());
        }
    }

    public List<Recording> getRecordings() {
        List<Recording> recordings = new ArrayList<>();
        for (String name : _names) {
            Creation creation = _creations.get(name);
            if (creation == null) continue;
            Recording recording = creation.getBestRecording();
            if (recording == null) continue;
            recordings.add(recording);
        }
        return recordings;
    }

    public Date lastModified() {
        Date latestDate = new Date(0);
        for (Creation creation : _creations.values()) {
            Date candidateDate = creation.getLatestRecording().getDate();
            if (candidateDate.compareTo(latestDate) >= 0) {
                latestDate = candidateDate;
            }
        }
        return latestDate;
    }

    public boolean has(Creation creation) {
        return getCreations().contains(creation);
    }

    public String toString() {
        return String.join(" ", _names);
    }

    public IntegerProperty streaksProperty() {
        return _streaks;
    }

    public boolean matchesRecordings(List<Recording> recordings) {
        List<Recording> ourRecordings = getRecordings();
        if (ourRecordings.size() == 0) {
            return false;
        };
        if (ourRecordings.size() != recordings.size()) {
            return false;
        }
        for (int i = 0; i < ourRecordings.size(); i++) {
            if (recordings.get(i).getCreation() != ourRecordings.get(i).getCreation()) {
                return false;
            }
        }
        return true;
    }

    private void refresh() {
        List<Recording> bestRecordings = new ArrayList<>();

        _creationStore.removeListener(_storeChangeHandler);
        _creationStore.addListener(_storeChangeHandler);
        _creations.clear();

        for (String name : _names) {
            Creation creation = _creationStore.get(name);
            if (creation == null) continue;

            creation.removeListener(_creationChangeHandler);
            creation.addListener(_creationChangeHandler);

            creation.streaksProperty().removeListener(_streakChangeHandler);
            creation.streaksProperty().addListener(_streakChangeHandler);

            _creations.put(name, creation);

            Recording recording = creation.getBestRecording();
            if (recording != null) {
                bestRecordings.add(recording);
            }
        }

        updateStreaks();
    }

    private void updateStreaks() {
        int streakValue = -1;

        // Use the minimum streak value.
        for (Creation creation : _creations.values()) {
            if (streakValue == -1 || creation.getStreaks() < streakValue) {
                streakValue = creation.getStreaks();
            }
        }

        _streaks.setValue(streakValue);
    }

}
