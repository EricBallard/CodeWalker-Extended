package me.ericballard.cwx.threads;

import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Platform;
import javafx.stage.Stage;
import me.ericballard.cwx.data.Data;
import me.ericballard.cwx.gui.GUI;
import me.ericballard.cwx.machine.apps.Apps;
import me.ericballard.cwx.machine.apps.Windows;

import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Point;


public class OverlayPosition extends Thread {

    //TODO re-write with native hooks

    @Override
    public void run() {
        System.out.println("CWX | Overlay Position - Started");
        boolean userIsHoverinControls = false;

        // While app hasn't been closed
        while (!isInterrupted()) {
            // GUI
            final Stage gui = GUI.get();

            if (gui != null) {
                final WinDef.HWND wHandle = Apps.getHandle(true), eHandle = Apps.getHandle(false);

                if (wHandle != null && eHandle != null) {
                    // Window bounds
                    Rectangle wBounds = null, eBounds;

                    try {
                        wBounds = WindowUtils.getWindowLocationAndSize(wHandle);
                        eBounds = WindowUtils.getWindowLocationAndSize(eHandle);
                    } catch (Win32Exception e) {
                        String closedWinodw = (wBounds == null ? "CodeWalker" : "Editor");
                        System.out.println("CWX | (" + closedWinodw + ") Window Closed - " + e.getMessage());

                        Platform.runLater(() -> GUI.get().hide());
                        Windows.resetEditor();

                        synchronized (this) {
                            try {
                                this.wait();
                            } catch (InterruptedException ignored) {
                                break;
                            }
                        }
                        continue;
                    }

                    // Cache window positions/sizes
                    if (Data.settings != null
                            && Data.settings.saveWindowPosAndSize) {

                        // Apply saved window positions/sizes
                        Data.settings.wBounds = wBounds;
                        Data.settings.eBounds = eBounds;
                    }


                    final int w = eBounds.width,
                            h = eBounds.height;

                    final int blw = (int) (w * .05), blh = (int) (h * .10);
                    Rectangle boundLimits = new Rectangle(eBounds.x + (blw / 2), (int) (eBounds.y + (blh / 1.5)), (w - blw), (h - blh));

                    // Determine rather editor is visible
                    boolean shouldDisplay = false;

                    // Require window to be >= 400px tall
                    if (w >= 600 && h >= 400) {
                        // Current focused window
                        final WinDef.HWND focused = User32.INSTANCE.GetForegroundWindow();

                        if (focused != null) {
                            if (focused.equals(eHandle) || focused.equals(wHandle))
                                shouldDisplay = true;
                            else {
                                Rectangle focusedBounds = null;

                                try {
                                    focusedBounds = WindowUtils.getWindowLocationAndSize(focused);
                                } catch (Win32Exception ignored) {
                                }

                                // Display if focused window is not over-lapping editor
                                shouldDisplay = focusedBounds != null && !focusedBounds.intersects(boundLimits);
                            }
                        }
                    }

                    // Show/hide overlay
                    if (shouldDisplay) {
                        if (!gui.isShowing())
                            Platform.runLater(gui::show);
                    } else if (gui.isShowing()) {
                        // Platform.runLater(gui::hide);
                    }

                    // Position stage over editor window
                    gui.setY(boundLimits.getY());
                    gui.setX(boundLimits.getX());

                    // Set stage to size of editor window
                    gui.setWidth(boundLimits.width);
                    gui.setHeight(boundLimits.height);

                    // Detect if mouse is within window title bar
                    final Point mousePos = MouseInfo.getPointerInfo().getLocation();

                    // NOTE: This works well for slow resizing but for quick, large-distance, resizing update is slightly chunky
                    userIsHoverinControls = eBounds.contains(mousePos) && !boundLimits.contains(mousePos);
                    //System.out.println("HOVERING: " + userIsHoveringTitleBar);
                }
            }

            try {
                Thread.sleep(userIsHoverinControls ? 1 : 100);
            } catch (InterruptedException e) {
                break;
            }
        }

        System.out.println("CWX | Overlay Position - Stopped");
    }
}
