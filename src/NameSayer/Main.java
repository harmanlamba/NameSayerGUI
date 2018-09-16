package NameSayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../ControllersAndFXML/MainSceneFoenix.fxml"));
        Font.loadFont(Main.class.getResource("../icons/fa-regular-400.ttf").toExternalForm(), 10);
        System.out.println(Font.getFontNames());
        primaryStage.setTitle("NameSayer");
        primaryStage.setScene(new Scene(root, 618, 291));
        primaryStage.setMinWidth(618);
        primaryStage.setMinHeight(291);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
