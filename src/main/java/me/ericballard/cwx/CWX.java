package me.ericballard.cwx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import me.ericballard.cwx.data.Data;
import me.ericballard.cwx.gui.GUI;
import me.ericballard.cwx.machine.hooks.key.KeyHook;
import me.ericballard.cwx.machine.hooks.mouse.MouseHook;
import me.ericballard.cwx.threads.AutoInteract;
import me.ericballard.cwx.threads.OverlayPosition;
import me.ericballard.cwx.threads.WindowFinder;


// CodeWalker-Extended
public class CWX extends Application {

    // Threads
    public static final WindowFinder windowFinder = new WindowFinder();

    public static final OverlayPosition overlayPosition = new OverlayPosition();

    public static final AutoInteract autoInteract = new AutoInteract();

    // Init global mouse hook (Temporary blocks to ensure perfect automation)
    public static MouseHook mouseHook = new MouseHook();

    public static KeyHook keyHook = new KeyHook();

    // Start of Application
    public static void main(String... args) {
        //Start of Application
        System.out.println("CWX | Started");

        // Init thread to identify CodeWalker windows
        windowFinder.start();

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
    }

    // Save data, close threads, etc
    public static void close() {
        Data.save();
        Platform.exit();

        mouseHook.unhook();
        keyHook.unhook();

        windowFinder.interrupt();
        overlayPosition.interrupt();
        autoInteract.interrupt();

        System.out.println("CWX | Stopped");
        System.exit(1);
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