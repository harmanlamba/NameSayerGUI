package namesayer.controller;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Streaks indicator component. Has a nice fire to it.
 */
public class Streaks extends HBox {

    private static final String FIRE_CHAR = "\uf319";
    private static final int THRESHOLD = 3;

    private final IntegerProperty _streaks = new SimpleIntegerProperty();

    public Streaks() {
        super();

        // Inner components:

        Label count = new Label();
        count.setPrefWidth(30);
        count.setAlignment(Pos.CENTER_RIGHT);

        Label fire = new Label(FIRE_CHAR);
        fire.getStyleClass().add("streaks-fire");

        // Outer component:

        setSpacing(10);
        getChildren().setAll(count, fire);

        // Bindings:

        // Just display the number as a text.
        count.textProperty().bind(_streaks.asString());

        // Set the threshold before the streaks is displayed.
        visibleProperty().bind(_streaks.greaterThanOrEqualTo(THRESHOLD));
    }

    /**
     * Sync this component with a streaks property of a creation or a creationsListEntry.
     */
    public void bindStreaks(IntegerProperty streaksProperty) {
        _streaks.bind(streaksProperty);
    }

}
