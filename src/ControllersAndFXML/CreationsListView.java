package ControllersAndFXML;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXButton;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.InvalidationListener;

import java.util.List;

import NameSayer.backend.Recording;
import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;

public class CreationsListView extends JFXListView<Creation> {

    private ObservableList<Recording> _selectedRecordings = FXCollections.observableArrayList();
    private ObjectProperty<CreationStore> _creationStore = new SimpleObjectProperty<CreationStore>();

    public CreationsListView() {

        InvalidationListener creationListener = observer -> {
            // Prune selected recordings.
            for (Recording recording : _selectedRecordings) {
                if (!recording.getCreation().has(recording)) {
                    _selectedRecordings.remove(recording);
                }
            }
        };

        _creationStore.addListener(observer1 -> {
            getItems().setAll(_creationStore.getValue().getCreations());

            _creationStore.getValue().addListener(observer2 -> {
                // TODO: search filtered, preserve selections.
                getItems().setAll(_creationStore.getValue().getCreations());

                // Linear pass through is okay here
                for (Creation creation : _creationStore.getValue().getCreations()) {
                    creation.removeListener(creationListener);
                    creation.addListener(creationListener);
                }
            });
        });

        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setCellFactory(listView -> new CreationsListOuterCell(_selectedRecordings));
    }

    public void setCreationStore(CreationStore store) {
        _creationStore.setValue(store);
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

    private class SingleCellContents extends HBox {

        private JFXCheckBox _checkBox = new JFXCheckBox();
        private Label _labelNumber = new Label();
        private Label _labelName = new Label();
        private Label _labelDate = new Label();
        private QualityStars _qualityStars = new QualityStars();
        private JFXButton _btnDelete = new JFXButton();

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
            _labelDate.setText(recording.getDate().toString());
            _qualityStars.setRecording(recording);
            _btnDelete.setText("DEL");
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

            getChildren().setAll(_checkBox, _labelNumber, _labelName,
                    _labelDate, _qualityStars, _btnDelete);
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

        private Label _labelArrow = new Label();
        private Label _labelName = new Label();
        private Label _labelCount = new Label();
        private JFXListView<Recording> _listView = new JFXListView<>();

        public MultiCellContents(Creation creation, ObservableList<Recording> selectedRecordings) {
            super();
            _labelArrow.setText("EXPAND");
            _labelName.setText(creation.getName());
            _labelCount.setText("Versions: " + creation.getRecordingCount());

            _listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            _listView.setCellFactory(listView -> new CreationsListInnerCell(selectedRecordings));

            _listView.getItems().setAll(creation.getVersions()); // TODO

            getChildren().setAll(new HBox(_labelArrow, _labelName, _labelCount), _listView);
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
                    } else {
                        recording = attempts.get(0);
                    }
                    setCellContents(new SingleCellContents(recording, this, _selectedRecordings));
                }
            }
        }
    }

}
