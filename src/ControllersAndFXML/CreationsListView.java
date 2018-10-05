package ControllersAndFXML;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXButton;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Accordion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.InvalidationListener;

import java.util.List;
import java.util.ArrayList;

import NameSayer.backend.Recording;
import NameSayer.backend.Creation;
import NameSayer.backend.CreationsList;
import NameSayer.backend.CreationsListEntry;

// TODO: Refactor out the private inner classes into a new package as individual files.

public class CreationsListView extends JFXListView<CreationsListEntry> {

    private ObservableList<Recording> _selectedRecordings = FXCollections.observableArrayList();
    private CreationsList _creationsList;

    public CreationsListView() {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setCellFactory(listView -> new CreationsListOuterCell(_selectedRecordings));

        // TODO: change this label when filter is enabled, so it says "Begin by entering a name above".
        setPlaceholder(new Label("No recordings found.\nAdd them to the ./data/database folder."));
    }

    public void setCreationsList(CreationsList creationsList) {
        _creationsList = creationsList;
        _creationsList.addListener((Observable o) -> refreshList());
        refreshList();
    }

    /**
     * Note: this is automatically synced with the current selection.
     */
    public ObservableList<Recording> getSelectedRecordings() {
        return _selectedRecordings;
    }

    public void selectNext() {

        // Pick very last recording selected in the list.
        int indexToSelect = -1;
        for (Recording recording : _selectedRecordings) {
            int candidateIndex = _creationsList.indexOfCreation(recording.getCreation());
            if (candidateIndex > indexToSelect) {
                indexToSelect = candidateIndex;
            }
        }

        // Advance.
        indexToSelect++;

        // Positive modulo to wrap around.
        indexToSelect += _creationsList.size();
        indexToSelect %= _creationsList.size();

        CreationsListEntry entryToSelect = _creationsList.get(indexToSelect);
        Recording recordingToSelect = entryToSelect.getRepresentativeRecording();
        _selectedRecordings.setAll(recordingToSelect);
        scrollTo(entryToSelect);
    }

    public void selectPrevious() {

        // Pick very first recording selected in the list.
        int indexToSelect = _creationsList.size();
        for (Recording recording : _selectedRecordings) {
            int candidateIndex = _creationsList.indexOfCreation(recording.getCreation());
            if (candidateIndex < indexToSelect) {
                indexToSelect = candidateIndex;
            }
        }

        // Advance backwards.
        indexToSelect--;

        // Positive modulo to wrap around.
        indexToSelect += _creationsList.size();
        indexToSelect %= _creationsList.size();

        CreationsListEntry entryToSelect = _creationsList.get(indexToSelect);
        Recording recordingToSelect = entryToSelect.getRepresentativeRecording();
        _selectedRecordings.setAll(recordingToSelect);
        scrollTo(entryToSelect);
    }

    private void refreshList() {
        List<Recording> wasSelected = new ArrayList<>(_selectedRecordings);
        getItems().setAll(_creationsList);

        // Updating our listview deselects some recordings. Reselect them even when they are
        // no longer visible on the list through the current list filter.
        _selectedRecordings.setAll(wasSelected);
    }

    /**
     * Displayed entry for a recording, or a creation with a single recording.
     */
    private class SingleCellContents extends HBox {

        private JFXCheckBox _checkBox = new JFXCheckBox();
        private Label _labelNumber = new Label();
        private Label _labelName = new Label();
        private Label _labelDate = new Label();
        private QualityStars _qualityStars = new QualityStars();
        private JFXButton _btnDelete = new JFXButton() {
            public void fire() {
                _recording.delete();
            }
        };

        private Recording _recording;
        private JFXListCell<?> _cell;
        private ObservableList<Recording> _selectedRecordings;

        public SingleCellContents(Recording recording, JFXListCell<?> cell, ObservableList<Recording> selectedRecordings) {
            super();
            _recording = recording;
            _cell = cell;
            _selectedRecordings = selectedRecordings;
            setSelected(_selectedRecordings.contains(recording));

            _labelName.setText(recording.getCreation().getName());
            _labelDate.setText(recording.getDateString());
            _qualityStars.setRecording(recording);

            BooleanProperty isHovered = new SimpleBooleanProperty(false);
            setOnMouseEntered(event -> isHovered.setValue(true));
            setOnMouseExited(event -> isHovered.setValue(false));

            _labelNumber.visibleProperty().bind(_checkBox.selectedProperty());
            _selectedRecordings.addListener((InvalidationListener)(o -> updateFromSelectedRecordings()));

            _checkBox.visibleProperty().bind(isHovered.or(_checkBox.selectedProperty()));
            _checkBox.selectedProperty().addListener(o -> {
                setSelected(_checkBox.isSelected());
            });
            _checkBox.focusedProperty().addListener(o -> {
                // Do not allow focus onto checkbox as this breaks JavaFX's focus cleanup routine
                // when the nested list view goes off-screen.
                _cell.getListView().requestFocus();
            });

            _btnDelete.setText("\uf252");
            _btnDelete.getStyleClass().add("delete-btn");
            _btnDelete.setVisible(recording.getType() == Recording.Type.ATTEMPT);

            _labelNumber.setPrefWidth(16);
            Region spaceBetweenNumberName = new Region();
            spaceBetweenNumberName.setMinWidth(20);
            _labelName.setMaxWidth(Double.POSITIVE_INFINITY);
            _labelName.setPrefWidth(0);
            HBox.setHgrow(_labelName, Priority.ALWAYS);
            _labelDate.setMaxWidth(Double.POSITIVE_INFINITY);
            _labelDate.setPrefWidth(0);
            HBox.setHgrow(_labelDate, Priority.ALWAYS);
            Region spaceBetweenStarsDelete = new Region();
            spaceBetweenStarsDelete.setMinWidth(20);

            getChildren().setAll(
                    _checkBox,
                    _labelNumber,
                    spaceBetweenNumberName,
                    _labelName,
                    _labelDate,
                    _qualityStars,
                    spaceBetweenStarsDelete,
                    _btnDelete);
        }

