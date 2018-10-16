package namesayer.controller.components;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.InvalidationListener;

import java.util.List;
import java.util.ArrayList;

import namesayer.model.Recording;
import namesayer.model.Creation;
import namesayer.model.CreationsList;
import namesayer.model.CreationsListEntry;

public class CreationsListView extends JFXListView<CreationsListEntry> {

    private ObservableList<Recording> _selectedRecordings = FXCollections.observableArrayList();
    private CreationsList _creationsList;

    public CreationsListView() {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setCellFactory(listView -> new CreationsListOuterCell(_selectedRecordings));

        setPlaceholder(new Label("Begin by entering a name.\nAll name tags will appear\nin the ordered typed"));
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
        int originalIndex = _creationsList.lastIndexInSelection(_selectedRecordings);

        for (int i = 1; i <= _creationsList.size(); i++) {
            if (trySelectIndex(originalIndex + i)) {
                return;
            }
        }
    }

    public void selectPrevious() {
        int originalIndex = _creationsList.firstIndexInSelection(_selectedRecordings);

        if (originalIndex == -1) {
            originalIndex = _creationsList.size();
        }

        for (int i = 1; i <= _creationsList.size(); i++) {
            if (trySelectIndex(originalIndex - i)) {
                return;
            }
        }
    }

    private boolean trySelectIndex(int index) {
        assert index >= -_creationsList.size();
        index += _creationsList.size();
        index %= _creationsList.size();

        List<Recording> recordings = _creationsList.get(index).getRecordings();
        if (recordings.isEmpty()) {
            return false;
        } else {
            _selectedRecordings.setAll(recordings);
            scrollTo(index);
            return true;
        }
    }

    private void refreshList() {
        List<Recording> wasSelected = new ArrayList<>(_selectedRecordings);
        getItems().setAll(_creationsList);

        // Updating our listview deselects some recordings. Reselect them even when they are
        // no longer visible on the list through the current list filter.
        _selectedRecordings.setAll(wasSelected);
    }

}
