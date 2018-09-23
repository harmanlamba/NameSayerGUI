package ControllersAndFXML;

import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import NameSayer.backend.Recording;

public class QualityStars extends HBox {

    //Setting up the constants for the icons of the filled and unfilled stars
    private static final String STAR_FILLED_CHAR = "\uf2fc";
    private static final String STAR_UNFILLED_CHAR = "\uf3ae";

    private final ObjectProperty<Recording> _recording = new SimpleObjectProperty<Recording>();

    private final Label[] _stars = {
        new Label(STAR_UNFILLED_CHAR),
        new Label(STAR_UNFILLED_CHAR),
        new Label(STAR_UNFILLED_CHAR),
        new Label(STAR_UNFILLED_CHAR),
        new Label(STAR_UNFILLED_CHAR),
    };

    public QualityStars() {
        //Setting the stars into the HBox, it is important to note that quality stars is a custom component that extends
        //an HBox
        getChildren().setAll(_stars);
        //Iterating through all the stars and adding a mouseClicked event and the adequate style class.
        for (int i = 0; i < _stars.length; i++) {
            final int qualityIndex = i + 1;
            _stars[i].setOnMouseClicked(event -> selectQuality(qualityIndex));
            _stars[i].getStyleClass().add("quality-star");
        }
        //Ensuring that the quality stars get updated when a new quality is assigned.
        _recording.addListener(o1 -> {
            renderQuality();
            _recording.getValue().qualityProperty().addListener(o2 -> renderQuality());
        });
    }

    private void selectQuality(int i) {
        if (_recording.getValue() == null) return;
        if (i < 1) i = 1;
        if (i > 5) i = 5;
        _recording.getValue().setQuality(Recording.Quality.fromIndex(i));
    }

    private void renderQuality() {
        int qualityIndex = _recording.getValue().getQuality().getIndex();
        for (int i = 0; i < _stars.length; i++) {
            if (i < qualityIndex) _stars[i].setText(STAR_FILLED_CHAR);
            else _stars[i].setText(STAR_UNFILLED_CHAR);
        }
    }

    public void setRecording(Recording recording) {
        _recording.setValue(recording);
    }

}