        private void updateFromSelectedRecordings() {
            setSelected(_selectedRecordings.contains(_recording));
        }

        public Recording getRecording() {
            return _recording;
        }

        public void setSelected(boolean value) {
            if (!isStillValid()) {
                return;
            }
            SelectionModel<?> selectionModel = _cell.getListView().getSelectionModel();
            _checkBox.setSelected(value);
            if (value) {
                selectionModel.select(_cell.getIndex());
                if (!_selectedRecordings.contains(_recording)) {
                    _selectedRecordings.add(_recording);
                }
                int selectionNumber = _selectedRecordings.indexOf(_recording) + 1;
                _labelNumber.setText(selectionNumber + "");
            } else {
                selectionModel.clearSelection(_cell.getIndex());
                if (_selectedRecordings.contains(_recording)) {
                    _selectedRecordings.remove(_recording);
                }
            }
        }

        private boolean isStillValid() {
            Object item = _cell.getItem();
            if (item == null) {
                return false;
            } else if (item instanceof Recording) {
                return item == _recording;
            } else if (item instanceof CreationsListEntry) {
                CreationsListEntry entry = (CreationsListEntry)item;
                return entry.getRepresentativeRecording() == _recording;
            } else {
                assert false;
                return false;
            }
        }

    }

    /**
     * Displayed entry for creations with multiple recordings.
     */
    private class MultiCellContents extends VBox {

        public MultiCellContents(CreationsListEntry entry, ObservableList<Recording> selectedRecordings) {
            super();

            // TODO: checkboxes? Selection handler?
            HBox heading = new HBox(new Label(entry.toString()));

            Accordion accordion = new Accordion();

            for (String name : entry.getNames()) {

                JFXListView<Recording> innerListView = new JFXListView<>();
                innerListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                innerListView.setCellFactory(listView -> new CreationsListInnerCell(selectedRecordings));

                Creation creation = entry.getCreation(name);
                if (creation != null) {
                    innerListView.getItems().setAll(creation.getAllRecordings());
                }

                TitledPane pane = new TitledPane(name, innerListView);
                accordion.getPanes().add(pane);

            }

            getChildren().setAll(heading, accordion);
        }

    }

    private abstract class CreationsListCell<T> extends JFXListCell<T> {

        private SingleCellContents _currentSingleCellContents = null;
        private Recording _recording = null;
        private InvalidationListener cellSelectedListener = o -> {
            // Do not delesct when the cell goes off screen, or when the item is
            // transitioning to a different recording.
            if (_currentSingleCellContents == null) return;
            if (!isVisible()) return;
            if (_recording != _currentSingleCellContents.getRecording()) return;

            _currentSingleCellContents.setSelected(isSelected());
        };

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);

            // Only have one registered at any time.
            // Calling removeListener is no-op during the first time when nothing is registered.
            selectedProperty().removeListener(cellSelectedListener);
            selectedProperty().addListener(cellSelectedListener);
        }

        protected void setCellContents(Node node) {
            if (node instanceof SingleCellContents) {
                _currentSingleCellContents = (SingleCellContents)node;
                _recording = _currentSingleCellContents.getRecording();
            } else {
                _currentSingleCellContents = null;
                _recording = null;
            }
            setGraphic(node);
        }

    }

    private class CreationsListInnerCell extends CreationsListCell<Recording> {

        private ObservableList<Recording> _selectedRecordings;

        public CreationsListInnerCell(ObservableList<Recording> selectedRecordings) {
            _selectedRecordings = selectedRecordings;
        }

        @Override
        public void updateItem(Recording recording, boolean empty) {
            super.updateItem(recording, empty);

            if (empty) {
                setCellContents(null);
            } else {
                assert recording != null;
                setCellContents(new SingleCellContents(recording, this, _selectedRecordings));
            }
        }

    }

    private class CreationsListOuterCell extends CreationsListCell<CreationsListEntry> {

        private ObservableList<Recording> _selectedRecordings;

        public CreationsListOuterCell(ObservableList<Recording> selectedRecordings) {
            _selectedRecordings = selectedRecordings;
        }

        @Override
        public void updateItem(CreationsListEntry entry, boolean empty) {
            super.updateItem(entry, empty);

            if (empty) {
                setCellContents(null);
            } else {
                assert entry != null;

                if (entry.isSingleRecording()) {
                    Recording recording = entry.getRepresentativeRecording();
                    setCellContents(new SingleCellContents(recording, this, _selectedRecordings));
                } else {
                    setCellContents(new MultiCellContents(entry, _selectedRecordings));
                }
            }
        }
    }

}
