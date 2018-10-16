package namesayer.controller.components.listview;

public abstract class CreationsListCell<T> extends JFXListCell<T> {

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
            _currentCellContents = (CellContents) node;
            _item = _currentCellContents.getItem();
        }
        setGraphic(node);
    }

}
