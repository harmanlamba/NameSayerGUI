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

    public MultiCellContents(CreationsListEntry entry, JFXListCell<CreationsListEntry> cell, ObservableList<Recording> selectedRecordings) {
        super();
        _entry = entry;
        _cell = cell;
        _selectedRecordings = selectedRecordings;
        _selectedRecordings.addListener((InvalidationListener) (o -> updateFromSelectedRecordings()));
        updateFromSelectedRecordings();

        entry.addListener(o -> updateDisabled());
        updateDisabled();

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
        HBox heading = new HBox(_checkBox, checkboxSpacer, names, streaks);

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
        setSelected(_entry.matchesRecordings(_selectedRecordings));
    }

    public void setSelected(boolean value) {
        if (!isStillValid()) {
            return;
        }

        SelectionModel<CreationsListEntry> selectionModel = _cell.getListView().getSelectionModel();
        _checkBox.setSelected(value);
        if (value) {
            selectionModel.select(_cell.getIndex());
            if (!_entry.matchesRecordings(_selectedRecordings)) {
                _selectedRecordings.setAll(_entry.getRecordings());
            }
        } else {
            selectionModel.clearSelection(_cell.getIndex());
            if (_entry.matchesRecordings(_selectedRecordings)) {
                _selectedRecordings.clear();
            }
        }
    }

    public Object getItem() {
        return _entry;
    }

    private void updateDisabled() {
        if (!isStillValid()) {
            return;
        }
        _cell.setDisable(_entry.getRecordings().isEmpty());
    }

    private boolean isStillValid() {
        return _entry == _cell.getItem();
    }

}
