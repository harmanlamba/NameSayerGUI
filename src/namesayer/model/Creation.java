package namesayer.model;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.beans.InvalidationListener;

/**
 * Represents the collection of all recordings for the same name.
 * Invalidates when recordings are added/removed from this creation.
 */
public class Creation extends ObservableBase {

    private String _name;
    private IntegerProperty _streaks = new SimpleIntegerProperty(0);
    private final ObservableMap<Date, Recording> _versions = FXCollections.observableHashMap();
    private final ObservableMap<Date, Recording> _attempts = FXCollections.observableHashMap();

    // Note: do not allow constructions outside of backend package - it should be
    // done through CreationStore.
    Creation(String name) {
        // Invalidate whenever recordings are added/removed.
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
                _versions.remove(date);
                break;
            case ATTEMPT:
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

    public Recording getBestRecording() {
        assert getRecordingCount() > 0;
        List<Recording> candidates = getAllRecordings();

        Collections.sort(candidates, (r1, r2) -> {

            // Prioritise database versions before user attempts.
            if (r1.getType() != r2.getType()) {
                switch (r1.getType()) {
                    case VERSION:
                        return -1;
                    case ATTEMPT:
                        return 1;
                }
            }

            // Then sort by quality.
            int value = r2.getQuality().getGoodness() - r1.getQuality().getGoodness();
            if (value != 0) {
                return value;
            }

            // Use randomness as tie-breaker.
            int random = (int) (Math.random() * 2) * 2 - 1;
            return random;

        });

        return candidates.get(0);
    }

    public Recording getLatestRecording() {
        Date latestDate = new Date(0);
        Recording latestRecording = null;
        for (Recording recording : _versions.values()) {
            if (recording.getDate().compareTo(latestDate) >= 0) {
                latestDate = recording.getDate();
                latestRecording = recording;
            }
        }
        return latestRecording;
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

    public int getStreaks() {
        return _streaks.get();
    }

    public IntegerProperty streaksProperty() {
        return _streaks;
    }

    public void setStreaks(int streaks) {
        _streaks.set(streaks);
    }
}
