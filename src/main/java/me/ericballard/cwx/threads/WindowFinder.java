package me.ericballard.cwx.threads;

import com.sun.jna.platform.win32.User32;
import me.ericballard.cwx.CWX;
import me.ericballard.cwx.data.Data;
import me.ericballard.cwx.machine.Apps;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.Instant;

public class WindowFinder extends Thread {

    final static DecimalFormat df = new DecimalFormat("##.##");

    private static boolean foundWalker, foundEditor, startedPositioner;

    private static long startTime = -1L;

    private static int lookAttempts = 2;

    @Override
    public void run() {
        System.out.println("CWX | Identifying CodeWalker Window(s)...");
        foundEditor = false;

        // While app hasn't been closed
        while (!isInterrupted()) {
            if (Data.settings == null)
                continue;

            // Verify CodeWalker is still running
            if (Apps.isClosed()) {
                CWX.close();
                return;
            }


            // Init time elapsed
            if (startTime == -1L)
                startTime = Instant.now().toEpochMilli();

            // Look for window handles
            if (!foundWalker) {
                if (foundWalker = (Apps.getWindow(true) != null)) {

                    if (Data.settings.saveWindowPosAndSize && Data.settings.wBounds != null) {
                        final Rectangle wBounds = Data.settings.wBounds;
                        User32.INSTANCE.MoveWindow(Apps.getHandle(true), wBounds.x, wBounds.y, wBounds.width, wBounds.height, true);
                        System.out.println("CWX |Applied window position and size to CodeWalker");
                    }

                    continue;
                } else {
                    if (lookAttempts-- == 0) {
                        System.out.println("CWX | Initializing CodeWalker...");

                        if (!Apps.openCodeWalker()) {
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
                if (foundEditor = (Apps.getWindow(false) != null)) {
                    // Init window position thread
                    if (!startedPositioner) {
                        startedPositioner = true;
                        CWX.overlayPosition.start();
                    }

                    if (Data.settings.saveWindowPosAndSize && Data.settings.eBounds != null) {
                        final Rectangle eBounds = Data.settings.eBounds;
                        User32.INSTANCE.MoveWindow(Apps.getHandle(false), eBounds.x, eBounds.y, eBounds.width, eBounds.height, true);
                        System.out.println("CWX |Applied window position and size to Editor");
                    }
                    continue;
                }
            } else {
                // Found all the windows - time to get hacky
                final double timeElapsed = (Instant.now().toEpochMilli() - startTime) / 1000.0;
                System.out.println("CWX | Identified CodeWalker and Editor in " + df.format(timeElapsed) + "s");
                startTime = -1L;

                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                continue;
            }
        }
    }
}
