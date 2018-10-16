package namesayer.controller.components.creationslistview;

public class CreationsListOuterCell extends CreationsListCell<CreationsListEntry> {

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
