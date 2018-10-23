package namesayer.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.InvalidationListener;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * Represents a requested name.
 * A list of single name words and the available creations needed to build this requested name.
 */
public class CreationsListEntry extends ObservableBase {

    private final static String NAME_DELIMETER_PATTERN = "[\\s\\-]+";

    public static List<String> parseNamesIntoList(String names) {
        // TODO filter out bad values.
        List<String> list = Arrays.asList(names.toLowerCase().split(NAME_DELIMETER_PATTERN));
        list = new ArrayList<String>(list);
        list.removeIf(entry -> entry.isEmpty());
        return list;
    }

    private List<String> _names;
    private Map<String, Creation> _creations = new HashMap<>();
    private CreationStore _creationStore;
    private IntegerProperty _streaks = new SimpleIntegerProperty();
    private InvalidationListener _storeChangeHandler = o -> refresh();
    private InvalidationListener _creationChangeHandler = o -> invalidate();
    private InvalidationListener _streakChangeHandler = o -> updateStreaks();

    private CreationsListEntry(CreationStore creationStore) {
        assert creationStore != null;
        _creationStore = creationStore;
    }

    /**
     * Create from an unparsed string or from a single name.
     */
    CreationsListEntry(String name, CreationStore creationStore) {
        this(creationStore);
        _names = parseNamesIntoList(name);
        refresh();
    }

    /**
     * Create from a parsed list of name words to build the desired name.
     */
    CreationsListEntry(List<String> names, CreationStore creationStore) {
        this(creationStore);
        assert names.size() > 0;
        _names = names;
        refresh();
    }

    /**
     * @return Whether this entry is representable by a single recording.
     */
    public boolean isSingleRecording() {
        if (_names.size() > 1) return false;
        if (_creations.size() > 1) return false;
        if (_creations.isEmpty()) return false;

        Creation theOnlyCreation = getCreations().get(0);
        if (theOnlyCreation.getRecordingCount() > 1) return false;
        if (theOnlyCreation.getVersions().isEmpty()) return false;

        return true;
    }

    /**
     * @return Name words that make up the full/single name represented by this entry.
     */
    public List<String> getNames() {
        return new ArrayList<String>(_names);
    }

    /**
     * @return Creations (in order) that are available for the requested name words.
     */
    public List<Creation> getCreations() {
        return new ArrayList<Creation>(_creations.values());
    }

    /**
     * @param name
     * @return Creation for a given name word, or null if no such creation is available, or if
     *         the given name word is not part of this CreationsListEntry.
     */
    public Creation getCreation(String name) {
        return _creations.get(name);
    }

    /**
     * @return Creation that represents user attempts for the overall concatenated name, if it exists.
     *         Null if no such attempts were made yet, or if there is only a single name word in
     *         this CreationsListEntry as such entries don't have an overall concatenated name.
     */
    public Creation getOverallAttemptsCreation() {
        if (_names.size() == 1) {
            return null;
        } else {
            return _creationStore.get(toString());
        }
    }

    /**
     * @return Sequence of recordings that most optimally satisfy the desired name words.
     */
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

    /**
     * @return The last modified date of all creations in this entry.
     */
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

    public String toString() {
        return String.join(" ", _names);
    }

    /**
     * @return The min streak count of the contained creations.
     */
    public IntegerProperty streaksProperty() {
        return _streaks;
    }

    /**
     * Search for the starting index of the first subsequence in the list of recordings that
     * matches this CreationsListEntry in terms of their associated creations.
     * @param recordings List in which to search.
     * @return Starting index of such subsequence, or -1 if not found.
     */
    public int findInRecordings(List<Recording> recordings) {
        List<Creation> ourCreations = new ArrayList<>();
        for (Recording r : getRecordings()) {
            ourCreations.add(r.getCreation());
        }

        if (ourCreations.size() == 0) {
            return -1;
        }

        List<Creation> theirCreations = new ArrayList<>();
        for (Recording r : recordings) {
            theirCreations.add(r.getCreation());
        }

        return Collections.indexOfSubList(theirCreations, ourCreations);
    }

    /**
     * Determines if this CreationsListEntry is represented in the given list of recordings.
     * @param recordings
     * @return Whether there exists a representative subsequence in the list.
     */
    public boolean includedInRecordings(List<Recording> recordings) {
        return findInRecordings(recordings) != -1;
    }

    /**
     * Call this whenever creations are added or removed to the CreationStore.
     * This keeps track of the creations that are available to represent the name words requested.
     * When new creations are added, they are listened for changes in their list of recordings.
     * When their streaks change, the overall streaks counter is also updated.
     */
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

        Creation overallCreation = getOverallAttemptsCreation();
        if (overallCreation != null) {
            overallCreation.removeListener(_creationChangeHandler);
            overallCreation.addListener(_creationChangeHandler);
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
