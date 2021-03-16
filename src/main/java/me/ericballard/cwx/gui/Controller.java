package me.ericballard.cwx.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class Controller implements Initializable {

    @FXML
    public AnchorPane anchorPane;

    @FXML
    public GridPane gridPane;

    public Controller() {

    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        System.out.println("CWX | GUI Controller Initialized " + anchorPane);
    }
}
