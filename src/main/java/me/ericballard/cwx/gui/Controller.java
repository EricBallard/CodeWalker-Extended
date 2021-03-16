package me.ericballard.cwx.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.*;

public class Controller implements Initializable {

    @FXML
    public GridPane gridPane;


    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        System.out.println("CWX | FXML Controller Initialized");

    }
}
