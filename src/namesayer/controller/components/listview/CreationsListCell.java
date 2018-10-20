package namesayer.controller.components.listview;

import com.jfoenix.controls.JFXListCell;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;

/**
 * Handles the selection behaviour for the Recordings that the class represents.
 */
public abstract class CreationsListCell<T> extends JFXListCell<T> {

    private CellContents _currentCellContents = null;
    private Object _item = null;
    private InvalidationListener cellSelectedListener = o -> {
        // Do not deselect when the cell goes off screen, or when the item is
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

    /**
     * Use this instead of setGraphic directly, so selection handling is configured for
     * the new cell contents.
     */
    protected void setCellContents(Node node) {
        if (node != null) {
            if (_currentCellContents != null) {
                _currentCellContents.detachListeners();
            }
            _currentCellContents = (CellContents) node;
            _item = _currentCellContents.getItem();
        }
        setGraphic(node);
    }

}
