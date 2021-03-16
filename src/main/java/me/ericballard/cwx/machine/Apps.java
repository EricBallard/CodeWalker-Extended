package me.ericballard.cwx.machine;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Apps {

    private static DesktopWindow walker, editor;

    public static DesktopWindow getWindow(boolean codeWalker) {
        if (codeWalker ? walker == null : editor == null)
            findWindow(codeWalker);
        return codeWalker ? walker : editor;
    }

    public static void resetEditor() { editor = null; }

    public static boolean isClosed() { return walker != null && !User32.INSTANCE.IsWindow(walker.getHWND()); }

    private static void findWindow(boolean codeWalker) {
        //System.out.println("CWX | Searching for window (" + (codeWalker ? "CodeWalker" : "Editor") + ")");

        // Iterate windows
        for (DesktopWindow desktopWindow : WindowUtils.getAllWindows(true)) {
            // Title
            final String title = desktopWindow.getTitle();

            //
            if (codeWalker ? title.equals("CodeWalker") : title.contains("- CodeWalker")) {
                // Cache game window and obtain handle
                HWND hwnd = (codeWalker ? (walker = desktopWindow).getHWND()
                        : (editor = desktopWindow).getHWND());

                // Failed to get handle?
                if (hwnd == null)
                    continue;

                // Success - Ensure window is visible and on top
                User32.INSTANCE.ShowWindow(hwnd, 9);
                User32.INSTANCE.SetForegroundWindow(hwnd);

                //System.out.println("CWX | Found window! (" + codeWalker + ")");
                break;
            }
        }
    }

    public static int getTitleBarHeight(Rectangle bounds) {
         /*
         Identify  the height of window title bar

         When user's mouse is within title bar bounds we want to update position
         for a more frequently (as it's likely the user could be re-sizing)
         to for a more seamless, native, effect

          You can check for the native Win32 event but it looks
          like it still requires recursive checking
        */

        Robot robot;

        try {
            robot = new Robot();
        } catch (AWTException e) {
            //TODO
            e.printStackTrace();
            return -1;
        }

        final int x = (int) (bounds.getX() + bounds.width / 1.5),
                y = (int) bounds.getY() + 1;

        int tempY = y, matchingPixels = 0;
        Color c = robot.getPixelColor(x, y);

        while (!Thread.interrupted()) {
            tempY += 1;

            if (robot.getPixelColor(x, tempY).equals(c)) {
                matchingPixels += 1;
            } else {
                if (matchingPixels > 1)
                    break;
                else
                    tempY = y;
            }
        }

        int barHeight = matchingPixels + 2;
        System.out.println("CWX | Detected Title Bar Height: " + barHeight);
        return barHeight;
    }

    public static boolean openCodeWalker() {
        final String path = "C:\\Users\\Desktop\\Desktop\\CW_dev\\";
        ProcessBuilder pb = new ProcessBuilder(path + "CodeWalker.exe");
        pb.directory(new File(path));

        try {
            pb.start();
        } catch (IOException e) {
            //TODO - dialog
            System.out.println("CWX | Failed to open CodeWalker.exe");
            return false;
        }

        return true;
    }
}
