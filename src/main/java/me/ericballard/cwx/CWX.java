package me.ericballard.cwx;

import javafx.application.Platform;
import me.ericballard.cwx.gui.GUI;
import javafx.application.Application;
import javafx.stage.Stage;
import me.ericballard.cwx.threads.OverlayPosition;
import me.ericballard.cwx.threads.WindowFinder;

// CodeWalker-Extended
public class CWX extends Application {

    // Threads
    private static WindowFinder windowFinder;
    private static OverlayPosition overlayPosition;

    // Native Hooks

    // Start of Application
    public static void main(String... args) {
        //Start of Application
        System.out.println("CWX | Started");

        // Init thread to identify CodeWalker windows
        (windowFinder = new WindowFinder()).start();

        // Start GUI (launch call is blocking - will idle until parent frame closes)
        launch(args);

        // End of Application
        close();
    }

    // Start of GUI
    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(false);

        // Init GUI
        GUI.get(stage);

        // Init thread to position GUI
        (overlayPosition = new OverlayPosition()).start();
    }

    // Closes threads, etc
    private static void close() {
        windowFinder.interrupt();
        overlayPosition.interrupt();

        Platform.exit();
        System.out.println("CWX | Stopped");
    }

    // Prints stack track to system
    public static void print(Exception e) {
        String stackTrace = null;

        for (StackTraceElement ste : e.getStackTrace())
            stackTrace = (stackTrace != null ? stackTrace + "\n" : "") + ste;

        System.out.println(stackTrace);
        System.out.println(e.getMessage());
    }

}