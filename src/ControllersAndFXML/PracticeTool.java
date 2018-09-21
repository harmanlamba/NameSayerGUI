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
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
    private CreationsListView _listView;
    private MediaView _databaseMediaView;
    private MediaView _userMediaView;
    private ObservableList<Recording> _databaseRecordings;
    private ObservableList<Recording> _attemptRecordings;
    private RecordingStore _recordingStore;

    @FXML
    public ComboBox<Recording> databaseComboBox;
    public ComboBox<Recording> userComboBox;
    public Button databaseShuffleButton;
    public JFXSlider databaseVolumeSlider;
    public JFXSlider userVolumeSlider;


    public PracticeTool(Controller controller, CreationsListView listView, RecordingStore recordingStore) {
        _controller = controller;
        _listView = listView;
        _recordingStore = recordingStore;
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
        refreshUserComboBox();
        databaseComboBox.getSelectionModel().selectedItemProperty().addListener(e -> {
            refreshUserComboBox();
        });
        populateDatabaseRecordings();
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

    }


    public void recordButtonAction() throws IOException {
        _controller.recordButtonAction();
    }

    public void populateDatabaseRecordings() {
        _databaseRecordings = _listView.getSelectedRecordings();
        databaseComboBox.setItems(_databaseRecordings);
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

    }

    public void databasePlayButtonAction() {
        Recording recordingToPlay = databaseComboBox.getValue();
        Media media = new Media(new File(recordingToPlay.getPath().toString()).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        _databaseMediaView.setMediaPlayer(mediaPlayer);
    }

    private void refreshUserComboBox() {
        //userComboBox.setItems(databaseComboBox.getSelectionModel().getSelectedItem().getCreation().getAttempts());

    }

}
