package me.ericballard.cwx.machine.apps;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import me.ericballard.cwx.CWX;
import mmarquee.automation.AutomationException;
import mmarquee.automation.controls.AutomationBase;
import mmarquee.automation.controls.Window;

import java.awt.Point;

import java.util.List;

public class Windows {

    private static Window walker, editor;

    public static void resetEditor() {
        editor = null;
    }

    public static boolean isMinimized(WinDef.HWND handle) {
        // Get visibility state
        WinUser.WINDOWPLACEMENT placement = new WinUser.WINDOWPLACEMENT();
        User32.INSTANCE.GetWindowPlacement(handle, placement);

        switch (placement.showCmd) {
            case WinUser.SW_SHOWMINIMIZED:
            case WinUser.SW_FORCEMINIMIZE:
            case WinUser.SW_MINIMIZE:
            case WinUser.SW_RESTORE:
                return true;
            default:
                break;
        }
        return false;
    }

    public static boolean isOnButton(boolean project, int x, int y) {
        if (project || CWX.windowFinder.isExplorerOpen) {
            try {
                return (project ? CWX.windowFinder.projectButton : CWX.windowFinder.dialogButton).getBoundingRectangle().toRectangle().contains(new Point(x, y));
            } catch (AutomationException ignored) {
            }
        }
        return false;
    }

    public static boolean isOnProject(int x, int y) {
        final Point pos = new Point(x, y);

        try {
            AutomationBase explorerItems = CWX.windowFinder.extractChild(CWX.windowFinder.dialog.getChildren(true), "Items View");
            List<AutomationBase> children = explorerItems.getChildren(false);

            for (AutomationBase ab : children) {
                try {
                    if (ab.getName().contains(".cwproj")) {
                        if (ab.getBoundingRectangle().toRectangle().contains(pos))
                            return true;
                    }
                } catch (AutomationException ignored) {
                }
            }
        } catch (AutomationException | NullPointerException ignored) {
        }

        return false;
    }

    public static Window get(boolean codeWalker) {
        if (codeWalker ? walker == null : editor == null)
            find(codeWalker);

        return codeWalker ? walker : editor;
    }

    private static void find(boolean codeWalker) {
        //System.out.println("CWX | Searching for window (" + (codeWalker ? "CodeWalker" : "Editor") + ")");

        if (!codeWalker) {
            // Find editor window
            List<AutomationBase> children = null;

            try {
                children = walker.getChildren(false);

                for (AutomationBase ab : children) {
                    if (ab instanceof Window) {
                        final Window window = (Window) ab;

                        final WinDef.HWND hwnd = window.getNativeWindowHandle();
                        final String name = window.getName();

                        if (hwnd == null || name == null)
                            continue;
                        else if (name.contains("CodeWalker")) {
                            // Cache window and handle
                            Apps.setHandle(false, hwnd);
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
                    WinDef.HWND hwnd = window.getNativeWindowHandle();

                    // Failed to get handle?
                    if (hwnd == null)
                        continue;

                    // Cache window and handle
                    System.out.println("CWX | Found CodeWalker...");
                    Apps.setHandle(true, hwnd);
                    walker = window;

                    CWX.autoInteract.start();
                    break;
                }
            } catch (AutomationException ignored) {
            }
        }
    }
}
