package ControllersAndFXML;


import NameSayer.ConcatAndSilence;
import NameSayer.backend.CreationStore;
import NameSayer.backend.Recording;
import NameSayer.CreationFilter;
import com.jfoenix.controls.JFXSlider;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Collections;

public class Controller implements Initializable {

    private CreationStore _creationStore;
    private CreationFilter _creationFilter;
    private MediaView _mediaView = new MediaView();
    private ObservableList<Recording> _selectedRecordings;
    private BooleanProperty _isMediaPlaying = new SimpleBooleanProperty();

    @FXML
    public Button playButton;
    public Button nextButton;
    public Button previousButton;
    public Button recordButton;
    public Button compareButton;
    public Button practiceButton;
    public Button shuffleButton;
    public ComboBox comboBox;
    public Label topLabel;
    public Label bottomLabel;
    public HBox bottomLabelHBox;
    public HBox mediaControlHBox;
    public CreationsListView listView;
    public JFXSlider playbackSlider;
    public JFXSlider volumeSlider;
    public TextField textField;

    public Controller(CreationStore creationStore) {
        _creationStore = creationStore;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bottomLabelHBox.setMouseTransparent(false);
        mediaControlHBox.setMouseTransparent(false);
        bottomLabel.setMouseTransparent(false);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                System.out.println("new volume value: " + (volumeSlider.getValue() / 100));
                if (_mediaView.getMediaPlayer() != null) {
                    System.out.println("media player not null");
                    _mediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100);
                    System.out.println("media player volume: " + _mediaView.getMediaPlayer().getVolume());
                }
            }
        });
        comboBox.getItems().addAll("Baboons", "Soajsdlkfasd");

        _creationFilter = new CreationFilter(textField.textProperty(), _creationStore);
        listView.setCreationsList(_creationFilter.getFilterResults());

        playbackSlider.setDisable(true);
        playbackSlider.setMax(-1);
        playbackSlider.setMin(0);

        _selectedRecordings = listView.getSelectedRecordings();
        BooleanBinding isSelected = Bindings.isNotEmpty(_selectedRecordings);
        BooleanBinding isMultipleSelections = Bindings.size(_selectedRecordings).greaterThan(1);
        playbackSlider.disableProperty().bind(isSelected.not());
        practiceButton.disableProperty().bind(isSelected.not().or(_isMediaPlaying));
        recordButton.disableProperty().bind(isSelected.not().or(_isMediaPlaying));
        shuffleButton.disableProperty().bind(isMultipleSelections.not().or(_isMediaPlaying));
        topLabel.visibleProperty().bind(isSelected);

        InvalidationListener compareButtonDisabler = (Observable observable) -> {
            bottomLabel.setText(getCombinedName());
            compareButton.setDisable(
                _selectedRecordings.size() != 2 ||
                    _selectedRecordings.get(0).getCreation() != _selectedRecordings.get(1).getCreation() ||
                    _selectedRecordings.get(0).getType() == _selectedRecordings.get(1).getType() || _isMediaPlaying.get());
        };


        compareButton.setDisable(true);
        _selectedRecordings.addListener(compareButtonDisabler);
        _isMediaPlaying.addListener(compareButtonDisabler);

    }

    public void recordButtonAction() throws IOException {
        openRecordingBox(getCombinedName());
    }

    public void openRecordingBox(String creationName) throws IOException {
        //Parent recordingScene = FXMLLoader.load(getClass().getResource("/ControllersAndFXML/RecordingBox.fxml"));
        Stage recordingWindow = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ControllersAndFXML/RecordingBox.fxml"));
        //Important to note that we have a place-holder for the creationName...
        loader.setController(new RecordingBox(recordingWindow, creationName));
        Parent recordingScene = loader.load();
        recordingWindow.initModality(Modality.APPLICATION_MODAL);
        recordingWindow.setResizable(false);
        recordingWindow.setTitle("Recording Tool - " + creationName);
        recordingWindow.setScene(new Scene(recordingScene, 600, 168));
        recordingWindow.show();
        recordingScene.requestFocus();
        recordingWindow.setOnHidden(e -> {
            topLabel.requestFocus();
        });
    }

    public void shuffleButtonAction() {
        Collections.shuffle(_selectedRecordings);
    }

    public void playButtonAction() {
        Path _path = Paths.get("./data/tempPlayback");
        try {
            if (Files.notExists(_path)) {
                Files.createDirectories(_path);
            }
            if (!Files.isDirectory(_path)) {
                Files.delete(_path);
                Files.createDirectories(_path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConcatAndSilence concatAndSilence = new ConcatAndSilence(_selectedRecordings) {
            @Override
            public void ready(String filePath) {
                mediaLoaderAndPlayer(filePath);
            }
        };

    }

    public void nextButtonAction() {
        listView.selectNext();
        playButtonAction();
    }

    public void previousButtonAction() {
        listView.selectPrevious();
        playButtonAction();
    }


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
        mediaPlayer.setVolume(volumeSlider.getValue() / 100);

        //configuring the playback slider
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                playbackSlider.setValue(newValue.toSeconds());
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            _isMediaPlaying.set(false);
            playbackSlider.setValue(playbackSlider.getMax());
        });

        playbackSlider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mediaPlayer.seek(Duration.seconds(playbackSlider.getValue()));
            }
        });

    }

    public void compareRecordingsAction() throws IOException {
        Stage compareRecordingsWindow = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ControllersAndFXML/ComparingRecordingsBox.fxml"));
        loader.setController(new CompareRecordingsBox(listView));
        Parent comparingScene = loader.load();
        compareRecordingsWindow.initModality(Modality.APPLICATION_MODAL);
        compareRecordingsWindow.setResizable(false);
        compareRecordingsWindow.setTitle("Comparing Tool");
        compareRecordingsWindow.setScene(new Scene(comparingScene, 716, 198));
        compareRecordingsWindow.show();
        compareRecordingsWindow.requestFocus();
        compareRecordingsWindow.setOnHidden(e -> {
            topLabel.requestFocus();
        });

    }

    public void practiceRecordingsAction() throws IOException {
        Stage practiceRecordingsWindow = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ControllersAndFXML/PracticeTool.fxml"));
        loader.setController(new PracticeTool(this, _selectedRecordings));
        Parent comparingScene = loader.load();
        practiceRecordingsWindow.initModality(Modality.APPLICATION_MODAL);
        practiceRecordingsWindow.setResizable(false);
        practiceRecordingsWindow.setTitle("Practice Tool");
        practiceRecordingsWindow.setScene(new Scene(comparingScene, 794, 368));
        practiceRecordingsWindow.show();
        practiceRecordingsWindow.requestFocus();
        practiceRecordingsWindow.setOnHidden(e -> {
            topLabel.requestFocus();
        });

    }

    private String getCombinedName() {
        StringBuilder concatenatedName = new StringBuilder();

        if (_selectedRecordings.size() == 0) return "";

        for (Recording recording : _selectedRecordings) {
            concatenatedName.append(recording.getCreation().getName());
            concatenatedName.append(" ");
        }
        concatenatedName.deleteCharAt(concatenatedName.length() - 1);

        return concatenatedName.toString();
    }

}

