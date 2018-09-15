package NameSayer;

import java.nio.file.Paths;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import NameSayer.backend.Recording;
import NameSayer.backend.RecordingStore;
import NameSayer.backend.CreationStore;

public class Main extends Application {

    CreationStore _creationStore;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../ControllersAndFXML/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
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
