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
        Font font=Font.loadFont(getClass().getResource("../icons/ionicons.ttf").toExternalForm(), 10);
        System.out.println(font);
        //System.out.println(Font.getFontNames());
        primaryStage.setTitle("NameSayer");
        primaryStage.setScene(new Scene(root, 981, 553));
        primaryStage.setMinWidth(981);
        primaryStage.setMinHeight(553);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
