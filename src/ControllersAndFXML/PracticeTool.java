package ControllersAndFXML;

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
import java.util.ResourceBundle;


public class PracticeTool implements Initializable {
    private Controller _controller;
    private MediaView _databaseMediaView = new MediaView();
    private MediaView _userMediaView = new MediaView();
    private ObservableList<Recording> _databaseRecordings;
    private ObservableList<Recording> _attemptRecordings;

    @FXML
    public ComboBox<Recording> databaseComboBox;
    public ComboBox<Recording> userComboBox;
    public Button userPlayButton;
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

        populateDatabaseRecordings();

        refreshUserComboBox();
        databaseQualityStars.setRecording(databaseComboBox.getValue());
        databaseComboBox.getSelectionModel().selectedItemProperty().addListener(e -> {
            databaseQualityStars.setRecording(databaseComboBox.getValue());
            refreshUserComboBox();
        });

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
        userPlayButton.disableProperty().bind(isUserRecordingSelected.not());
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
    }

    public void databasePlayButtonAction() {
        Recording recordingToPlay = databaseComboBox.getValue();
        Media media = new Media(new File(recordingToPlay.getPath().toString()).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        _databaseMediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
    }

    private void refreshUserComboBox() {
        List<Recording> attempts = databaseComboBox.getValue().getCreation().getAttempts();
        userComboBox.getItems().setAll(attempts);
    }

}
