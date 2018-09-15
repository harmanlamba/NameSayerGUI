package ControllersAndFXML;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public Button playButton;

    @FXML
    public ComboBox comboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboBox.getItems().addAll("Baboons","Soajsdlkfasd");
    }


    @FXML
    public void handleButton() {
        System.out.println("Baboons");
        playButton.setDisable(true);
    }

}

