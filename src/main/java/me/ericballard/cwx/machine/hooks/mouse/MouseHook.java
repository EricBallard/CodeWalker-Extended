package me.ericballard.cwx.machine.hooks.mouse;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import me.ericballard.cwx.machine.hooks.mouse.struct.LowLevelMouseProc;
import me.ericballard.cwx.machine.hooks.mouse.struct.MOUSEHOOKSTRUCT;


public class MouseHook {

    private HHOOK nativeHook;

    private User32Util.MessageLoopThread messageThread;


    // Event codes
    public static final int WM_MOUSEMOVE = 512,
            WM_MOUSESCROLL = 522,
            WM_MOUSELDOWN = 513,
            WM_MOUSELUP = 514,
            WM_MOUSEMDOWN = 519,
            WM_MOUSEMUP = 520,
            WM_MOUSERDOWN = 516,
            WM_MOUSERUP = 517;

    public MouseHook() {  }

    public void hook() {
        new Thread(() -> {
            // Init
            final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
            final LowLevelMouseProc mouseHook = this::handle;

            // Hook
            nativeHook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_MOUSE_LL, mouseHook, hMod, 0);
            System.out.println("CWX | MouseHook (ON)");

            // Listen - blocking
            messageThread = new User32Util.MessageLoopThread();
            messageThread.run();
        }).start();
    }

    public void unhook() {
        if (messageThread != null)
            messageThread.exit();

        System.out.println("CWX | MouseHook (OFF)");
        User32.INSTANCE.UnhookWindowsHookEx(nativeHook);
    }

    private LRESULT handle(int nCode, WPARAM wParam, MOUSEHOOKSTRUCT info) {
        switch (wParam.intValue()) {
            case WM_MOUSELUP:
            case WM_MOUSELDOWN:
                if (1 == 1)
                    return new LRESULT(1);
                else
                    break;
            case WM_MOUSEMUP:
                break;
            case WM_MOUSEMDOWN:
                break;
            case WM_MOUSERUP:
                break;
            case WM_MOUSERDOWN:
                break;
            case WM_MOUSESCROLL:
                break;
            case WM_MOUSEMOVE:
                break;
        }

        Pointer ptr = info.getPointer();
        long peer = Pointer.nativeValue(ptr);
        return User32.INSTANCE.CallNextHookEx(nativeHook, nCode, wParam, new WinDef.LPARAM(peer));
    }
}