package ControllersAndFXML;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Streaks extends HBox {

    private static final String FIRE_CHAR = "\uf319";

    private final IntegerProperty _streaks = new SimpleIntegerProperty();

    public Streaks() {
        super();

        Label count = new Label();
        count.setPrefWidth(30);
        count.setAlignment(Pos.CENTER_RIGHT);

        Label fire = new Label(FIRE_CHAR);
        fire.getStyleClass().add("streaks-fire");

        count.textProperty().bind(_streaks.asString());
        visibleProperty().bind(_streaks.greaterThanOrEqualTo(3));

        setSpacing(10);

        getChildren().setAll(count, fire);
    }

    public void bindStreaks(IntegerProperty streaksProperty) {
        _streaks.bind(streaksProperty);
    }

}
