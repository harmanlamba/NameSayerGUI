package namesayer.controller;

import namesayer.model.RecordingStore;
import namesayer.Util;

import com.jfoenix.controls.JFXSpinner;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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


public class RecordingTool implements Initializable {
    /**
     * Max duration to record before stopping.
     */
    private static final int MAX_SECONDS = 15;

    /**
     * Number of channels to record.
     * Set to mono (1) to be consistent with database recordings, so framesize is consistent when merging later on.
     */
    private static final int NUM_CHANNELS = 1;

    private Stage _recordingWindow;
    private String _creationName;
    private Timeline _recordingTimeline;
    private MediaPlayer _mediaPlayer;
    private MicrophoneLevel _microphoneLevel;
    private BooleanProperty _hasRecorded = new SimpleBooleanProperty();
    private BooleanProperty _isRecording = new SimpleBooleanProperty();
    private BooleanProperty _isPlaying = new SimpleBooleanProperty();

    private Task<Void> _recordingTask;

    public RecordingTool(Stage recordingWindow, String creationName) {
        _recordingWindow = recordingWindow;
        _creationName = creationName;

        // Setting it so having an instance of a RecordingTool automatically starts the mic level progress bar, and on
        // closing the window the mic level progress bar stops its data collection.
        _microphoneLevel = new MicrophoneLevel();
        _recordingWindow.setOnHiding(e -> {
            _microphoneLevel.close();
        });
    }

    // Setting up the FXML injections so that the components can be referenced directly by the code.

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
        saveButton.setDisable(true);

        _isRecording.addListener(o -> {
            if (_isRecording.get()) {
                recordButton.setText("Stop");

                // Starting the recording animation, which tells the user the amount of time they have remaining.
                recordingTimerStart();
            } else {
                // Note: don't set hasRecorded to true, as recording may have failed.
                recordButton.setText("Record");
                recordingTimerStop();
            }
        });

        _isPlaying.addListener(o ->{
            upperLabel.requestFocus();
        });

