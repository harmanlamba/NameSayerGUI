package namesayer;

import namesayer.model.Recording;
import namesayer.model.RecordingStore;
import namesayer.model.CreationStore;
import namesayer.controller.MainScene;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import namesayer.model.StreaksAndTiers;

import java.nio.file.Paths;


public class Main extends Application {

    private CreationStore _creationStore;
    private RecordingStore _versionsStore;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Reading and initializing data from database.
        initStores();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/namesayer/view/MainScene.fxml"));
        loader.setController(new MainScene(_creationStore, _versionsStore));
        Parent root = loader.load();
        primaryStage.setTitle("NameSayer");
        primaryStage.setScene(new Scene(root, 981, 553));
        primaryStage.setMinWidth(981);
        primaryStage.setMinHeight(553);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Begin listening to the folders.
    private void initStores() {
        _creationStore = new CreationStore();
        _versionsStore = new RecordingStore(Paths.get("data/database"), _creationStore, Recording.Type.VERSION);
        new RecordingStore(Paths.get("data/attempts"), _creationStore, Recording.Type.ATTEMPT);
        new StreaksAndTiers(_creationStore);
    }

}
