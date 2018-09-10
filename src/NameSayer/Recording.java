package NameSayer;

import java.nio.file.Path;
import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Recording {

    public enum Quality {
        QUALITY_UNRATED,
        QUALITY_1_STAR,
        QUALITY_2_STAR,
        QUALITY_3_STAR,
        QUALITY_4_STAR,
        QUALITY_5_STAR,
    }

    public enum Type {
        VERSION, // From database.
        ATTEMPT, // From user recording.
    }

    private Name _name;
    private Date _date;
    private Path _path;
    private final Type _type;

    private ObjectProperty<Quality> _quality =
        new SimpleObjectProperty<Quality>(Quality.QUALITY_UNRATED);

    public Recording(Name name, Date date, Path path, Type type) {
        _name = name;
        _name.addRecording(this);
        _date = date;
        _path = path;
        _type = type;
    }

    public Date getDate() {
        return _date;
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

}
