package me.ericballard.cwx.threads;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
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

            // GUI
            final Stage gui = GUI.get();

            // Position GUI over CodeWalker
            final DesktopWindow window;

            if (gui != null && (window = Apps.getWindow(false)) != null) {
                // CodeWalker window bounds
                Rectangle cwBounds = WindowUtils.getWindowLocationAndSize(window.getHWND());

                // Detect window title bar height
                if (barHeight == -1) {
                    barHeight = Apps.getTitleBarHeight(cwBounds);
                    continue;
                }

                final int w = cwBounds.width,
                        h = cwBounds.height;

                // Determine rather  CodeWalker is visible
                boolean shouldDisplay = false;

                // Require CodeWalker window to be >= 400px tall
                if (h >= 400) {
                    // Current focused window
                    final WinDef.HWND focused = User32.INSTANCE.GetForegroundWindow();

                    if (focused != null) {
                        if (focused.equals(window.getHWND()))
                            shouldDisplay = true;
                        else {
                            Rectangle focusedBounds = WindowUtils.getWindowLocationAndSize(focused);

                            final int blw = (int) (w * .05), blh = (int) (h * .05);
                            Rectangle boundLimits = new Rectangle(cwBounds.x + blw, cwBounds.y + blh, (w - blw), (h - blh));

                            if (!focusedBounds.intersects(boundLimits))
                                shouldDisplay = true;
                        }
                    }
                }

                // Show/hide overlay
                if (shouldDisplay) {
                    if (!gui.isShowing())
                        Platform.runLater(() -> gui.show());
                } else if (gui.isShowing()) {
                    Platform.runLater(() -> gui.hide());
                }

                final int blw = (int) (w * .05), blh = (int) (h * .10);
                Rectangle boundLimits = new Rectangle(cwBounds.x + (blw / 2), (int) (cwBounds.y + (blh / 1.5)), (w - blw), (h - blh));

                // Position stage over editor window
                gui.setY(boundLimits.getY());
                gui.setX(boundLimits.getX());

                // Set stage to size of editor window
                gui.setWidth(boundLimits.width);
                gui.setHeight(boundLimits.height);

                // Detect if mouse is within window title bar
                final Point mousePos = MouseInfo.getPointerInfo().getLocation();
                final Rectangle barBounds = new Rectangle(cwBounds.x, cwBounds.y, w, barHeight);

                if (!(userIsHoveringTitleBar = barBounds.contains(mousePos))) {
                    // Not hovering title bar are we on outskirts of window?

                    // NOTE: This works well for slow resizing but for quick, large-distance, resizing update is slightly chunky
                    userIsHoveringTitleBar = cwBounds.contains(mousePos) && !boundLimits.contains(mousePos);
                }

                //System.out.println("HOVERING: " + userIsHoveringTitleBar);
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
