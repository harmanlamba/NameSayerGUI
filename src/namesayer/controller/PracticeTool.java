package namesayer.controller;

import namesayer.controller.components.QualityStars;
import namesayer.model.Creation;
import namesayer.model.CreationStore;
import namesayer.model.Recording;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXSlider;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class PracticeTool implements Initializable {
    private static final String PRACTICE_TEMP_FOLDER = "tempPractice";

    private Controller _controller;
    private CreationStore _creationStore;
    private MediaView _databaseMediaView = new MediaView();
    private MediaView _userMediaView = new MediaView();
    private ObservableList<Recording> _databaseRecordings;
    private ObservableList<Recording> _databaseOptions;
    private List<Recording> _recordings;
    private BooleanProperty _isUserMediaPlaying = new SimpleBooleanProperty();
    private BooleanProperty _isDatabaseMediaPlaying = new SimpleBooleanProperty();

    //Setting up the FXML injections so they can be referenced directly through the code
    public ComboBox<Recording> databaseComboBox;
    public ComboBox<Recording> userComboBox;
    public Button userPlayButton;
    public Button databasePlayButton;
    public Button databaseShuffleButton;
    public Button userRecordButton;
    public Label recordingLabel;
    public JFXSlider databaseVolumeSlider;
    public JFXSlider userVolumeSlider;
    public QualityStars databaseQualityStars;


    public PracticeTool(Controller controller, CreationStore creationStore, List<Recording> recordings) {
        _controller = controller;
        _creationStore = creationStore;
        _recordings = recordings;

        // Streaks.
        for (Recording recording : recordings) {
            Creation creation = recording.getCreation();
            creation.setStreaks(creation.getStreaks() + 1);
        }

        // Clone. Don't break the recording box if the selection changes in the main scene.
        _databaseOptions = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Filtering the recording so the database comboBox does not double up on database recordings and user recordings
        _databaseRecordings = filterSelectedRecordings(_recordings);
        populateDatabaseRecordings();
        //Setting up the rendering for the comboBox, as the comboBox contains type Recording and not type String,
        //So we have to tell the comboBox what to display in the cell.
        databaseComboBox.setCellFactory((listView) -> new JFXListCell<Recording>() {
            @Override
            protected void updateItem(Recording item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCreation().getName());
                }
            }
        });
        //Setting the same cell factory as the one previously defined for the button cell
        databaseComboBox.setButtonCell(databaseComboBox.getCellFactory().call(null));
        //For the first time the practice tool is open so the label displays the Recording name, and overides the
        //default text of "Label"
        if (databaseComboBox.getValue() != null) {
            recordingLabel.setText(databaseComboBox.getValue().getCreation().getName());
        }
        //Listener to update the recording label whenever the recording changes in the database comboBox
        databaseComboBox.valueProperty().addListener(e -> {
            recordingLabel.setText(databaseComboBox.getValue().getCreation().getName());
            databaseQualityStars.setRecording(databaseComboBox.getValue());
            refreshUserComboBox();
        });
        refreshUserComboBox();
        _creationStore.addListener(o -> refreshUserComboBox());
        databaseQualityStars.setRecording(databaseComboBox.getValue());
        //Defining how the cells for the user comboBox should be rendered
        userComboBox.setCellFactory((listView) -> new JFXListCell<Recording>() {
            @Override
            protected void updateItem(Recording item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDate().toString());

                }
            }
        });
        userComboBox.setButtonCell(userComboBox.getCellFactory().call(null));
        userComboBox.setPlaceholder(new Label("No attempts yet. Make one below"));

        //Setting up the volume sliders for both database recording mediaplayer and the user attempts mediaplayer
        userVolumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (_userMediaView.getMediaPlayer() != null) {
                    _userMediaView.getMediaPlayer().setVolume(userVolumeSlider.getValue() / 100);
                }
            }
        });
        databaseVolumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (_databaseMediaView.getMediaPlayer() != null) {
                    _databaseMediaView.getMediaPlayer().setVolume(databaseVolumeSlider.getValue() / 100);
                }
            }
        });

        /*
        Using boolean bindings to automatically bind the buttons disableProperty on the state of the user media playing
        and the database media playing. i.e: If a media is playing the buttons become disabled as the boolean property
        changes.
         */
        BooleanBinding isUserRecordingSelected = userComboBox.valueProperty().isNotNull();
        userPlayButton.disableProperty().bind(isUserRecordingSelected.not().or(_isDatabaseMediaPlaying).or(_isUserMediaPlaying));
        databasePlayButton.disableProperty().bind((_isDatabaseMediaPlaying).or(_isUserMediaPlaying));
        userRecordButton.disableProperty().bind((_isDatabaseMediaPlaying).or(_isUserMediaPlaying));
        databaseShuffleButton.disableProperty().bind((_isDatabaseMediaPlaying).or(_isUserMediaPlaying));

        //Ensuring that once we exit the PracticeTool the mediaplayers stop their respective playback.
        databaseComboBox.sceneProperty().addListener(o -> {
            databaseComboBox.getScene().windowProperty().addListener(o1 -> {
                databaseComboBox.getScene().getWindow().setOnHiding(event -> {
                    if (_databaseMediaView.getMediaPlayer() != null) {
                        _databaseMediaView.getMediaPlayer().stop();
                    }
                    if (_userMediaView.getMediaPlayer() != null) {
                        _userMediaView.getMediaPlayer().stop();
                    }
                });
            });
        });


    }

    //Re-using the code from RecordBox
    public void recordButtonAction() throws IOException {
        _controller.openRecordingBox(databaseComboBox.getValue().getCreation().getName());
    }

    public void populateDatabaseRecordings() {
        if (_databaseRecordings.size() > 1) {
            //Adding the concatenating recording as index 0, in the database comboBox
            new ConcatAndSilence(_databaseRecordings, PRACTICE_TEMP_FOLDER) {
                @Override
                public void ready(String filePathString) {
                    Path recordingPath = Paths.get(filePathString);
                    _databaseOptions.setAll(_databaseRecordings);
                    _databaseOptions.add(0, Recording.representConcatenated(_databaseRecordings, recordingPath));
                    databaseComboBox.setItems(_databaseOptions);
                    databaseComboBox.getSelectionModel().select(0);
                }
            };
        } else {
            _databaseOptions.setAll(_databaseRecordings);
            databaseComboBox.setItems(_databaseOptions);
            databaseComboBox.getSelectionModel().select(0);
        }
    }

    public void shuffleDatabaseRecordings() {
        //Using java's built in shuffle feature to randomize the selected recordings and then populate the database
        Collections.shuffle(_databaseRecordings);
        populateDatabaseRecordings();
    }

    //Setting up the action handlers for the userPlay button to playback the media when the play button is pressed.
    public void userPlayButtonAction() {
        Recording userRecordingToPlay = userComboBox.getValue();
        Media media = new Media(new File(userRecordingToPlay.getPath().toString()).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        _userMediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
        //Defining the boolean properties
        _isUserMediaPlaying.set(true);
        mediaPlayer.setOnEndOfMedia(() -> {
            _isUserMediaPlaying.set(false);
        });

    }

    //Setting up action handler for the databasePlayButton so that the media is loaded and played.
    public void databasePlayButtonAction() {
        List<Recording> recordings = new ArrayList<>();
        //Identifying which recording is selected
        recordings.add(databaseComboBox.getValue());
        //Passing it through the ConcatAndSilence to play the file adequately
        new ConcatAndSilence(recordings) {
            @Override
            public void ready(String filePath) {
                Recording recordingToPlay = databaseComboBox.getValue();
                Media media = new Media(new File(filePath).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                _databaseMediaView.setMediaPlayer(mediaPlayer);
                mediaPlayer.play();
                //Defining the boolean properties
                _isDatabaseMediaPlaying.set(true);
                mediaPlayer.setOnEndOfMedia(() -> {
                    _isDatabaseMediaPlaying.set(false);
                });
            }
        };
    }

    private void refreshUserComboBox() {
        if (databaseComboBox.getValue() == null) {
            userComboBox.getItems().clear();
            userComboBox.setValue(null);
        } else {
            // Note: using this indirect method to handle phantom recordings of concatenated names.
            String name = databaseComboBox.getValue().getCreation().getName();
            Creation creation = _creationStore.get(name);
            if (creation == null) {
                userComboBox.getItems().clear();
                userComboBox.setValue(null);
                return;
            }
            creation.addListener(o -> refreshUserComboBox());
            List<Recording> attempts = creation.getAttempts();
            userComboBox.getItems().setAll(attempts);
            if (attempts.size() > 0) {
                userComboBox.getSelectionModel().selectLast();
            }
        }
    }

    /*
    This method makes sure that only database recordings get added to the database comboBox by filtering thorough the
    recordings and adding the database recordings to the ObeservableList called databaseOnly. It is a static method since
    this method is a utility method.
     */
    public static ObservableList<Recording> filterSelectedRecordings(List<Recording> recordings) {
        ObservableList<Recording> databaseOnly = FXCollections.observableArrayList();
        for (Recording counter : recordings) {
            switch (counter.getType()) {
                case VERSION:
                    databaseOnly.add(counter);
                    break;
                case ATTEMPT:
                    //Do nothing
                    break;
            }
        }
        return databaseOnly;
    }

}