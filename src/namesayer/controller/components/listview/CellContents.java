package namesayer.controller.components.listview;

public interface CellContents {
    /**
     * Manually select the cell contents when determined to be appropriate by the cell container.
     * For example, when the cell container is selected and the container has verified that
     * the selection is intended via some heuristics.
     *
     * @param value True to trigger the selection logic, false to trigger the unselection logic.
     */
    public void setSelected(boolean value);

    /**
     * Retrieve the item that the cell contents is currently associated with, to help the
     * cell container to verify the intention of the selection event.
     *
     * @return The CreationsListEntry or Recording object, whatever the actual list cell is
     *         associated with.
     */
    public Object getItem();

    /**
     * When the cell gets reused to display different cell contents, stop the old cell contents
     * from updating the wrong items.
     */
    public void detachListeners();
}
