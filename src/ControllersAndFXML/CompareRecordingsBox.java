package ControllersAndFXML;

import NameSayer.backend.Recording;
import com.jfoenix.controls.JFXSlider;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CompareRecordingsBox implements Initializable {

    private MediaPlayer _leftPlayer;
    private MediaPlayer _rightPlayer;
    private Path[] _paths= new Path[2];
    private CreationsListView _listView;

    public CompareRecordingsBox(CreationsListView listView){
        _listView=listView;
    }

    @FXML
    public Label recordingLabel;
    public Label leftTitleLabel;
    public Button leftPlayButton;
    public Label rightTitleLabel;
    public Button rightPlayButton;
    public JFXSlider leftVolumeSlider;
    public JFXSlider rightVolumeSlider;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Recording> selectedRecordings= _listView.getSelectedRecordings();
        recordingLabel.setText(selectedRecordings.get(0).getCreation().getName());
        determineRecordingType(_listView.getSelectedRecordings().get(0),_listView.getSelectedRecordings().get(1));
        leftVolumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if(_leftPlayer != null){
                    _leftPlayer.setVolume(leftVolumeSlider.getValue()/100);
                }
            }
        });
        rightVolumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if(_rightPlayer != null){
                    _rightPlayer.setVolume(rightVolumeSlider.getValue()/100);
                }
            }
        });
    }


    public void determineRecordingType(Recording recording1, Recording recording2){
        //_paths[0]= represents attempts
        //_paths[1]= represents versions
        String pattern= "dd/mm/yyyy hh:mm:ss";
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat(pattern);

        switch(recording1.getType()){
            case ATTEMPT:
                _paths[0]=recording1.getPath();
                String date1Left= simpleDateFormat.format(recording1.getDate());
                leftTitleLabel.setText("User Recording: " +date1Left);
                break;
            case VERSION:
                _paths[1]=recording1.getPath();
                String date1Right= simpleDateFormat.format(recording1.getDate());
                rightTitleLabel.setText("Database Recording: "+date1Right);
                break;
        }

        switch(recording2.getType()) {
            case ATTEMPT:
                _paths[0] = recording2.getPath();
                String date2Left= simpleDateFormat.format(recording2.getDate());
                leftTitleLabel.setText("User Recording: " +date2Left);
                break;
            case VERSION:
                _paths[1] = recording2.getPath();
                String date2Right= simpleDateFormat.format(recording1.getDate());
                rightTitleLabel.setText("Database Recording: "+date2Right);
                break;
        }




    }

    public void leftPlayButtonAction(){
        Media leftMedia= new Media(new File(_paths[0].toString()).toURI().toString());
        _leftPlayer= new MediaPlayer(leftMedia);
        _leftPlayer.play();
        leftPlayButton.setDisable(true);
        rightPlayButton.setDisable(true);
        _leftPlayer.setOnEndOfMedia(() -> {
            leftPlayButton.setDisable(false);
            rightPlayButton.setDisable(false);
        });

    }

    public void rightPlayButtonAction(){
        Media rightMedia= new Media(new File(_paths[1].toString()).toURI().toString());
        _rightPlayer=new MediaPlayer(rightMedia);
        _rightPlayer.play();
        leftPlayButton.setDisable(true);
        rightPlayButton.setDisable(true);
        _rightPlayer.setOnEndOfMedia(() -> {
            leftPlayButton.setDisable(false);
            rightPlayButton.setDisable(false);
        });

    }


}
