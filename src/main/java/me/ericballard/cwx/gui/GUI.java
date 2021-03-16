package me.ericballard.cwx.gui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.ericballard.cwx.CWX;
import me.ericballard.cwx.data.Data;

public class GUI {

    public static Controller controller;

    private static Stage childStage;

    public static Stage get() {
        return childStage;
    }

    public static void get(Stage stage) {
        // Parent stage (Util - Allows app to not show in task bar)
        stage.initStyle(StageStyle.UTILITY);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setOpacity(0);
        stage.setHeight(0);
        stage.setWidth(0);

        // Child stage - (Transparent - Has all the goodies, fxml)
        Parent root;
        childStage = new Stage();

        try {
            // Load GUI from xml, cache controller instance, apply settings from saved data
            FXMLLoader loader = new FXMLLoader();
            root = loader.load(GUI.class.getClass().getResource("/Interface.fxml"));
            Data.load(controller = loader.getController());
        } catch (IOException e) {
            CWX.print(e);
            return;
        }

        // Init scene and set to transparent
        Scene scene = new Scene(root);
        childStage.setScene(scene);
        childStage.setOpacity(0.5);
        scene.setFill(Color.TRANSPARENT);
        childStage.initStyle(StageStyle.TRANSPARENT);

        // Bind parent w/h to child
        stage.widthProperty().addListener(e -> childStage.setWidth(stage.getWidth()));
        stage.heightProperty().addListener(e -> childStage.setHeight(stage.getHeight()));

        // Bind parent x/y to child
        stage.xProperty().addListener(e -> childStage.setX(stage.getX()));
        stage.yProperty().addListener(e -> childStage.setY(stage.getY()));

        // Bind parent stage to child
        childStage.initOwner(stage);
        stage.show();
    }
}
