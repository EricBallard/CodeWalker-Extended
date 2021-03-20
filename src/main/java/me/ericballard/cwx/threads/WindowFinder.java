package me.ericballard.cwx.threads;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import me.ericballard.cwx.CWX;
import me.ericballard.cwx.data.Data;
import me.ericballard.cwx.machine.apps.Apps;
import me.ericballard.cwx.machine.apps.Windows;
import mmarquee.automation.AutomationException;
import mmarquee.automation.controls.AutomationBase;
import mmarquee.automation.controls.Window;

import java.awt.Rectangle;
import java.util.List;

public class WindowFinder extends Thread {

    public AutomationBase dialog, button;

    public boolean isExplorerOpen;

    @Override
    public void run() {
        System.out.println("CWX | WindowFinder (STARTED)");

        boolean startedPositioner = false,
                foundWalker = false,
                foundEditor = false;

        int lookAttempts = 2;

        // While app hasn't been closed
        while (!isInterrupted()) {

            if (Data.settings == null)
                continue;
            else if (Apps.isClosed()) {
                // Verify CodeWalker is still running
                CWX.close();
                return;
            }

            // Look for window handles
            if (!foundWalker) {
                if (foundWalker = (Windows.get(true) != null)) {

                    if (Data.settings.saveWindowPosAndSize && Data.settings.wBounds != null) {
                        final Rectangle wBounds = Data.settings.wBounds;
                        User32.INSTANCE.MoveWindow(Apps.getHandle(true), wBounds.x, wBounds.y, wBounds.width, wBounds.height, true);
                        System.out.println("CWX |Applied window position and size to CodeWalker");
                    }
                } else {
                    if (lookAttempts-- == 0) {
                        System.out.println("CWX | Initializing CodeWalker...");

                        if (!Apps.open()) {
                            //TODO - dialog
                            CWX.close();
                            return;
                        }
                    } else if (lookAttempts > 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            } else if (!foundEditor) {
                // System.out.println("Looking for editor...");

                if (foundEditor = (Windows.get(false) != null)) {
                    // Init window position thread
                    if (!startedPositioner) {
                        startedPositioner = true;
                        CWX.overlayPosition.start();
                    } else {
                        synchronized (CWX.overlayPosition) {
                            CWX.overlayPosition.notify();
                        }
                    }

                    if (Data.settings.saveWindowPosAndSize && Data.settings.eBounds != null) {
                        final Rectangle eBounds = Data.settings.eBounds;
                        User32.INSTANCE.MoveWindow(Apps.getHandle(false), eBounds.x, eBounds.y, eBounds.width, eBounds.height, true);
                        System.out.println("CWX |Applied window position and size to Editor");
                    }
                }
            } else {
                // Look for file explorer
                final Window editor = Windows.get(false);

                if (editor == null) {
                    // Editor window closed
                    foundEditor = false;
                    continue;
                }

                if (!isExplorerOpen) {
                    if (isExplorerOpen = isProjectExplorerOpen(editor)) {
                        // Project explorer is open
                        System.out.println("CWX | Detected Project Explorer (OPEN)");
                    }
                } else {
                    // Check if explorer is still open
                    WinDef.HWND dialogHwnd = null;

                    try {
                        dialogHwnd = dialog.getNativeWindowHandle();
                    } catch (AutomationException ignored) {
                    }

                    // User has closed the project explorer without selecting project
                    if (dialogHwnd == null || !User32.INSTANCE.IsWindow(dialogHwnd)) {
                        setExplorerClosed();
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        System.out.println("CWX | WindowFinder (STOPPED)");
    }


    //        if (name.contains(".cwproj"))
//            projectName.set(c);
//        else if (name.contains("Address:"))
//            projectPath.set(c);

    private AutomationBase extractChild(List<AutomationBase> children, String byName) {
        for (AutomationBase child : children) {
            try {
                final String name = child.getName();
                if (name.contains(byName))
                    return child;
            } catch (AutomationException e) {
            }
        }
        return null;
    }

    private boolean isProjectExplorerOpen(Window editor) {
        List<AutomationBase> children;

        try {
            children = editor.getChildren(false);

            AutomationBase dialogExplorer = extractChild(children, "Open");

            // Project explorer is not open
            if (dialogExplorer == null)
                return false;

            // Dialog (Project Explorer) children
            children = dialogExplorer.getChildren(false);

            // Open project button
            AutomationBase openButton = extractChild(children, "Open");

            if (openButton == null)
                return false;

            dialog = dialogExplorer;
            button = openButton;
            return true;
        } catch (AutomationException ignored) {
        }

        return false;
    }

    public void setExplorerClosed() {
        dialog = null;
        button = null;
        isExplorerOpen = false;
        System.out.println("CWX | Detected Project Explorer (CLOSED)");
    }
}
