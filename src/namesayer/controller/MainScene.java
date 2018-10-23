package namesayer.controller;


import namesayer.controller.components.TagInput;
import namesayer.controller.components.listview.CreationsListView;
import namesayer.model.CreationFilter;
import com.jfoenix.controls.JFXNodesList;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import namesayer.model.CreationStore;
import namesayer.model.Recording;
import namesayer.model.RecordingStore;
import namesayer.model.UserTextFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class MainScene implements Initializable {

    private CreationStore _creationStore;
    private RecordingStore _recordingStore;
    private CreationFilter _creationFilter;
    private MediaView _mediaView = new MediaView();
    private ObservableList<Recording> _selectedRecordings;
    private BooleanProperty _isMediaPlaying = new SimpleBooleanProperty(false);
    private BooleanProperty _isMediaPaused = new SimpleBooleanProperty(true);
    private BooleanProperty _isMediaLoading = new SimpleBooleanProperty(false);

    public Button playButton;
    public Button nextButton;
    public Button previousButton;
    public Button recordButton;
    public Button practiceButton;
    public Button shuffleButton;
    public ComboBox<CreationFilter.SortStrategy> comboBox;
    public Label topLabel;
    public Label bottomLabel;
    public CreationsListView listView;
    public JFXSlider playbackSlider;
    public JFXSlider volumeSlider;
    public JFXCheckBox filterDisabler;
    public TagInput tagInput;
    public JFXNodesList nodeList;
    public GridPane mainGridPane;
    public ScrollPane scrollPane;
    public HBox inputBar;

    public MainScene(CreationStore creationStore) {
        _creationStore = creationStore;
    }

    public MainScene(CreationStore creationStore, RecordingStore recordingStore) {
        _creationStore = creationStore;
        _recordingStore = recordingStore;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Making node list animate upwards.
        nodeList.setRotate(180);
        scrollPane.setFitToWidth(true);

        // Making sure that clicking anywhere in the scene makes it so the nodeLists collapses.
        mainGridPane.setOnMouseClicked(e -> {
            if (e.getPickResult().getIntersectedNode() != nodeList) {
                nodeList.animateList(false);
            }
        });
        listView.setOnMouseClicked(e -> {
            nodeList.animateList(false);
        });

        // Bind volume slider to media player.
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (_mediaView.getMediaPlayer() != null) {
                    _mediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100);
                }
            }
        });

        // Bind list view, sort type combo, tag input, and filter disabler.
        _creationFilter = new CreationFilter(tagInput.getChips(), _creationStore);
        listView.setCreationsList(_creationFilter.getFilterResults());
        comboBox.getItems().addAll(
            CreationFilter.SortStrategy.DONT_SORT,
            CreationFilter.SortStrategy.SORT_BY_NAME,
            CreationFilter.SortStrategy.SORT_BY_DATE
        );
        comboBox.getSelectionModel().selectFirst();
        comboBox.valueProperty().bindBidirectional(_creationFilter.sortStrategyProperty());
        filterDisabler.selectedProperty().bindBidirectional(_creationFilter.filterDisableProperty());
        tagInput.setCreationStore(_creationStore);

        // Disable and hide components appropriately:
        _selectedRecordings = listView.getSelectedRecordings();
        BooleanBinding isListEmpty = Bindings.isEmpty(_creationFilter.getFilterResults());
        BooleanBinding isSelected = Bindings.isNotEmpty(_selectedRecordings);
        BooleanProperty isTagsNotShown = filterDisabler.selectedProperty();
        BooleanBinding hasMultipleTags = Bindings.size(tagInput.getChips()).greaterThan(1);

        recordButton.disableProperty().bind(isSelected.not().or(_isMediaPaused.not()));
        shuffleButton.disableProperty().bind(hasMultipleTags.not().or(isTagsNotShown).or(_isMediaPaused.not()));
        playButton.disableProperty().bind(isSelected.not().or(_isMediaLoading));
        topLabel.visibleProperty().bind(isSelected);
        nextButton.disableProperty().bind(isListEmpty.or(_isMediaLoading));
        previousButton.disableProperty().bind(isListEmpty.or(_isMediaLoading));

        // Do not show thumb on playback slider when nothing is selected to play.
        isSelected.addListener(o -> {
            if (!isSelected.get()) {
                playbackSlider.setDisable(true);
                playbackSlider.setMax(-1);
                playbackSlider.setMin(0);
            } else {
                playbackSlider.setDisable(false);
            }
        });
        playbackSlider.setDisable(true);
        playbackSlider.setMax(-1);
        playbackSlider.setMin(0);

        // Disable practice button appropriately:
        InvalidationListener practiceButtonDisabler = (Observable observable) -> {
            practiceButton.setDisable(
                !_isMediaPaused.get() ||
                    PracticeTool.filterSelectedRecordings(_selectedRecordings).isEmpty());
        };
        _selectedRecordings.addListener(practiceButtonDisabler);
        _isMediaPaused.addListener(practiceButtonDisabler);
        practiceButtonDisabler.invalidated(null);

        // Play button icon sync.
        _isMediaPaused.addListener(o -> {
            if (_isMediaPaused.get()) {
                playButton.setText("\uf215"); // play icon when paused
            } else {
                playButton.setText("\uf478"); // pause icon when playing
            }
        });

        // Reset player whenever we change selections. Also update bottom label.
        _selectedRecordings.addListener((Observable o) -> {
            bottomLabel.setText(getCombinedName());
            playbackSlider.setValue(0);
            _isMediaPlaying.set(false);
            _isMediaPaused.set(true);
            if (_mediaView.getMediaPlayer() != null) {
                _mediaView.getMediaPlayer().stop();
                _mediaView.setMediaPlayer(null);
            }
        });

    }

    /**
     * Attaches useful shortcut key combinations to the specified scene.
     */
    public void initShortcuts(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN), () -> recordButtonAction());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN), () -> playButtonAction());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN), () -> listView.selectNext());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN), () -> listView.selectPrevious());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN), () -> previousButtonAction());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN), () -> nextButtonAction());
    }

    // Setting up the action handlers for the buttons

    /**
     * The associated action when the clear button is pressed. It clears the TagInput, in addition to clearing the
     * selectedRecordings list
     */
    public void clearButtonAction() {
        tagInput.getChips().clear();
        _selectedRecordings.clear();
    }

    /**
     * The associated action when the record button is pressed. The method simply opens the recordingTool, which then
     * takes over the recording process. Please refer to the comments in the RecordingTool for more further information.s
     * @throws IOException
     */
    public void recordButtonAction() {
        if (recordButton.isDisabled()) {
            return;
        }
        openRecordingBox(getCombinedName());
    }

    /**
     * The associated action when the shuffle button is pressed. The method simply shuffles the associated list which
     * keeps track of the creations. (i.e. _creationFiler)
     */
    public void shuffleButtonAction() {
        _creationFilter.shuffle();
    }

    /**
     * The associated action when the play button is pressed.
     * The play button has 3 roles.
     * 1. starting the media player.
     * 2. Pausing the media player if it's playing.
     * 3. Resuming the media player if it's paused.
     */
    public void playButtonAction() {
        if (playButton.isDisabled()) {
            return;
        }

        if (!_isMediaPlaying.get()) {
            _isMediaLoading.set(true);
            AudioProcessor audioProcessor = new AudioProcessor(_selectedRecordings) {
                @Override
                public void ready(String filePath) {
                    _isMediaLoading.set(false);
                    mediaLoaderAndPlayer(filePath);
                }

                @Override
                public void failed() {
                    _isMediaLoading.set(false);
                }
            };
        } else if (_isMediaPlaying.getValue() && !_isMediaPaused.get()) {
            _mediaView.getMediaPlayer().pause();
            _isMediaPaused.set(true);
        } else if (_isMediaPlaying.getValue() && _isMediaPaused.get()) {
            _mediaView.getMediaPlayer().play();
            _isMediaPaused.set(false);
        }
    }

    /**
     * The associated action when the next button (->) is pressed. The method simply
     * selects the next recording available from the listView.
     * It is important to note that this method has a recursive call to the method "playButtonAction" which then takes care
     * of handling the playback.
     */
    public void nextButtonAction() {
        if (nextButton.isDisabled()) {
            return;
        }
        listView.selectNext();
        playButtonAction();
    }

    /**
     * The associated action when the previous button (<-) is pressed. The method simply selects the previous recording
     * available from the listView. It is important to note that this method has a recursive call to the method
     * "playButtonAction" which then takes care of handling the playback.
     */
    public void previousButtonAction() {
        if (previousButton.isDisabled()) {
            return;
        }
        listView.selectPrevious();
        playButtonAction();
    }

    /**
     * This method is responsible to load the media and play it given the path of the file as a String. It is important
     * to note that it does not only handle the loading and playing but also handles the seek slider and the volume
     * slider
     * @param filePath The path to the file that has to be played.
     */
    public void mediaLoaderAndPlayer(String filePath) {

        Media media = new Media(new File(filePath).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        _mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.totalDurationProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                playbackSlider.setMax(newValue.toSeconds());
            }
        });
        mediaPlayer.play();
        _isMediaPlaying.set(true);
        _isMediaPaused.set(false);
        mediaPlayer.setVolume(volumeSlider.getValue() / 100);

        // Configuring the playback slider
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                playbackSlider.setValue(newValue.toSeconds());
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            _isMediaPlaying.set(false);
            _isMediaPaused.set(true);
            playbackSlider.setValue(playbackSlider.getMax());
        });

        playbackSlider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mediaPlayer.seek(Duration.seconds(playbackSlider.getValue()));
            }
        });

    }

    // Note: The following window-opening routines are repeated in code to encourage customizability
    // and tweaking of each window during our prototyping stage.

    /**
     * Corresponds to the action, when the "Practice" button is pressed. It sets the properties for the
     * PracticeTool and opens it in a new "Window/Stage"
     * @throws IOException
     */
    public void practiceRecordingsAction() throws IOException {
        Stage practiceRecordingsWindow = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/namesayer/view/PracticeTool.fxml"));
        loader.setController(new PracticeTool(this, _creationStore, _selectedRecordings, practiceRecordingsWindow));
        Parent comparingScene = loader.load();
        // Ensuring that the background stage can not be used while this stage is open
        practiceRecordingsWindow.initModality(Modality.APPLICATION_MODAL);
        practiceRecordingsWindow.setResizable(false);
        practiceRecordingsWindow.setTitle("Practice Tool");
        practiceRecordingsWindow.setScene(new Scene(comparingScene, 794, 368));
        practiceRecordingsWindow.show();
        practiceRecordingsWindow.requestFocus();
        practiceRecordingsWindow.setOnHidden(e -> {
            // Deselect the practice button.
            topLabel.requestFocus();
        });

    }

    /**
     * Corresponds to the associated action when the Recording button is pressed. This method sets up the properties
     * for the RecordingTool and opens it up in a new Stage.
     * @param creationName The name which is being recorded
     * @throws IOException
     */
    public void openRecordingBox(String creationName) {
        Stage recordingWindow = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/namesayer/view/RecordingTool.fxml"));
        loader.setController(new RecordingTool(recordingWindow, creationName));

        Parent recordingScene;
        try {
            recordingScene = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        recordingWindow.initModality(Modality.APPLICATION_MODAL);
        recordingWindow.setResizable(false);
        recordingWindow.setTitle("Recording Tool - " + creationName);
        recordingWindow.setScene(new Scene(recordingScene, 600, 168));
        recordingWindow.show();
        recordingScene.requestFocus();
        recordingWindow.setOnHidden(e -> {
            // Deselect the record button.
            topLabel.requestFocus();
        });
    }

    /**
     * Corresponds to the button action for "Append". This method passes the chosen database folder to the RecordingStore
     * for the recordings to appear in the CreationListView and be fully "followed/tracked".
     */
    public void appendDatabase() {
        tagInput.requestFocus();
        DirectoryChooser dc = new DirectoryChooser();
        File selectedDirectory = dc.showDialog(bottomLabel.getScene().getWindow());
        if (selectedDirectory != null) {
            try {
                new RecordingStore(Paths.get(selectedDirectory.getPath()), _creationStore, Recording.Type.VERSION);
            } catch (RecordingStore.StoreUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Corresponds to the action for the Replace button. It is important to note that in contrast to the append method
     * this method stops the watcher for the pre-loaded database and creates a new watcher for the new database that is
     * chosen by the user, which is initialised by passing it into the RecordingStore.
     */
    public void replaceDatabase() {
        tagInput.requestFocus();
        DirectoryChooser dc = new DirectoryChooser();
        File selectedDirectory = dc.showDialog(bottomLabel.getScene().getWindow());
        if (selectedDirectory != null) {
            _recordingStore.stopWatcher();
            _creationStore.clear();
            try {
                _recordingStore = new RecordingStore(Paths.get(selectedDirectory.getPath()), _creationStore, Recording.Type.VERSION);
            } catch (RecordingStore.StoreUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Corresponds to the "Edit Names List" button action, to display the tags in a larger popover.
     */
    public void expandInput() {
        tagInput.expand(inputBar);
    }

    /**
     * Corresponds to the "Upload List" button action, where a static utility method from the UserTextFile is called
     * to read the file. A List of List of Strings is used which is then set for the tagInput which then sets the TagInput.
     * It is important to note that the TagInput distinguishes each list in the list as its own "Tag" i.e. the row in the
     * text file.
     */
    public void uploadUserList() {
        List<List<String>> userNames = UserTextFile.readFile((Stage) bottomLabel.getScene().getWindow());
        tagInput.getChips().addAll(userNames);
    }

    private String getCombinedName() {
        return Recording.getCombinedName(_selectedRecordings);
    }

}

