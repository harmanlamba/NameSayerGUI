package namesayer.controller.components.listview;

/**
 * Displayed entry for a recording, or a creation with a single recording.
 */
private class SingleCellContents extends HBox implements CellContents {

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
        _btnDelete.setVisible(recording.getType() == Recording.Type.ATTEMPT);

        Streaks streaks = new Streaks();
        streaks.bindStreaks(recording.getCreation().streaksProperty());

        boolean showStreaks = recording.getCreation().getRecordingCount() == 1;

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
            showStreaks ? streaks : _btnDelete);
    }

    private void updateFromSelectedRecordings() {
        setSelected(_selectedRecordings.contains(_recording));
    }

    public Object getItem() {
        return _recording;
    }

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
