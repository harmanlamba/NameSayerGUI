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
        selectIndex(_creationsList.lastIndexInSelection(_selectedRecordings) + 1);
    }

    public void selectPrevious() {
        selectIndex(_creationsList.firstIndexInSelection(_selectedRecordings) - 1);
    }

    private void selectIndex(int index) {
        assert index >= -1;
        index += _creationsList.size();
        index %= _creationsList.size();

        _selectedRecordings.setAll(_creationsList.get(index).getRecordings());
        scrollTo(index);
    }

    private void refreshList() {
        List<Recording> wasSelected = new ArrayList<>(_selectedRecordings);
        getItems().setAll(_creationsList);

        // Updating our listview deselects some recordings. Reselect them even when they are
        // no longer visible on the list through the current list filter.
        _selectedRecordings.setAll(wasSelected);
    }

    private interface CellContents {
        public void setSelected(boolean value);
        public Object getItem();
    }

    /**
     * Displayed entry for a recording, or a creation with a single recording.
     */
    private class SingleCellContents extends HBox implements CellContents {

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
            _selectedRecordings.addListener((InvalidationListener)(o -> updateFromSelectedRecordings()));
            updateFromSelectedRecordings();

            _labelName.setText(recording.getCreation().getName());
            _labelDate.setText(recording.getDateString());
            _qualityStars.setRecording(recording);

            BooleanProperty isHovered = new SimpleBooleanProperty(false);
            setOnMouseEntered(event -> isHovered.setValue(true));
            setOnMouseExited(event -> isHovered.setValue(false));

            _labelNumber.visibleProperty().bind(_checkBox.selectedProperty());

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

        public Object getItem() {
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
                List<Recording> recordings = entry.getRecordings();
                return recordings.size() == 1 && recordings.get(0) == _recording;
            } else {
                assert false;
                return false;
            }
        }

    }

    /**
     * Displayed entry for creations with multiple recordings.
     */
    private class MultiCellContents extends VBox implements CellContents {

        private JFXCheckBox _checkBox = new JFXCheckBox();
        private CreationsListEntry _entry;
        private JFXListCell<CreationsListEntry> _cell;
        private ObservableList<Recording> _selectedRecordings;

        public MultiCellContents(CreationsListEntry entry, JFXListCell<CreationsListEntry> cell, ObservableList<Recording> selectedRecordings) {
            super();
            _entry = entry;
            _cell = cell;
            _selectedRecordings = selectedRecordings;
            _selectedRecordings.addListener((InvalidationListener)(o -> updateFromSelectedRecordings()));
            updateFromSelectedRecordings();

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

            HBox heading = new HBox(_checkBox, new Label(entry.toString()));

            Accordion accordion = new Accordion();

            for (String name : entry.getNames()) {

                JFXListView<Recording> innerListView = new JFXListView<>();
                innerListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                innerListView.setCellFactory(listView -> new CreationsListInnerCell(selectedRecordings));

                Creation creation = entry.getCreation(name);
                if (creation != null) {
                    for (Recording recording : creation.getAllRecordings()) {
                        innerListView.getItems().add(recording);
                    }
                }

                TitledPane pane = new TitledPane(name, innerListView);
                accordion.getPanes().add(pane);

            }

            getChildren().setAll(heading, accordion);
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

        private boolean isStillValid() {
            return _entry == _cell.getItem();
        }

    }

    private abstract class CreationsListCell<T> extends JFXListCell<T> {

        private CellContents _currentCellContents = null;
        private Object _item = null;
        private InvalidationListener cellSelectedListener = o -> {
            // Do not delesct when the cell goes off screen, or when the item is
            // transitioning to a different recording.
            if (_currentCellContents == null) return;
            if (!isVisible()) return;
            if (_item != _currentCellContents.getItem()) return;

            _currentCellContents.setSelected(isSelected());
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
            if (node != null) {
                _currentCellContents = (CellContents)node;
                _item = _currentCellContents.getItem();
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
        public void updateItem(Recording item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setCellContents(null);
            } else {
                assert item != null;
                setCellContents(new SingleCellContents(item, this, _selectedRecordings));
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
                    Recording recording = entry.getRecordings().get(0);
                    setCellContents(new SingleCellContents(recording, this, _selectedRecordings));
                } else {
                    setCellContents(new MultiCellContents(entry, this, _selectedRecordings));
                }
            }
        }
    }

}
