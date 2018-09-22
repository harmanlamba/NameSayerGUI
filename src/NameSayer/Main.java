package NameSayer;

import NameSayer.backend.Recording;
import NameSayer.backend.RecordingStore;
import NameSayer.backend.CreationStore;
import ControllersAndFXML.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.nio.file.Paths;


public class Main extends Application {

    private CreationStore _creationStore;

    @Override
    public void start(Stage primaryStage) throws Exception{
        initStores();

        Font.loadFont(getClass().getResource("/icons/ionicons.ttf").toExternalForm(), 10);

        //Parent root = FXMLLoader.load(getClass().getResource("/ControllersAndFXML/MainSceneFoenix.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ControllersAndFXML/MainSceneFoenix.fxml"));
        loader.setController(new Controller(_creationStore));
        Parent root= loader.load();
        primaryStage.setTitle("NameSayer");
        primaryStage.setScene(new Scene(root, 981, 553));
        primaryStage.setMinWidth(981);
        primaryStage.setMinHeight(553);
        primaryStage.show();
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
