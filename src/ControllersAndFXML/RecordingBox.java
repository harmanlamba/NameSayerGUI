package ControllersAndFXML;

import NameSayer.MicrophoneLevel;

import com.jfoenix.controls.JFXSpinner;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;


public class RecordingBox implements Initializable {
    private Stage _recordingWindow;
    private Scene _primaryScene;
    private String _creationName;
    private int seconds = 5;
    private MediaPlayer _mediaPlayer;
    private MicrophoneLevel _microphoneLevel;

    public RecordingBox(Stage recordingWindow, String creationName) {
        _recordingWindow = recordingWindow;
        _creationName = creationName;

        _microphoneLevel = new MicrophoneLevel();

        _recordingWindow.setOnHiding(e -> {
            deleteTempCreations();
            _microphoneLevel.close();
        });
    }

    @FXML
    public Label upperLabel;
    public Label recordingLabel;
    public Button recordButton;
    public Button playButton;
    public Button cancelButton;
    public Button saveButton;
    public ProgressBar progressBar;
    public JFXSpinner recordingSpinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.progressProperty().bind(_microphoneLevel.levelProperty());
        playButton.setDisable(true);
    }

    public void startRecord() {
        Task task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                Path _path = Paths.get("./data/tempCreations");
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

                String recordingCmd ="ffmpeg -nostdin -y -f alsa -ac 2 -i default -t 5  ./data/tempCreations/tempAudio.wav";
                Platform.runLater(() -> {
                    recordingTimer();
                });

                ProcessBuilder recordingAudio = new ProcessBuilder("/bin/bash", "-c", recordingCmd);
                try {
                    Process process = recordingAudio.start();
                    try {
                        if (process.waitFor() != 0) {
                            System.out.println("Error in recording audio");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void succeeded() {
                playButton.setDisable(false);
                recordingLabel.setOpacity(0);
                recordingSpinner.setDisable(true);
                recordingSpinner.setOpacity(0);
            }

            @Override
            protected void failed() {
                recordingLabel.setOpacity(0);
                recordingSpinner.setDisable(true);
                recordingSpinner.setOpacity(0);
            }
        };
        Thread thread = new Thread(task);
        thread.start();

    }

    public void playRecording() {
        String creationPathTemp = "./data/tempCreations/tempAudio.wav";
        Media media = new Media(new File(creationPathTemp).toURI().toString());
        _mediaPlayer = new MediaPlayer(media);
        _mediaPlayer.play();
        recordButton.setDisable(true);
        playButton.setDisable(true);
        cancelButton.setDisable(true);
        saveButton.setDisable(true);
        _mediaPlayer.setOnEndOfMedia(() -> {
            recordButton.setDisable(false);
            playButton.setDisable(false);
            cancelButton.setDisable(false);
            saveButton.setDisable(false);
            upperLabel.requestFocus();
        });
    }

    public void cancelRecordingBox() {
        _recordingWindow.close();
    }

    public void saveRecording() {
        // TODO
        String moveCreations = "mv ./data/tempCreations/tempAudio.wav ./data/attempts/" + "\"" + "se206_16-5-2018_23-34-53_" + _creationName + "\"" + ".wav";
        ProcessBuilder moveCreationsProcess = new ProcessBuilder("/bin/bash", "-c", moveCreations);
        try {
            Process moveCreation = moveCreationsProcess.start();
            try {
                if (moveCreation.waitFor() != 0) {
                    System.out.println("Error in saving recording");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        _recordingWindow.close();
    }

    private void recordingTimer() {
        final int[] seconds2 = {seconds};
        Timeline time = new Timeline();
        time.setCycleCount(Timeline.INDEFINITE);
        if (time != null) {
            time.stop();
        }
        recordingLabel.setOpacity(1);
        recordingSpinner.setDisable(false);
        recordingSpinner.setOpacity(1);
        recordingLabel.setWrapText(true);
        recordingLabel.setTextFill(Color.web("ab4642"));
        KeyFrame frame = new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                recordingLabel.setText("Recording NOW: You have " + seconds2[0] + " seconds remaining");
                if (seconds2[0] <= 0) {
                    time.stop();
                }
                seconds2[0]--;
            }
        });
        time.getKeyFrames().add(frame);
        time.playFrom(Duration.seconds(0.999));
    }

    public void deleteTempCreations() {
        String deleteCmd = "rm -r ./data/attempts/tempCreations";
        /*ProcessBuilder deleteTempCreations = new ProcessBuilder("/bin/bash", "-c", deleteCmd);
        try {
            Process process = deleteTempCreations.start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }


}
