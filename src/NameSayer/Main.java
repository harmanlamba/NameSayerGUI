package NameSayer;

import java.nio.file.Paths;

import ControllersAndFXML.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import NameSayer.backend.Recording;
import NameSayer.backend.RecordingStore;
import NameSayer.backend.CreationStore;

public class Main extends Application {

    CreationStore _creationStore;

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("/ControllersAndFXML/MainSceneFoenix.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ControllersAndFXML/MainSceneFoenix.fxml"));
        loader.setController(new Controller());
        Parent root= loader.load();
        primaryStage.setTitle("NameSayer");
        primaryStage.setScene(new Scene(root, 981, 553));
        primaryStage.setMinWidth(981);
        primaryStage.setMinHeight(553);
        primaryStage.show();

        initStores();
    }

    public static void main(String[] args) {
        launch(args);
    }
  
    private void initStores() {
        _creationStore = new CreationStore();
        new RecordingStore(Paths.get("data/database"), _creationStore, Recording.Type.VERSION);
        new RecordingStore(Paths.get("data/attempts"), _creationStore, Recording.Type.ATTEMPT);

        _creationStore.debugDump();
    }

}