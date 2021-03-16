package me.ericballard.cwx.gui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.ericballard.cwx.CWX;

public class GUI {

    public static Controller controller;

    private static Stage guiStage;


    public static Stage get() {
        return guiStage;
    }

    public static void get(Stage stage) {
        // Init fxml
        Parent root;
        FXMLLoader loader = new FXMLLoader();

        try {
            root = loader.load(GUI.class.getClass().getResource("/Interface.fxml"));
        } catch (IOException e) {
            CWX.print(e);
            return;
        }

        loader.setController(new Controller());

        // Init scene and set to transparent
        Scene scene = new Scene(root);

        scene.setFill(Color.TRANSPARENT);
        stage.setAlwaysOnTop(true);

        stage.setOpacity(0.5);
        stage.setResizable(true);
        stage.initStyle(StageStyle.TRANSPARENT);

        stage.setScene(scene);

        // Cache stage and show
        (guiStage = stage).show();
    }
}
