package NameSayer.backend;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Recording {

    public enum Quality {
        QUALITY_UNRATED(0),
        QUALITY_1_STAR(1),
        QUALITY_2_STAR(2),
        QUALITY_3_STAR(3),
        QUALITY_4_STAR(4),
        QUALITY_5_STAR(5);

        private int _index;

        private Quality(int index) {
            _index = index;
        }

        public int getIndex() {
            return _index;
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
        SimpleDateFormat format = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss");
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

    public void debugDump() {
        System.out.println("        - date=" + _date);
        System.out.println("        - path=" + _path);
        System.out.println("        - type=" + _type);
    }

}
