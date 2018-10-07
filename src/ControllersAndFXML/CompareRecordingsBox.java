package ControllersAndFXML;

import NameSayer.ConcatAndSilence;
import NameSayer.backend.Recording;
import com.jfoenix.controls.JFXSlider;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CompareRecordingsBox implements Initializable {

    private MediaPlayer _leftPlayer;
    private MediaPlayer _rightPlayer;
    private Path[] _paths = new Path[2];
    private CreationsListView _listView;
    private List<Recording> recordings = new ArrayList<Recording>();
    private BooleanProperty _isLooping = new SimpleBooleanProperty();
    private BooleanProperty _isRightPlayerPLaying = new SimpleBooleanProperty();
    private BooleanProperty _isLeftPlayerPlaying = new SimpleBooleanProperty();

    public CompareRecordingsBox(CreationsListView listView) {
        _listView = listView;
    }

    //Setting up the FXML injections to be able to reference the components in the code
    @FXML
    public Label recordingLabel;
    public Label leftTitleLabel;
    public Button leftPlayButton;
    public Label rightTitleLabel;
    public Button rightPlayButton;
    public Button loopingButton;
    public JFXSlider leftVolumeSlider;
    public JFXSlider rightVolumeSlider;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Recording> selectedRecordings = _listView.getSelectedRecordings();
        recordingLabel.setText(selectedRecordings.get(0).getCreation().getName());
        //Determine which recording is the user selected one and which recording is the database one
        determineRecordingType(_listView.getSelectedRecordings().get(0), _listView.getSelectedRecordings().get(1));
        //Adding listeners to control the volume for each of the players
        leftVolumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (_leftPlayer != null) {
                    _leftPlayer.setVolume(leftVolumeSlider.getValue() / 100);
                }
            }
        });
        rightVolumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (_rightPlayer != null) {
                    _rightPlayer.setVolume(rightVolumeSlider.getValue() / 100);
                }
            }
        });
        //Ensuring that once the window is closed the recording playback stops
        leftPlayButton.sceneProperty().addListener(o -> {
            leftPlayButton.getScene().windowProperty().addListener(o1 -> {
                leftPlayButton.getScene().getWindow().setOnHiding(e -> {
                    if (_leftPlayer != null) {
                        _leftPlayer.stop();
                    }
                    if (_rightPlayer != null) {
                        _rightPlayer.stop();
                    }

                });
            });
        });

        //Setting up boolean properties
        leftPlayButton.disableProperty().bind(_isLeftPlayerPlaying.or(_isRightPlayerPLaying));
        rightPlayButton.disableProperty().bind(_isLeftPlayerPlaying.or(_isRightPlayerPLaying));
        _isLeftPlayerPlaying.addListener(e -> {
            if (_isLooping.get() && !_isLeftPlayerPlaying.get()) {
                rightPlayButtonAction();
            }
        });

        _isRightPlayerPLaying.addListener(e -> {
            if (_isLooping.get() && !_isRightPlayerPLaying.get()) {
                leftPlayButtonAction();
            }
        });


    }

    /*
    This methods task is to mainly distinguish the type of recording from the 2 selected recordings. I.e.: It identifies
    which recording is the user attempted recording and which one is a database recording, to then be able to display
    the recordings in the correct side, and hook them up to the correct media players.
     */
    public void determineRecordingType(Recording recording1, Recording recording2) {
        //_paths[0]= represents attempts
        //_paths[1]= represents versions

        switch (recording1.getType()) {
            case ATTEMPT:
                _paths[0] = recording1.getPath();
                leftTitleLabel.setText("User Recording: " + recording1.getDateString());
                break;
            case VERSION:
                _paths[1] = recording1.getPath();
                recordings.add(recording1);
                rightTitleLabel.setText("Database Recording: " + recording1.getDateString());
                break;
        }

        switch (recording2.getType()) {
            case ATTEMPT:
                _paths[0] = recording2.getPath();
                leftTitleLabel.setText("User Recording: " + recording2.getDateString());
                break;
            case VERSION:
                _paths[1] = recording2.getPath();
                recordings.add(recording2);
                rightTitleLabel.setText("Database Recording: " + recording2.getDateString());
                break;
        }


    }

    /*
    Adding action handlers for the left play, button. Essentially it just loads the media and gives it to the mediaplayer,
    so the user recording can be played.
    */
    public void leftPlayButtonAction() {
        Media leftMedia = new Media(new File(_paths[0].toString()).toURI().toString());
        _leftPlayer = new MediaPlayer(leftMedia);
        //Setting the volume of the mediaplayer to the current value of the volume slider
        _leftPlayer.setVolume(leftVolumeSlider.getValue() / 100);
        _leftPlayer.play();
        _isLeftPlayerPlaying.set(true);
        _leftPlayer.setOnEndOfMedia(() -> {
            _isLeftPlayerPlaying.set(false);
        });
        _leftPlayer.setOnStopped(() -> {
            _isLeftPlayerPlaying.set(false);
        });

    }

    public void rightPlayButtonAction() {
        /*
        Using ConcatAndSilence which is an abstract class, to play the database recording. For more information on the
        nature of this class, please refer to the comments located directly in ConcatAndSilence
         */
        new ConcatAndSilence(recordings) {
            @Override
            public void ready(String filePath) {
                Media rightMedia = new Media(new File(filePath).toURI().toString());
                _rightPlayer = new MediaPlayer(rightMedia);
                //Setting the volume of the mediaplayer to the current value of the volume slider
                _rightPlayer.setVolume(rightVolumeSlider.getValue() / 100);
                _rightPlayer.play();
                _isRightPlayerPLaying.set(true);
                _rightPlayer.setOnEndOfMedia(() -> {
                    _isRightPlayerPLaying.set(false);
                });
                _rightPlayer.setOnStopped(() -> {
                    _isRightPlayerPLaying.set(false);
                });
            }
        };
    }

    public void loopingButtonAction() {
        _isLooping.set(!_isLooping.get());
        if (_isLooping.get()) {
            loopingButton.setText("\uf24f");
            recordingLabel.requestFocus();
            leftPlayButtonAction();
        } else {
            loopingButton.setText("\uf201");
            recordingLabel.requestFocus();
            if (_leftPlayer != null) _leftPlayer.stop();
            if (_rightPlayer != null) _rightPlayer.stop();
        }

    }


}
