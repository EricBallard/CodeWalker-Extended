package me.ericballard.cwx.machine.hooks.key;

import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;

import java.util.concurrent.atomic.AtomicBoolean;

public class KeyHook {

    private HHOOK nativeHook;

    private User32Util.MessageLoopThread messageThread;

    private AtomicBoolean isHoldingAlt = new AtomicBoolean(false);

    public KeyHook() { }

    public void hook() {
        new Thread(() -> {
            // Init
            final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
            final LowLevelKeyboardProc keyboardHook = this::handle;

            // Hook
            nativeHook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardHook, hMod, 0);
            System.out.println("CWX | KeyHook (ON)");

            // Listen - blocking
            messageThread = new User32Util.MessageLoopThread();
            messageThread.run();
        }).start();
    }

    public void unhook() {
        if (messageThread != null)
            messageThread.exit();

        System.out.println("CWX | KeyHook (OFF)");
        User32.INSTANCE.UnhookWindowsHookEx(nativeHook);
    }

    private LRESULT handle(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
        // Handle received key messages/events
        if (nCode >= 0) {
            // Enter=13
            // Alt=164
            // o=79

            final int key = info.vkCode;
            System.err.println("key=" + key);

            switch (wParam.intValue()) {
                case WinUser.WM_SYSKEYDOWN:
                case WinUser.WM_KEYDOWN:
                    if (key == 164)
                        isHoldingAlt.set(true);
                    else if (key == 79 && isHoldingAlt.get()) {
                        System.out.println("Detected Alt+o");
                    }
                    break;
                case WinUser.WM_SYSKEYUP:
                case WinUser.WM_KEYUP:
                    if (key == 164)
                        isHoldingAlt.set(false);

                    break;
            }
        }

        // Pointer ptr = info.getPointer();
        // long peer = Pointer.nativeValue(ptr);
        // return User32.INSTANCE.CallNextHookEx(nativeHook, nCode, wParam, new LPARAM(peer));
        return new LRESULT(1);
    }
}