        recordButton.disableProperty().bind(_isPlaying);
        playButton.disableProperty().bind(_hasRecorded.not().or(_isRecording).or(_isPlaying));
        saveButton.disableProperty().bind(_hasRecorded.not().or(_isRecording).or(_isPlaying));
        cancelButton.disableProperty().bind(_isPlaying);
    }

    public void recordButtonClicked() {
        if (_isRecording.get()) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        _recordingTask = new Task<Void>() {

            @Override
            protected Void call() throws Util.HandledException {
                // Checking if the tempCreations folder exist, and in the case that it doesn't the folder is created.
                Util.ensureFolderExists(Paths.get("./data/tempCreations"));

                // Setting up the recording command for ffmpeg.
                String recordingCmd = "ffmpeg " +
                    "-nostdin " +
                    "-y " +
                    "-f alsa " +
                    "-ac " + NUM_CHANNELS + " " +
                    "-i default " +
                    "-t " + MAX_SECONDS + " " +
                    "./data/tempCreations/tempAudio.wav";

                // Ensuring that the correct buttons are disabled during the recording to not corrupt the recording by mistake.
                Platform.runLater(() -> {
                    _isRecording.set(true);
                    recordingLabel.requestFocus();
                });

                ProcessBuilder recordingAudio = new ProcessBuilder("/bin/bash", "-c", recordingCmd);
                try {
                    Process process = recordingAudio.start();
                    while (process.isAlive()) {
                        try {
                            if (isCancelled()) {
                                Thread.sleep(10);
                                process.destroy();
                                return null;
                            }
                            Thread.sleep(10);
                        } catch(InterruptedException e) {
                            // We don't mind getting interrupted.
                        }
                    }
                    if (process.exitValue() != 0) {
                        Util.showProblem("Error while recording audio",
                            "Sorry, but something went wrong while recording the audio",
                            "Please try again later.");
                        throw new Util.HandledException();
                    }
                } catch (IOException e) {
                    Util.showException(e, "Error while recording audio",
                        "Sorry, but something went wrong while recording the audio\n" +
                        "Please try again later.");
                    throw new Util.HandledException();
                }

                return null;
            }

            // After recording is completed, re-enable all the buttons for the user.
            @Override
            protected void succeeded() {
                _hasRecorded.set(true);
                cleanup();
            }

            @Override
            protected void failed() {
                cleanup();
            }

            @Override
            protected void cancelled() {
                _hasRecorded.set(true);
                cleanup();
            }

            private void cleanup() {
                _isRecording.set(false);
                _recordingTask = null;
            }
        };
        Thread thread = new Thread(_recordingTask);
        thread.start();

    }

    private void stopRecording() {
        if (_recordingTask == null) {
            return;
        }
        _recordingTask.cancel(false);
    }

    public void playRecording() {
        String creationPathTemp = "./data/tempCreations/tempAudio.wav";
        Media media = new Media(new File(creationPathTemp).toURI().toString());
        _mediaPlayer = new MediaPlayer(media);
        _mediaPlayer.play();
        _isPlaying.set(true);
        _mediaPlayer.setOnEndOfMedia(() -> {
            _isPlaying.set(false);
        });
    }

    public void cancelRecordingBox() {
        if (_isRecording.get()) {
            stopRecording();
        }
        _recordingWindow.close();
    }

    /**
     * Moves temporary recording that was created to the ./data/attempts
     * folder, which then gets picked up by the listeners and gets added to the list view.
     */
    public void saveRecording() {
        // Correctly naming the file in addition to having the move command.
        String moveCreations = "mv ./data/tempCreations/tempAudio.wav ./data/attempts/" +
            "\"" + "se206_" + RecordingStore.getDateStringNow() + "_" + _creationName + "\"" + ".wav";
        ProcessBuilder moveCreationsProcess = new ProcessBuilder("/bin/bash", "-c", moveCreations);
        try {
            Process moveCreation = moveCreationsProcess.start();
            Util.awaitProcess(moveCreation, "Error while saving your attempt",
                "Sorry, but we couldn't save your audio file into the folder of attempts.\n"  +
                "Please try again.");
        } catch (IOException e) {
            Util.showException(e, "Error while saving your attempt",
                "Sorry, but we couldn't save your audio file into the folder of attempts.\n" +
                "Please try again.");
        } catch (Util.HandledException e) {
            // Don't continue with closing window.
            // Don't do anything else with this exception since it's already handled.
            return;
        }
        _recordingWindow.close();
    }

    /**
     * Show and begin countdown animation.
     */
    private void recordingTimerStart() {

        final int[] seconds2 = { MAX_SECONDS };

        _recordingTimeline = new Timeline();
        _recordingTimeline.setCycleCount(Timeline.INDEFINITE);
        recordingLabel.setOpacity(1);
        recordingSpinner.setDisable(false);
        recordingSpinner.setOpacity(1);
        recordingLabel.setWrapText(true);

        // Changing the color to RED, to clearly tell the user recording has begun.
        recordingLabel.setTextFill(Color.web("ab4642"));

        // Counting down every second, having and EventHandler ensure that every second the value is reduced by one.
        KeyFrame frame = new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                recordingLabel.setText("Recording NOW: You have " + seconds2[0] + " seconds remaining");
                if (seconds2[0] <= 0) {
                    _recordingTimeline.stop();
                }
                seconds2[0]--;
            }
        });
        _recordingTimeline.getKeyFrames().add(frame);
        _recordingTimeline.playFrom(Duration.seconds(0.999));
    }

    private void recordingTimerStop() {
        recordingLabel.setOpacity(0);
        recordingSpinner.setDisable(true);
        recordingSpinner.setOpacity(0);
        _recordingTimeline.stop();
    }
}
