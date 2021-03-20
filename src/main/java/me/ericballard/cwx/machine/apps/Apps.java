package me.ericballard.cwx.machine.apps;

import com.sun.jna.platform.win32.*;

import java.io.File;
import java.io.IOException;

public class Apps {

    public static String path = "C:\\Users\\Desktop\\Desktop\\CW_dev\\";

    private static WinDef.HWND walkerHandle, editorHandle;

    public static WinDef.HWND getHandle(boolean codeWalker) {
        return codeWalker ? walkerHandle : editorHandle;
    }

    public static void setHandle(boolean codeWalker, WinDef.HWND handle) {
        if (codeWalker)
            walkerHandle = handle;
        else
            editorHandle = handle;
    }

    public static boolean isClosed() {
        return walkerHandle != null && !User32.INSTANCE.IsWindow(walkerHandle);
    }

    public static boolean open() {
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
