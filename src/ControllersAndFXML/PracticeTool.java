package ControllersAndFXML;

import NameSayer.ConcatAndSilence;
import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;
import NameSayer.backend.Recording;
import NameSayer.backend.RecordingStore;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class PracticeTool implements Initializable {
    private Controller _controller;
    private MediaView _databaseMediaView = new MediaView();
    private MediaView _userMediaView = new MediaView();
    private ObservableList<Recording> _databaseRecordings;
    private ObservableList<Recording> _attemptRecordings;
    private BooleanProperty _isUserMediaPlaying = new SimpleBooleanProperty();
    private BooleanProperty _isDatabaseMediaPlaying = new SimpleBooleanProperty();

    @FXML
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


    public PracticeTool(Controller controller, List<Recording> recordings) {
        _controller = controller;

        // Clone. Don't break the recording box if the selection changes in the main scene.
        _databaseRecordings = FXCollections.observableArrayList(recordings);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateDatabaseRecordings();
        databaseComboBox.setCellFactory((listView) -> new JFXListCell<Recording>() {
            @Override
            protected void updateItem(Recording item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item.getCreation().getName());
                }
            }
        });
        databaseComboBox.setButtonCell(databaseComboBox.getCellFactory().call(null));
        databaseComboBox.valueProperty().addListener(e -> {
            recordingLabel.setText(databaseComboBox.getValue().getCreation().getName());
            databaseQualityStars.setRecording(databaseComboBox.getValue());
            refreshUserComboBox();
        });
        refreshUserComboBox();
        databaseQualityStars.setRecording(databaseComboBox.getValue());

        userComboBox.setCellFactory((listView) -> new JFXListCell<Recording>() {
            @Override
            protected void updateItem(Recording item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item.getDate().toString());

                }
            }
        });
        userComboBox.setButtonCell(userComboBox.getCellFactory().call(null));
        userComboBox.setPlaceholder(new Label("No attempts yet. Make one below"));


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

        BooleanBinding isUserRecordingSelected = userComboBox.valueProperty().isNotNull();
        userPlayButton.disableProperty().bind(isUserRecordingSelected.not().or(_isDatabaseMediaPlaying).or(_isUserMediaPlaying));
        databasePlayButton.disableProperty().bind((_isDatabaseMediaPlaying).or(_isUserMediaPlaying));
        userRecordButton.disableProperty().bind((_isDatabaseMediaPlaying).or(_isUserMediaPlaying));
        databaseShuffleButton.disableProperty().bind((_isDatabaseMediaPlaying).or(_isUserMediaPlaying));

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

    public void recordButtonAction() throws IOException {
        _controller.openRecordingBox(databaseComboBox.getValue().getCreation().getName());
    }

    public void populateDatabaseRecordings() {
        databaseComboBox.setItems(_databaseRecordings);
        databaseComboBox.getSelectionModel().select(0);
    }

    public void shuffleDatabaseRecordings() {
        Collections.shuffle(_databaseRecordings);
        populateDatabaseRecordings();
    }

    public void userPlayButtonAction() {
        Recording userRecordingToPlay = userComboBox.getValue();
        Media media = new Media(new File(userRecordingToPlay.getPath().toString()).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        _userMediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
        _isUserMediaPlaying.set(true);
        mediaPlayer.setOnEndOfMedia(() -> {
            _isUserMediaPlaying.set(false);
        });

    }

    public void databasePlayButtonAction() {
        List<Recording> recordings = new ArrayList();
        recordings.add(databaseComboBox.getValue());
        new ConcatAndSilence(recordings) {
            @Override
            public void ready(String filePath) {
                Recording recordingToPlay = databaseComboBox.getValue();
                Media media = new Media(new File(filePath).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                _databaseMediaView.setMediaPlayer(mediaPlayer);
                mediaPlayer.play();
                _isDatabaseMediaPlaying.set(true);
                mediaPlayer.setOnEndOfMedia(() -> {
                    _isDatabaseMediaPlaying.set(false);
                });
            }
        };
    }

    private void refreshUserComboBox() {
        List<Recording> attempts = databaseComboBox.getValue().getCreation().getAttempts();
        userComboBox.getItems().setAll(attempts);
    }

}
