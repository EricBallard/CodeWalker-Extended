package me.ericballard.cwx.machine;

import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import me.ericballard.cwx.CWX;
import mmarquee.automation.AutomationException;
import mmarquee.automation.controls.AutomationBase;
import mmarquee.automation.controls.Window;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Apps {

    private static Window walker, editor;

    private static HWND walkerHandle, editorHandle;

    public static Window getWindow(boolean codeWalker) {
        if (codeWalker ? walker == null : editor == null)
            findWindow(codeWalker);

        return codeWalker ? walker : editor;
    }

    public static HWND getHandle(boolean codeWalker) {
        return codeWalker ? walkerHandle : editorHandle;
    }

    public static void resetEditor() {
        editor = null;
    }

    public static boolean isClosed() {
        return walker != null && !User32.INSTANCE.IsWindow(walkerHandle);
    }

    private static void findWindow(boolean codeWalker) {
        //System.out.println("CWX | Searching for window (" + (codeWalker ? "CodeWalker" : "Editor") + ")");

        if (!codeWalker) {
            // Find editor window
            List<AutomationBase> children = null;

            try {
                children = walker.getChildren(false);

                for (AutomationBase ab : children) {
                    if (ab instanceof Window) {
                        final Window window = (Window) ab;

                        final HWND hwnd = window.getNativeWindowHandle();
                        final String name = window.getName();

                        if (hwnd == null || name == null)
                            continue;
                        else if (name.contains("CodeWalker")) {
                            // Cache window and handle
                            editorHandle = hwnd;
                            editor = window;

                            System.out.println("CWX | Found Project Editor!");
                            break;
                        }
                    }
                }
            } catch (AutomationException ignored) {
            }
            return;
        }

        // Iterate windows
        List<Window> windows = null;

        try {
            windows = Automation.get().getDesktopWindows();
        } catch (AutomationException e) {
            //TODO -handle + dialog?
            e.printStackTrace();
            return;
        }

        // Find window via automation api (Grants native hwnd + attached process for uiautomation)
        for (Window window : windows) {
            try {
                final String title = window.getName();

                if (title.equals("CodeWalker")) {
                    HWND hwnd = window.getNativeWindowHandle();

                    // Failed to get handle?
                    if (hwnd == null)
                        continue;

                    // Cache window and handle
                    walkerHandle = hwnd;
                    walker = window;

                    // Success - Ensure window is visible and on top
                    //User32.INSTANCE.ShowWindow(hwnd, 9);
                    //User32.INSTANCE.SetForegroundWindow(hwnd);
                    System.out.println("CWX | Found CodeWalker...");
                    CWX.autoInteract.start();
                    break;
                }
            } catch (AutomationException ignored) {
            }
        }
    }


    public static String path = "C:\\Users\\Desktop\\Desktop\\CW_dev\\";

    public static boolean openCodeWalker() {
        ProcessBuilder pb = new ProcessBuilder(path + "CodeWalker.exe");
        pb.directory(new File(path));

        try {
            pb.start();
        } catch (IOException e) {
            //TODO - dialog
            System.out.println("CWX | Failed to open CodeWalker.exe");
            return false;
        }

        //Input.init();
        return true;
    }
}
