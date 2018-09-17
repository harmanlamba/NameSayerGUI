package ControllersAndFXML;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class RecordingBox {

    private int seconds=5;


    @FXML
    private Label upperLabel;

    public void startRecord(){
        //String recordingCmd ="ffmpeg -f alsa -ac 2 -i default -t 5  ./tempCreations/" + "\"" + creationName + "\"" + ".wav";
        recordingTimer();

        /*ProcessBuilder recordingAudio= new ProcessBuilder("/bin/bash","-c", recordingCmd);
        try{
            Process process= recordingAudio.start();
        }catch (IOException e){
            e.printStackTrace();
        } */
    }

    public void playRecording(){

    }

    public void cancelRecordingBox(){
        
    }

    public void saveRecording(){

    }

    private void recordingTimer(){

        Timeline time= new Timeline();
        time.setCycleCount(Timeline.INDEFINITE);
        if(time != null){
            time.stop();
        }
        KeyFrame frame= new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                upperLabel.setAlignment(Pos.CENTER);
                upperLabel.setWrapText(true);
                upperLabel.setText("Recording NOW: You have " + seconds + " remaining");
                seconds--;
                if(seconds <=0){
                    time.stop();
                    upperLabel.setText("Baboons");
                }
            }
        });
        time.getKeyFrames().add(frame);
        time.playFromStart();
    }


}
