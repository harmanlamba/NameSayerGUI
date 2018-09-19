package ControllersAndFXML;



import NameSayer.backend.CreationStore;
import NameSayer.backend.Recording;
import com.jfoenix.controls.JFXSlider;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private CreationStore _creationStore;
    private MediaView _mediaView = new MediaView();

    @FXML
    public Button playButton;
    public Button nextButton;
    public Button previousButton;
    public Button recordButton;
    public ComboBox comboBox;
    public Label topLabel;
    public Label bottomLabel;
    public HBox bottomLabelHBox;
    public HBox mediaControlHBox;
    public CreationsListView listView;
    public JFXSlider playbackSlider;
    public JFXSlider volumeSlider;

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
                if(_mediaView.getMediaPlayer() != null) {
                    System.out.println("media player not null");
                    _mediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100);
                    System.out.println("media player volume: " + _mediaView.getMediaPlayer().getVolume());
                }
            }
        });
        comboBox.getItems().addAll("Baboons", "Soajsdlkfasd");
        listView.setCreationStore(_creationStore);
        listView.getSelectedRecordings().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                boolean isSelected = listView.getSelectedRecordings().size() > 0;
                playbackSlider.setDisable(!isSelected);
            }
        });
        playbackSlider.setDisable(true);
        playbackSlider.setMax(-1);
    }

    public void recordButtonAction() throws IOException {
        //Parent recordingScene = FXMLLoader.load(getClass().getResource("/ControllersAndFXML/RecordingBox.fxml"));
        Stage recordingWindow = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ControllersAndFXML/RecordingBox.fxml"));
        //Important to note that we have a place-holder for the creationName...
        loader.setController(new RecordingBox(recordingWindow, "creationName"));
        Parent recordingScene = loader.load();
        recordingWindow.initModality(Modality.APPLICATION_MODAL);
        recordingWindow.setResizable(false);
        recordingWindow.setTitle("Recording Box");
        recordingWindow.setScene(new Scene(recordingScene, 600, 168));
        recordingWindow.show();
        recordingScene.requestFocus();
        recordingWindow.setOnHidden(e -> {
            topLabel.requestFocus();
        });
    }

    public void shuffleButtonAction() {
        // TODO
    }

    public void playButtonAction() {
        //If concatenation has to be done
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
        //TODO

        //Just Playing already existing files
        String filePath;
        StringBuilder concatenatedFileName = new StringBuilder();
        ObservableList<Recording> selectedCreations = listView.getSelectedRecordings();

        for (Recording counter : selectedCreations) {
            concatenatedFileName.append(counter.getCreation().getName());
        }

        if (selectedCreations.size() > 1) {
            filePath = "./data/tempPlayback/tempAudio.wav";
        } else {
            filePath = selectedCreations.get(0).getPath().toString();
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                mediaLoaderAndPlayer("./data/attempts/audio.wav");
            }
        });
        thread.start();
    }

    public void nextButtonAction() {
       listView.selectNext();
       playButtonAction();
    }

    public void previousButtonAction(){
        listView.selectPrevious();
        playButtonAction();
    }


    public void mediaLoaderAndPlayer(String filePath) {

        PauseTransition delay= new PauseTransition(Duration.seconds(5));
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
        delay.play();
        recordButton.setDisable(true);
        previousButton.setDisable(true);
        playButton.setDisable(true);
        nextButton.setDisable(true);

        delay.setOnFinished(e -> {
            recordButton.setDisable(false);
            previousButton.setDisable(false);
            playButton.setDisable(false);
            nextButton.setDisable(false);
        });

        mediaPlayer.setVolume(volumeSlider.getValue()/100);

        //configuring the playback slider
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                playbackSlider.setValue(newValue.toSeconds());
            }
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
        //Important to note that we have a place-holder for the creationName...
        loader.setController(new CompareRecordingsBox());
        Parent comparingScene = loader.load();
        compareRecordingsWindow.initModality(Modality.APPLICATION_MODAL);
        compareRecordingsWindow.setResizable(false);
        compareRecordingsWindow.setTitle("Recording Box");
        compareRecordingsWindow.setScene(new Scene(comparingScene, 716, 198));
        compareRecordingsWindow.show();
        compareRecordingsWindow.requestFocus();
        compareRecordingsWindow.setOnHidden(e -> {
            topLabel.requestFocus();
        });


    }




}

