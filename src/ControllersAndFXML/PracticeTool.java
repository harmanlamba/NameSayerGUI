package ControllersAndFXML;

import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;
import NameSayer.backend.Recording;
import NameSayer.backend.RecordingStore;
import com.jfoenix.controls.JFXSlider;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

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
    public ComboBox databaseComboBox;
    public Button databaseShuffleButton;
    public JFXSlider databaseVolumeSlider;
    public JFXSlider userVolumeSlider;


    public PracticeTool(Controller controller, CreationsListView listView, RecordingStore recordingStore) {
        _controller = controller;
        _listView = listView;
        _recordingStore= recordingStore;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateDatabaseRecordings();
    }


    public void recordButtonAction() throws IOException {
        _controller.recordButtonAction();
    }

    public void populateDatabaseRecordings() {
        _databaseRecordings = _listView.getSelectedRecordings();
        for (Recording counter : _databaseRecordings) {
            databaseComboBox.getItems().add(counter.getCreation().getName());
        }

    }

    public void shuffleDatabaseRecordings() {
        Collections.shuffle(_databaseRecordings);
        populateDatabaseRecordings();
    }

    public void userPlayButtonAction() {


    }

    public void databasePlayButtonAction() {
        String creationName = databaseComboBox.getValue().toString();
        Recording selectedRecording= _recordingStore.
        String filePath= selectedCreation.getPath();
        Media media = new Media(new File(filePath.toString()).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        _databaseMediaView.setMediaPlayer(mediaPlayer);
    }


}
