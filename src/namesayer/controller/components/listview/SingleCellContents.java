package namesayer.controller.components.listview;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXListCell;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import namesayer.controller.components.QualityStars;
import namesayer.controller.components.Streaks;
import namesayer.model.CreationsListEntry;
import namesayer.model.Recording;

import java.util.List;

/**
 * Displayed entry for a recording, or a creation with a single recording.
 */
public class SingleCellContents extends HBox implements CellContents {

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

        // Cell could be disabled due to it being previously used for MultiCellContents.
        cell.setDisable(false);

        _selectedRecordings = selectedRecordings;
        _selectedRecordings.addListener((InvalidationListener) (o -> updateFromSelectedRecordings()));
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

        Label labelType = new Label();
        labelType.getStyleClass().add("list-cell-type");
        if (recording.getType() == Recording.Type.ATTEMPT) {
            labelType.setText("ATTEMPT");
            cell.getStyleClass().add("attempt");
            _btnDelete.setVisible(true);
        } else {
            labelType.setVisible(false);
            cell.getStyleClass().add("version");
            _btnDelete.setVisible(false);
        }

        Streaks streaks = new Streaks();
        streaks.bindStreaks(recording.getCreation().streaksProperty());

        boolean showStreaks =
            recording.getCreation().getRecordingCount() == 1 &&
            recording.getType() == Recording.Type.VERSION;

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

        getStyleClass().add("single-cell-contents");

        getChildren().setAll(
            _checkBox,
            _labelNumber,
            spaceBetweenNumberName,
            _labelName,
            labelType,
            _labelDate,
            _qualityStars,
            spaceBetweenStarsDelete,
            showStreaks ? streaks : _btnDelete);
    }

    /**
     * Call this when the selected-recordings has changed.
     * Ensures this cell's selected state is synchronised with the overall selection.
     */
    private void updateFromSelectedRecordings() {
        setSelected(_selectedRecordings.contains(_recording));
    }

    /**
     * @see CellContents#getItem()
     */
    public Object getItem() {
        return _recording;
    }

    /**
     * Call this when there is new information/intention regarding the selection state of this cell.
     * 1. Updates states of internal components to accurately represent the desired selection state.
     * 2. Updates the selectedRecordings list if it doesn't realise the intended state.
     * This can cause a few recursive calls before the state settles.
     *
     * Note: This can be called when there are stale event listeners from old cell contents, since
     * list cells are regularly recycled by the list view. It is important to verify that this
     * cell contents class is still representing what the cell is displaying.
     *
     * @param value True/false refers to whether this cell should be selected or not.
     */
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

    /**
     * @return Whether (true) this cell contents still represent the item currently displayed by the
     *         actual list cell, or whether (false) this cell contents class has gone stale.
     */
    private boolean isStillValid() {
        Object item = _cell.getItem();
        if (item == null) {
            return false;
        } else if (item instanceof Recording) {
            return item == _recording;
        } else if (item instanceof CreationsListEntry) {
            CreationsListEntry entry = (CreationsListEntry) item;
            List<Recording> recordings = entry.getRecordings();
            return recordings.size() == 1 && recordings.get(0) == _recording;
        } else {
            assert false;
            return false;
        }
    }

}
