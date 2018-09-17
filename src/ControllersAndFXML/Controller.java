package ControllersAndFXML;


import NameSayer.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public Button playButton;
    public ComboBox comboBox;
    public Label topLabel;
    public Label bottomLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboBox.getItems().addAll("Baboons","Soajsdlkfasd");
    }

    public void recordButton() throws IOException {
        //Parent recordingScene = FXMLLoader.load(getClass().getResource("/ControllersAndFXML/RecordingBox.fxml"));
        Stage recordingWindow = new Stage();
        FXMLLoader loader= new FXMLLoader(getClass().getResource("/ControllersAndFXML/RecordingBox.fxml"));
        //Important to note that we have a place-holder for the creationName...
        loader.setController(new RecordingBox(recordingWindow, "creationName"));
        Parent recordingScene= loader.load();
        recordingWindow.initModality(Modality.APPLICATION_MODAL);
        recordingWindow.setResizable(false);
        recordingWindow.setTitle("Recording Box");
        recordingWindow.setScene(new Scene(recordingScene, 600 , 168));
        recordingWindow.show();
    }
}

