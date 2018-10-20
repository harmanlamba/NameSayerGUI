package namesayer.controller.components.listview;

import com.jfoenix.controls.JFXListView;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.Observable;

import java.util.List;
import java.util.ArrayList;

import namesayer.model.Recording;
import namesayer.model.CreationsList;
import namesayer.model.CreationsListEntry;


/**
 * Custom Component that displays the recordings in the main screen. This class is in charge of holding other custom
 * components and displaying the corresponding information in the "listView" for the user.
 */
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

        // Deselect recordings not in the list, as that confuses the user.
        wasSelected.removeIf(recording -> !_creationsList.hasRecording(recording));

        // Updating our listview deselects some recordings. Reselect them even when they are
        // no longer visible on the list through the current list filter.
        _selectedRecordings.setAll(wasSelected);
    }

}
