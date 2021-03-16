package me.ericballard.cwx.threads;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import javafx.application.Platform;
import javafx.stage.Stage;
import me.ericballard.cwx.gui.GUI;
import me.ericballard.cwx.machine.Apps;

import java.awt.*;

public class OverlayPosition extends Thread {

    private boolean userIsHoveringTitleBar = false;

    private int barHeight = -1;

    @Override
    public void run() {
        System.out.println("CWX | Overlay Position - Started");

        // While app hasn't been closed
        while (!isInterrupted()) {
            // Position GUI over CodeWalker
            final DesktopWindow window = Apps.getWindow(false);

            if (window != null) {
                Rectangle bounds = WindowUtils.getWindowLocationAndSize(window.getHWND());

                // Detect window title bar height
                if (barHeight == -1) {
                    barHeight = Apps.getTitleBarHeight(bounds);
                    continue;
                }

                final int w = bounds.width,
                        h = bounds.height;

                // Detect if mouse is within window title bar
                Rectangle barBounds = new Rectangle(bounds.x, bounds.y, w, barHeight);
                userIsHoveringTitleBar = barBounds.contains(MouseInfo.getPointerInfo().getLocation());

                // Position stage over editor window
                Stage gui = GUI.get();

                gui.setY(bounds.getY());
                gui.setX(bounds.getX());

                // Set stage to size of editor window
                gui.setWidth(w);
                gui.setHeight(h);

                // Update scene elment size on javafx thread
                Platform.runLater(() -> {
                    // Adjust pane size
                    if (GUI.controller != null) {
                        // GUI.controller.anchorPane.setMinSizFe(w, h);
                        // GUI.controller.anchorPane.setMaxSize(w, h);

                        //  GUI.controller.gridPane.setMinSize(w, h);
                        // GUI.controller.gridPane.setMaxSize(w, h);
                    }
                });
            }

            try {
                Thread.sleep(userIsHoveringTitleBar ? 1 : 100);
            } catch (InterruptedException e) {
                break;
            }
        }

        System.out.println("CWX | Overlay Position - Stopped");
    }
}
