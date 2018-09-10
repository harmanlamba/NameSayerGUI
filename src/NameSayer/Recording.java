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

    private Name _name;
    private Date _date;
    private Path _path;

    private ObjectProperty<Quality> _quality =
        new SimpleObjectProperty<Quality>(Quality.QUALITY_UNRATED);

    public Recording(Name name, Date date, Path path) {
        _name = name;
        _date = date;
        _path = path;
    }

    private Path getPath() {
        return _path;
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
