package ControllersAndFXML;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecordingBox {
    private Stage _recordingWindow;
    private String _creationName;
    private int seconds = 5;

    public RecordingBox(Stage recordingWindow, String creationName) {
        _recordingWindow = recordingWindow;
        _creationName = creationName;
    }

    @FXML
    private Label upperLabel;

    public void startRecord() {
        Task task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                Path _path = Paths.get("./data/attempts/tempCreations");
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

                //String recordingCmd ="ffmpeg -f alsa -ac 2 -i default -t 5  ./Data/attempts/" + "\"" + _creationName + "\"" + ".wav";
                Platform.runLater(() -> {
                    recordingTimer();
                });

               /* ProcessBuilder recordingAudio = new ProcessBuilder("/bin/bash", "-c", recordingCmd);
                try {
                    Process process = recordingAudio.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.start();

    }

    public void playRecording() {

    }

    public void cancelRecordingBox() {
        deleteTempCreations();
        _recordingWindow.close();
    }

    public void saveRecording() {
        String moveCreations= "mv ./data/attempts/tempCreations/"+ "\""+_creationName+"\"";
        ProcessBuilder moveCreationsProcess= new ProcessBuilder("/bin/bash","-c",moveCreations);
        try {
            Process moveCreation =moveCreationsProcess.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        deleteTempCreations();
    }

    private void recordingTimer() {

        Timeline time = new Timeline();
        time.setCycleCount(Timeline.INDEFINITE);
        if (time != null) {
            time.stop();
        }
        KeyFrame frame = new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                upperLabel.setAlignment(Pos.CENTER);
                upperLabel.setWrapText(true);
                upperLabel.setTextFill(Color.web("ab4642"));
                upperLabel.setText("Recording NOW: You have " + seconds + " remaining");
                seconds--;
                if (seconds <= 0) {
                    time.stop();
                    upperLabel.setTextFill(Color.web("#e8e8e8"));
                    upperLabel.setText("Baboons");
                }
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

}
