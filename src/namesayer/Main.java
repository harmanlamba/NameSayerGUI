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
        MainScene controller = new MainScene(_creationStore, _versionsStore);
        loader.setController(controller);
        Parent root = loader.load();
        primaryStage.setTitle("NameSayer");
        Scene scene = new Scene(root, 981, 553);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(981);
        primaryStage.setMinHeight(553);
        primaryStage.show();

        controller.initShortcuts(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Begin listening to the folders.
    private void initStores() {
        try {
            _creationStore = new CreationStore();
            _versionsStore = new RecordingStore(Paths.get("data/database"), _creationStore, Recording.Type.VERSION);
            new RecordingStore(Paths.get("data/attempts"), _creationStore, Recording.Type.ATTEMPT);
            new StreaksAndTiers(_creationStore);
        } catch (RecordingStore.StoreUnavailableException e) {
            // Can't continue running the app without all stores available.
            System.out.println("Some stores are unavailable. Closing the app.");
            System.exit(1);
        }
    }

}
