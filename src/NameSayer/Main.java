package NameSayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../ControllersAndFXML/MainSceneFoenix.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 618, 291));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
