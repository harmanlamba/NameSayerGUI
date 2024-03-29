package namesayer.controller.components.listview;

import javafx.collections.ObservableList;
import namesayer.model.Recording;

/**
 * Each object of the CreationListInnerCell represents a recording for the creation in the CreationListCell
 */
public class CreationsListInnerCell extends CreationsListCell<Recording> {

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
