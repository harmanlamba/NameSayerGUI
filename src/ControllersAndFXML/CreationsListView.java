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

public class CreationsListView extends JFXListView<Creation> {

    private ObservableList<Recording> _selectedRecordings = FXCollections.observableArrayList();
    private ObservableList<Creation> _creationsList;

    public CreationsListView() {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setCellFactory(listView -> new CreationsListOuterCell(_selectedRecordings));
        setPlaceholder(new Label("No recordings found.\nAdd them to the ./data/database folder."));
    }

    public void setCreationsList(ObservableList<Creation> creationsList) {
        _creationsList = creationsList;
        refreshList();

        InvalidationListener creationListener = observer -> {
            refreshList();

            // Prune selected recordings.
            for (Recording recording : _selectedRecordings) {
                if (!recording.getCreation().has(recording)) {
                    _selectedRecordings.remove(recording);
                }
            }
        };

        _creationsList.addListener((Observable observer2) -> {
            refreshList();

            // Linear pass through is okay here
            for (Creation creation : _creationsList) {
                creation.removeListener(creationListener);
                creation.addListener(creationListener);
            }
        });
    }

    public ObservableList<Recording> getSelectedRecordings() {
        return _selectedRecordings;
    }

    public void selectNext() {
        System.out.println("Selecting Next");
    }

    public void selectPrevious() {
        System.out.println("Selecting Previous");
    }

    private void refreshList() {
        List<Recording> wasSelected = new ArrayList<>(_selectedRecordings);
        getItems().setAll(_creationsList);
        _selectedRecordings.setAll(wasSelected);
    }

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
            _qualityStars.setRecording(recording);

            BooleanProperty isHovered = new SimpleBooleanProperty(false);
            setOnMouseEntered(event -> isHovered.setValue(true));
            setOnMouseExited(event -> isHovered.setValue(false));

            _labelNumber.visibleProperty().bind(_checkBox.selectedProperty());
            _selectedRecordings.addListener((InvalidationListener)(o -> updateNumber()));

            _checkBox.visibleProperty().bind(isHovered.or(_checkBox.selectedProperty()));
            _checkBox.selectedProperty().addListener(o -> {
                setSelected(_checkBox.isSelected());
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

        private void updateNumber() {
            int selectionNumber = _selectedRecordings.indexOf(_recording) + 1;
            _labelNumber.setText(selectionNumber + "");
        }

        public Recording getRecording() {
            return _recording;
        }

        public void setSelected(boolean value) {
            if (_cell.getItem() != _recording && _cell.getItem() != _recording.getCreation()) {
                return;
            }
            SelectionModel<?> selectionModel = _cell.getListView().getSelectionModel();
            _checkBox.setSelected(value);
            if (value) {
                selectionModel.select(_cell.getIndex());
                if (!_selectedRecordings.contains(_recording)) {
                    _selectedRecordings.add(_recording);
                }
                updateNumber();
            } else {
                selectionModel.clearSelection(_cell.getIndex());
                if (_selectedRecordings.contains(_recording)) {
                    _selectedRecordings.remove(_recording);
                }
            }
        }

    }

    private class MultiCellContents extends VBox {

        private Label _labelName = new Label();
        private Label _labelCount = new Label();
        private JFXListView<Recording> _listView = new JFXListView<>();

        public MultiCellContents(Creation creation, ObservableList<Recording> selectedRecordings) {
            super();
            _labelName.setText(creation.getName());
            _labelCount.setText(creation.getRecordingCount() + " recordings");

            _listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            _listView.setCellFactory(listView -> new CreationsListInnerCell(selectedRecordings));

            _listView.getItems().setAll(creation.getAllRecordings());

            Region spaceBetweenNameCount = new Region();
            HBox.setHgrow(spaceBetweenNameCount, Priority.ALWAYS);
            HBox heading = new HBox(_labelName, spaceBetweenNameCount, _labelCount);
            getChildren().setAll(heading, _listView);
        }

    }

    private abstract class CreationsListCell<T> extends JFXListCell<T> {

        private SingleCellContents _currentSingleCellContents = null;
        private Recording _recording = null;
        private InvalidationListener cellSelectedListener = o -> {
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

    private class CreationsListOuterCell extends CreationsListCell<Creation> {

        private ObservableList<Recording> _selectedRecordings;

        public CreationsListOuterCell(ObservableList<Recording> selectedRecordings) {
            _selectedRecordings = selectedRecordings;
        }

        @Override
        public void updateItem(Creation creation, boolean empty) {
            super.updateItem(creation, empty);

            if (empty) {
                setCellContents(null);
            } else {
                assert creation != null;

                List<Recording> versions = creation.getVersions();
                List<Recording> attempts = creation.getAttempts();

                if (creation.getRecordingCount() > 1) {
                    setCellContents(new MultiCellContents(creation, _selectedRecordings));
                } else {
                    Recording recording;
                    if (versions.size() > 0) {
                        recording = versions.get(0);
                    } else if (attempts.size() > 0) {
                        recording = attempts.get(0);
                    } else {
                        // BUG: this should not be possible.
                        // To reproduce: record in the practice tool.
                        setCellContents(null);
                        return;
                    }
                    setCellContents(new SingleCellContents(recording, this, _selectedRecordings));
                }
            }
        }
    }

}
