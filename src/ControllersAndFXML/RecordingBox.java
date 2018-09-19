package ControllersAndFXML;


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
    private Task _loopingTask;
    private Stage _recordingWindow;
    private Scene _primaryScene;
    private String _creationName;
    private int seconds = 5;

    public RecordingBox(Stage recordingWindow, String creationName) {
        _recordingWindow = recordingWindow;
        _creationName = creationName;
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
        loopingMicLevel();
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
        };
        Thread thread = new Thread(task);
        thread.start();

    }

    public void playRecording() {
        String creationPathTemp = "./data/tempCreations/tempAudio.wav";
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        Media media = new Media(new File(creationPathTemp).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        Platform.runLater(() -> {
            mediaPlayer.play();
        });
        delay.play();
        recordButton.setDisable(true);
        playButton.setDisable(true);
        cancelButton.setDisable(true);
        saveButton.setDisable(true);
        delay.setOnFinished(event -> {
            recordButton.setDisable(false);
            playButton.setDisable(false);
            cancelButton.setDisable(false);
            saveButton.setDisable(false);
        });
    }

    public void cancelRecordingBox() {
        deleteTempCreations();
        _recordingWindow.close();
        _loopingTask.cancel(true);

    }

    public void saveRecording() {
        String moveCreations = "mv ./data/tempCreations/tempAudio.wav ./data/attempts/" + "\"" + "se206_16-5-2018_23-34-53_Harman" + "\"" + ".wav";
        System.out.println("moveCreations = " + moveCreations);
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
        deleteTempCreations();
        _recordingWindow.close();
        _loopingTask.cancel(true);
    }

    private void recordingTimer() {
        final int[] seconds2 = {seconds};
        Timeline time = new Timeline();
        time.setCycleCount(Timeline.INDEFINITE);
        if (time != null) {
            time.stop();
        }
        KeyFrame frame = new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                recordingLabel.setOpacity(1);
                recordingSpinner.setDisable(false);
                recordingSpinner.setOpacity(1);
                recordingLabel.setWrapText(true);
                recordingLabel.setTextFill(Color.web("ab4642"));
                recordingLabel.setText("Recording NOW: You have " + seconds2[0] + " seconds remaining");
                if (seconds2[0] <= 0) {
                    time.stop();
                    recordingLabel.setOpacity(0);
                    recordingSpinner.setDisable(true);
                    recordingSpinner.setOpacity(0);
                }
                seconds2[0]--;
            }
        });
        time.getKeyFrames().add(frame);
        time.playFromStart();
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

    public int micInputLevel() {
        int level = 0;
        byte tempBuffer[] = new byte[4096];

        TargetDataLine targetRecordLine;
        AudioFormat format = new AudioFormat(11025, 8, 1, false, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        try {
            targetRecordLine = (TargetDataLine) AudioSystem.getLine(info);
            targetRecordLine.open(format);
            while (true) {
                if (targetRecordLine.read(tempBuffer, 0, tempBuffer.length) > 0) {
                    level = calculateAudioLevel(tempBuffer);
                    targetRecordLine.close();
                    return level;
                }else{
                    targetRecordLine.close();
                    break;
                }
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int calculateAudioLevel(byte[] audioData) {
        long lSum = 0;
        for (int i = 0; i < audioData.length; i++) {
            lSum = lSum + audioData[i];
        }

        double dAvg = lSum / audioData.length;
        double sumMeanSquare = 0d;

        for (int j = 0; j < audioData.length; j++) {
            sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);
        }

        double averageMeanSquare = sumMeanSquare / audioData.length;

        return (int) (Math.pow(averageMeanSquare, 0.5d) + 0.5);
    }

    public void loopingMicLevel() {
        _loopingTask = new Task<Void>() {
            int micLevel = 0;

            @Override
            protected Void call() throws Exception {
                while (!isCancelled()) {
                    micLevel = micInputLevel();
                    Platform.runLater(() -> {
                        progressBar.setProgress((double) micLevel);
                    });

                }
                return null;
            }

            @Override
            public void failed() {
                getException().printStackTrace();
            }
        };
        Thread thread = new Thread(_loopingTask);
        thread.setDaemon(true);
        thread.start();
    }

}
