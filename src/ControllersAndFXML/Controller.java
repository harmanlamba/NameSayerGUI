package ControllersAndFXML;


import NameSayer.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Screen;

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

}

