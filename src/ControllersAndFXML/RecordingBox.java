package ControllersAndFXML;

import NameSayer.MicrophoneLevel;
import NameSayer.backend.RecordingStore;

import com.jfoenix.controls.JFXSpinner;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


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

        //Setting it so having an instance of a RecordingBox automatically starts the mic level progress bar, and on
        //closing the window the mic level progress bar stops its data collection.
        _microphoneLevel = new MicrophoneLevel();
        _recordingWindow.setOnHiding(e -> {
            _microphoneLevel.close();
        });
    }

    //Setting up the FXML injections so that the components can be referenced directly by the code.
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
                //Checking if the tempCreations folder exist, and in the case that it doesn't the folder is created
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

                //Setting up the recording command for ffmpeg
                String recordingCmd = "ffmpeg -nostdin -y -f alsa -ac 1 -i default -t 5  ./data/tempCreations/tempAudio.wav";
                //Ensuring that the correct buttons are disabled during the recording to not corrupt the recording by mistake
                Platform.runLater(() -> {
                    saveButton.setDisable(true);
                    recordButton.setDisable(true);
                    playButton.setDisable(true);
                    recordingLabel.requestFocus();
                    //Starting the recording animation, which tells the user the amount of time they have remaining
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

            //After recording is completed, re-enable all the buttons for the user
            @Override
            protected void succeeded() {
                playButton.setDisable(false);
                saveButton.setDisable(false);
                recordButton.setDisable(false);
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

   /*
   This method, when the user clicks saves, moves the temporary recording that was created and moves it to the ./data/attempts
   folder, which then gets picked up by the listeners and gets added to the main scene
    */
    public void saveRecording() {
        //Correctly naming the file in addition to having the move command
        String moveCreations = "mv ./data/tempCreations/tempAudio.wav ./data/attempts/" +
            "\"" + "se206_" + RecordingStore.getDateStringNow() + "_" + _creationName + "\"" + ".wav";
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

    /*
    This method handles the countdown for the timer.
     */
    private void recordingTimer() {
        //Retrieve the constant value that has been set, currently it is at 5
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
        //Changing the color to RED, to clearly tell the user recording has begun.
        recordingLabel.setTextFill(Color.web("ab4642"));
        //Counting down every second, having and EventHandler ensure that every second the value is reduced by one.
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
}
