package namesayer.controller.components;

import namesayer.model.CreationStore;
import namesayer.model.CreationsListEntry;

import com.jfoenix.controls.JFXChipView;


import com.jfoenix.controls.JFXDefaultChip;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Custom component inheriting from the JFXChipView
public class TagInput extends JFXChipView<List<String>> {

    private final ObjectProperty<CreationStore> _creationStore = new SimpleObjectProperty<CreationStore>();

    private StringProperty _currentText = new SimpleStringProperty();
    private TextArea _textArea = null;

    public TagInput() {

        //Utility to convert strings into the 'Tags'/'Chips'
        setConverter(new StringConverter<List<String>>() {
            @Override
            public String toString(List<String> names) {
                return String.join(" ", names);
            }

            @Override
            public List<String> fromString(String string) {
                return CreationsListEntry.parseNamesIntoList(string);
            }
        });

        //Tells the chipView which creations to consider as suggestions, (Filters the suggestions)
        setPredicate((item, text) -> shouldSuggest(item, text));

        setSelectionHandler(autoCompletedBit -> autoCompleteSelectionHandler(autoCompletedBit));

        //Defining displaying the suggestions will be represented in the cells
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

        //Listener for real time suggestions update
        InvalidationListener suggestionsUpdater = o -> {
            getSuggestions().clear();
            List<String> names = _creationStore.getValue().getCreationNames();
            Collections.sort(names);
            for (String name : names) {
                if (!_creationStore.getValue().get(name).getVersions().isEmpty()) {
                    getSuggestions().add(CreationsListEntry.parseNamesIntoList(name));
                }
            }
        };

        _creationStore.addListener(o -> {
            suggestionsUpdater.invalidated(null);
            _creationStore.getValue().addListener(suggestionsUpdater);
        });

        //Making it so that if a creation is not present it is underlined in red
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

        skinProperty().addListener(o -> updateTextArea());
        InvalidationListener promptUpdater = o -> updatePromptText();
        getChips().addListener(promptUpdater);
        _currentText.addListener(promptUpdater);
        skinProperty().addListener(promptUpdater);

    }

    private void updateTextArea() {
        // Unfortunately, JFXChipView does not expose the text area to us.
        // The following is a ghetto work around to access the text area.
        Pane pane = (Pane)((SkinBase)getSkin()).getChildren().get(0);
        _textArea = (TextArea)pane.getChildren().get(0);
        _textArea.focusedProperty().addListener(o -> updatePromptText());
        _currentText.bind(_textArea.textProperty());
    }

    private void updatePromptText() {
        if (_textArea == null) {
            return;
        }

        boolean promptVisible =
            !_textArea.isFocused() &&
            getChips().isEmpty() &&
            _currentText.isEmpty().get();

        if (promptVisible) {
            _textArea.setPromptText("Enter some names here, and press enter...");
        } else {
            _textArea.setPromptText(null);
        }
    }

    public void setCreationStore(CreationStore creationStore) {
        _creationStore.setValue(creationStore);
    }

    private boolean shouldSuggest(List<String> suggestion, String text) {
        List<String> nameBits = CreationsListEntry.parseNamesIntoList(text);
        if (nameBits.isEmpty()) {
            return true;
        }

        String lastBit = nameBits.get(nameBits.size() - 1);
        return String.join(" ", suggestion).startsWith(lastBit);
    }

    private List<String> autoCompleteSelectionHandler(List<String> autoCompletedBit) {
        List<String> bits = CreationsListEntry.parseNamesIntoList(_currentText.getValue());

        // Can't work on abstract list directly, so clone it.
        bits = new ArrayList<>(bits);

        // Replace last bit with the autocompleted bit.
        if (!bits.isEmpty()) {
            bits.remove(bits.size() - 1);
        }
        bits.addAll(autoCompletedBit);

        return bits;
    }

}
