package namesayer.controller.components.listview;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.ArrayList;

import namesayer.controller.components.Streaks;
import namesayer.model.Creation;
import namesayer.model.CreationsListEntry;
import namesayer.model.Recording;

/**
 * Displayed entry for creations with multiple recordings.
 */
public class MultiCellContents extends VBox implements CellContents {

    private JFXCheckBox _checkBox = new JFXCheckBox();
    private CreationsListEntry _entry;
    private JFXListCell<CreationsListEntry> _cell;
    private VBox _innerLists = new VBox();
    private ObservableList<Recording> _selectedRecordings;
    private Label _foundCounter = new Label();

    public MultiCellContents(CreationsListEntry entry, JFXListCell<CreationsListEntry> cell, ObservableList<Recording> selectedRecordings) {
        super();
        _entry = entry;
        _cell = cell;
        _selectedRecordings = selectedRecordings;
        _selectedRecordings.addListener((InvalidationListener) (o -> updateFromSelectedRecordings()));
        updateFromSelectedRecordings();

        entry.addListener(o -> updateFromEntry());
        updateFromEntry();

        BooleanProperty isHovered = new SimpleBooleanProperty(false);
        setOnMouseEntered(event -> isHovered.setValue(true));
        setOnMouseExited(event -> isHovered.setValue(false));

        _checkBox.visibleProperty().bind(isHovered.or(_checkBox.selectedProperty()));
        _checkBox.selectedProperty().addListener(o -> {
            setSelected(_checkBox.isSelected());
        });
        _checkBox.focusedProperty().addListener(o -> {
            // Do not allow focus onto checkbox
            _cell.getListView().requestFocus();
        });

        Region checkboxSpacer = new Region();
        checkboxSpacer.setPrefWidth(36);

        HBox names = new HBox();
        names.setSpacing(8);
        names.setMaxWidth(Double.POSITIVE_INFINITY);
        HBox.setHgrow(names, Priority.ALWAYS);

        Streaks streaks = new Streaks();
        streaks.bindStreaks(entry.streaksProperty());
        HBox heading = new HBox(_checkBox, checkboxSpacer, names, _foundCounter, streaks);

        Creation overallAttempts = entry.getOverallAttemptsCreation();
        if (overallAttempts != null) {
            addInnerList("Your Attempts", overallAttempts);
        }

        for (String name : entry.getNames()) {
            Label nameLabel = new Label(name);
            names.getChildren().add(nameLabel);
            if (entry.getCreation(name) == null) {
                nameLabel.getStyleClass().add("invalid-name");
            }

            Creation creation = entry.getCreation(name);
            if (creation != null) {
                addInnerList(name, creation);
            }
        }

        getChildren().setAll(heading, _innerLists);
    }

    private void addInnerList(String title, Creation creation) {
        JFXListView<Recording> innerListView = new JFXListView<>();
        innerListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        innerListView.setCellFactory(listView -> new CreationsListInnerCell(_selectedRecordings));

        for (Recording recording : creation.getAllRecordings()) {
            innerListView.getItems().add(recording);
        }

        TitledPane pane = new TitledPane(title, innerListView);
        pane.setExpanded(false);
        _innerLists.getChildren().add(pane);
    }

    private void updateFromSelectedRecordings() {
        setSelected(_entry.includedInRecordings(_selectedRecordings));
    }

    public void setSelected(boolean value) {
        if (!isStillValid()) {
            return;
        }

        SelectionModel<CreationsListEntry> selectionModel = _cell.getListView().getSelectionModel();
        _checkBox.setSelected(value);
        if (value) {
            selectionModel.select(_cell.getIndex());
            if (!_entry.includedInRecordings(_selectedRecordings)) {
                _selectedRecordings.addAll(_entry.getRecordings());
            }
        } else {
            selectionModel.clearSelection(_cell.getIndex());

            int selectionIdx = _entry.findInRecordings(_selectedRecordings);
            if (selectionIdx != -1) {
                // Selection contains this CreationsListEntry. Remove the found subsequence by
                // creating the selection before and after it, and merging it altogether.

                // The list before the subsequence.
                List<Recording> newSelection = new ArrayList<>(_selectedRecordings.subList(0, selectionIdx));

                // Position after the subsequence.
                selectionIdx += _entry.getRecordings().size();

                // Merge in the list after the subsequence.
                newSelection.addAll(_selectedRecordings.subList(selectionIdx, _selectedRecordings.size()));

                // Update the selection.
                _selectedRecordings.setAll(newSelection);
            }
        }
    }

    public Object getItem() {
        return _entry;
    }

    private void updateFromEntry() {
        if (!isStillValid()) {
            return;
        }

        List<Recording> recordingsAvailable = _entry.getRecordings();
        _cell.setDisable(recordingsAvailable.isEmpty());

        int availableCount = recordingsAvailable.size();
        int requestedCount = _entry.getNames().size();
        if (availableCount != requestedCount) {
            _foundCounter.setText(recordingsAvailable.size() + " / " + requestedCount +  " available");
        } else {
            _foundCounter.setText("");
        }
    }

    private boolean isStillValid() {
        return _entry == _cell.getItem();
    }

}
