package ControllersAndFXML;

import NameSayer.backend.CreationStore;

import com.jfoenix.controls.JFXChip;
import com.jfoenix.controls.JFXChipView;


import com.jfoenix.controls.JFXDefaultChip;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;


import java.util.*;
import java.util.function.BiFunction;

public class TagInput extends JFXChipView<List<String>> {


    private final ObjectProperty<CreationStore> _creationStore = new SimpleObjectProperty<CreationStore>();


    public TagInput() {
        setConverter(new StringConverter<List<String>>() {
            @Override
            public String toString(List<String> names) {
                return String.join(" ", names);
            }

            @Override
            public List<String> fromString(String string) {
                return Arrays.asList(string.toLowerCase().split("[ -]+")); // TODO filter out bad values and other whitespace characters
            }
        });
        setPredicate((item, text) -> {
            return String.join(" ", item).startsWith(text);
        });
        setSuggestionsCellFactory(listview -> {
            return new ListCell<List<String>>() {
                @Override
                protected void updateItem(List<String> item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(null);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(String.join(" ", item));
                    }
                }
            };
        });
        InvalidationListener suggestionsUpdater = o -> {
            getSuggestions().clear();
            List<String> names = _creationStore.getValue().getCreationNames();
            Collections.sort(names);
            for (String name : names) {
                getSuggestions().add(Collections.singletonList(name));
            }
        };
        _creationStore.addListener(o -> {
            suggestionsUpdater.invalidated(null);
            _creationStore.getValue().addListener(suggestionsUpdater);
        });
        setChipFactory((chipView, chip) -> {
            return new JFXDefaultChip<List<String>>(chipView, chip) {{
                List<String> listOfNames = getItem();
                HBox labelAndCross = new HBox();
                labelAndCross.setSpacing(8);
                root.getChildren().set(0, labelAndCross);
                for (String name : listOfNames) {
                    Label label = new Label(name);
                    if (_creationStore.get().get(name) == null) {
                        //Creation Name does not exist
                        label.getStyleClass().add("invalid-name");
                    }
                    labelAndCross.getChildren().add(label);
                }
            }};
        });
    }

    public void setCreationStore(CreationStore creationStore) {
        _creationStore.setValue(creationStore);
    }


}
