package NameSayer;

import java.nio.file.Path;
import java.util.Date;

public class Recording {

    public enum Quality {
        QUALITY_1_STAR,
        QUALITY_2_STAR,
        QUALITY_3_STAR,
        QUALITY_4_STAR,
        QUALITY_5_STAR,
    }

    private Name _name;
    private Date _date;
    private Quality _quality;
    private Path _path;
    private RecordingStore _recordingStore;

    public Recording(Name name, Date date, RecordingStore recordingStore) {
        _name = name;
        _date = date;
        _recordingStore = recordingStore;
    }

    private Path getPath() {
        //return _recordingStore.getPathOf(_name, _date);
        // TODO
        assert false;
        return null;
    }

    public Quality getQuality() {
        return _quality;
    }

    public void setQuality(Quality quality) {
        _quality = quality;
        _recordingStore.invalidateQualities();
    }

}
