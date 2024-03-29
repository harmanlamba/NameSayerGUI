package namesayer.controller.components;

import namesayer.model.CreationStore;
import namesayer.model.CreationsListEntry;

import org.controlsfx.control.PopOver;
import com.jfoenix.controls.JFXChipView;
import com.jfoenix.controls.JFXDefaultChip;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Where the user types in what names they wish to practice. Comes with autocomplete, and a
 * visualisation of what names have been entered allowing the user to remove their entered names.
 */
public class TagInput extends JFXChipView<List<String>> {

    private final ObjectProperty<CreationStore> _creationStore = new SimpleObjectProperty<CreationStore>();

    private StringProperty _currentText = new SimpleStringProperty();
    private TextArea _textArea = null;

    public TagInput() {

        getStyleClass().add("tag-input");

        // Utility to convert strings into the 'Tags'/'Chips'.
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

        // Tells the chipView which creations to consider as suggestions, (Filters the suggestions).
        setPredicate((item, text) -> shouldSuggest(item, text));

        setSelectionHandler(autoCompletedBit -> autoCompleteSelectionHandler(autoCompletedBit));

        // Defining displaying the suggestions will be represented in the cells.
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

        // Listener for real time suggestions update.
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

        // Suggestions need to be updated when creations are added/removed to the CreationStore.
        _creationStore.addListener(o -> {
            suggestionsUpdater.invalidated(null);
            _creationStore.getValue().addListener(suggestionsUpdater);
        });

        // Making it so that if a creation is not present it is underlined in red.
        setChipFactory((chipView, chip) -> {
            return new JFXDefaultChip<List<String>>(chipView, chip) {{
                List<String> listOfNames = getItem();
                HBox labelAndCross = new HBox();
                labelAndCross.setSpacing(8);
                root.getChildren().set(0, labelAndCross);
                for (String name : listOfNames) {
                    Label label = new Label(name);
                    if (_creationStore.get().get(name) == null) {
                        // Creation Name does not exist.
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

        setOnMouseClicked(e -> {
            if (_textArea != null) {
                _textArea.requestFocus();
            }
        });

    }

    /**
     * Retrieve the TextArea associated with the JFXChipView.
     * Unfortunately, JFXChipView does not directly expose the text area to us.
     */
    private void updateTextArea() {
        Pane pane = (Pane)((SkinBase)getSkin()).getChildren().get(0);
        _textArea = (TextArea)pane.getChildren().get(pane.getChildren().size() - 1);
        _textArea.focusedProperty().addListener(o -> updatePromptText());

        // From this TextArea, we finally have acces to what's being typed in real-time.
        _currentText.bind(_textArea.textProperty());
    }

    /**
     * Only show the prompt text in certain conditions where the user expects them.
     * Call this method when such conditions change.
     */
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

    /**
     * TagInput requires a CreationStore purely for suggestions and validation purposes.
     */
    public void setCreationStore(CreationStore creationStore) {
        _creationStore.setValue(creationStore);
    }

    /**
     * Show a popover to use this input with better real estate for long lists of names.
     * @param node Node under which to show this popover.
     */
    public void expand(Node node) {
        TagInput expandedInput = new TagInput();
        expandedInput._creationStore.bind(_creationStore);
        expandedInput.getChips().setAll(getChips());
        expandedInput.getChips().addListener((Observable o) -> {
            getChips().setAll(expandedInput.getChips());
        });
        expandedInput.setMinWidth(600);

        ScrollPane container = new ScrollPane(expandedInput);
        container.setMinWidth(640);
        container.setMinHeight(400);

        PopOver popOver = new PopOver(container);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.setDetachable(false);
        popOver.show(node);
        popOver.setOnHidden(e -> setDisable(false));

        popOver.setX(popOver.getX() - 250);

        setDisable(true);
    }

    /**
     * The predicate for matching potential suggestions with the current text.
     * Match only the last name word out of the full name, so TagInput can continue suggesting
     * second and third names after the first name has been typed.
     *
     * @param suggestion Potential suggested name from the CreationsStore to match.
     * @param text Currently typed text.
     */
    private boolean shouldSuggest(List<String> suggestion, String text) {
        List<String> nameBits = CreationsListEntry.parseNamesIntoList(text);
        if (nameBits.isEmpty()) {
            return true;
        }

        String lastBit = nameBits.get(nameBits.size() - 1);
        return String.join(" ", suggestion).startsWith(lastBit);
    }

    /**
     * Apply the desired suggestion to the currently typed text.
     * Replaces the last name word out of the full name, with the auto-completed name word.
     *
     * @param autoCompletedBit The desired suggestion to apply.
     */
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
