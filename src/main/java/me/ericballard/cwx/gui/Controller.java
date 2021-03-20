package me.ericballard.cwx.gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;

public class Controller implements Initializable {

    @FXML
    public GridPane gridPane;

    @FXML
    public WebView webView;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        System.out.println("CWX | FXML Controller Initialized");

        webView.getEngine().load("http://gta-objects.xyz/");

        //GaussianBlur blur = new GaussianBlur();
        //blur.setRadius(100);

        //gridPane.setEffect(blur);

    }
}
