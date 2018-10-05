package NameSayer.backend;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

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

    private ObjectProperty<Recording> _representativeRecording = new SimpleObjectProperty<Recording>();

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

    public ObjectProperty<Recording> representativeRecordingProperty() {
        return _representativeRecording;
    }

    public Recording getRepresentativeRecording() {
        return _representativeRecording.getValue();
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
        if (_representativeRecording.getValue().getCreation() == creation) {
            return true;
        }
        return getCreations().contains(creation);
    }

    public String toString() {
        return String.join(" ", _names);
    }

    private void refresh() {
        List<Recording> bestRecordings = new ArrayList<>();

        _creationStore.addListener(o -> refresh());
        _creations.clear();

        for (String name : _names) {
            Creation creation = _creationStore.get(name);
            if (creation == null) continue;
            creation.addListener(o -> invalidate());
            _creations.put(name, creation);

            Recording recording = creation.getBestRecording();
            if (recording != null) {
                bestRecordings.add(recording);
            }
        }

        if (bestRecordings.size() == 1) {
            _representativeRecording.setValue(bestRecordings.get(0));
        } else {
            Recording recording = Recording.representConcatenated(bestRecordings, null); // TODO PATH!!
            _representativeRecording.setValue(recording);
        }
    }

}
