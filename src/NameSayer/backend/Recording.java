package NameSayer.backend;

import java.nio.file.Path;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

/**
 * Represents a recording file.
 */
public class Recording {

    // Create phantom recording objects to represent temporary concatenated wave files used
    // for playback, e.g. in the practice tool. These are not actually stored in the RecordingStore.
    public static Recording representConcatenated(List<Recording> recordings, Path audioPath) {
        Creation creation = new Creation(Recording.getCombinedName(recordings));
        Recording recording = new Recording(creation, new Date(), audioPath, Recording.Type.VERSION);
        return recording;
    }

    public static String getCombinedName(List<Recording> recordings) {
        StringBuilder concatenatedName = new StringBuilder();

        if (recordings.size() == 0) return "";

        for (Recording recording : recordings) {
            concatenatedName.append(recording.getCreation().getName());
            concatenatedName.append(" ");
        }
        concatenatedName.deleteCharAt(concatenatedName.length() - 1);

        return concatenatedName.toString();
    }

    public enum Quality {
        QUALITY_UNRATED(0,3),
        QUALITY_1_STAR(1),
        QUALITY_2_STAR(2),
        QUALITY_3_STAR(3),
        QUALITY_4_STAR(4),
        QUALITY_5_STAR(5);

        private int _index;
        private int _goodness;

        private Quality(int index) {
            this(index, index);
        }

        private Quality(int index, int goodness) {
            _index = index;
            _goodness = goodness;
        }

        public int getIndex() {
            return _index;
        }

        public int getGoodness() {
            return _goodness;
        }

        public static Quality fromIndex(int index) {
            switch (index) {
                case 0:
                    return QUALITY_UNRATED;
                case 1:
                    return QUALITY_1_STAR;
                case 2:
                    return QUALITY_2_STAR;
                case 3:
                    return QUALITY_3_STAR;
                case 4:
                    return QUALITY_4_STAR;
                case 5:
                    return QUALITY_5_STAR;
            }
            assert false : "Invalid index of " + index;
            return QUALITY_UNRATED;
        }
    }

    public enum Type {
        VERSION, // From database.
        ATTEMPT, // From user recording.
    }

    private Creation _creation;
    private Date _date;
    private Path _path;
    private final Type _type;

    private ObjectProperty<Quality> _quality =
        new SimpleObjectProperty<Quality>(Quality.QUALITY_UNRATED);

    // Note: do not allow constructions outside of backend package - it should be
    // done through RecordingStore.
    Recording(Creation creation, Date date, Path path, Type type) {
        _creation = creation;
        _date = date;
        _path = path;
        _type = type;
        _creation.addRecording(this);
    }

    public Creation getCreation() {
        return _creation;
    }

    public Date getDate() {
        return _date;
    }

    public String getDateString() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        return format.format(_date);
    }

    public Path getPath() {
        return _path;
    }

    public Type getType() {
        return _type;
    }

    public Quality getQuality() {
        return _quality.getValue();
    }

    public void setQuality(Quality quality) {
        _quality.setValue(quality);
    }

    public ObjectProperty<Quality> qualityProperty() {
        return _quality;
    }

    public void delete() {
        Task<Void> deleter = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                Files.delete(getPath());
                return null;
            }

            @Override
            public void failed() {
                getException().printStackTrace();
            }
        };
        Thread th = new Thread(deleter);
        th.start();
    }

    public void debugDump() {
        System.out.println("        - date=" + _date);
        System.out.println("        - path=" + _path);
        System.out.println("        - type=" + _type);
    }

}
