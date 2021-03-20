package me.ericballard.cwx.machine.hooks.mouse;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser.HHOOK;


import me.ericballard.cwx.CWX;
import me.ericballard.cwx.machine.apps.Automation;
import me.ericballard.cwx.machine.apps.Windows;
import me.ericballard.cwx.machine.hooks.mouse.struct.LowLevelMouseProc;
import me.ericballard.cwx.machine.hooks.mouse.struct.MOUSEHOOKSTRUCT;

import java.time.Instant;


public class MouseHook {

    private final LRESULT cancel = new LRESULT(1);

    private User32Util.MessageLoopThread messageThread;

    private HHOOK nativeHook;


    private long lastClick = -1L;

    public boolean started, running;

    // Event codes
    public static final int WM_MOUSEMOVE = 512,
            WM_MOUSESCROLL = 522,
            WM_MOUSELDOWN = 513,
            WM_MOUSELUP = 514,
            WM_MOUSEMDOWN = 519,
            WM_MOUSEMUP = 520,
            WM_MOUSERDOWN = 516,
            WM_MOUSERUP = 517;

    public MouseHook() {
    }

    public void hook() {
        if (started && running)
            return;

        started = true;
        running = false;

        new Thread(() -> {
            // Init
            final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
            final LowLevelMouseProc mouseHook = this::handle;

            // Hook
            nativeHook = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_MOUSE_LL, mouseHook, hMod, 0);
            System.out.println("CWX | MouseHook (ON)");

            // Listen - blocking
            messageThread = new User32Util.MessageLoopThread();

            running = true;
            messageThread.run();
        }).start();
    }

    public void unhook() {
        User32.INSTANCE.UnhookWindowsHookEx(nativeHook);

        if (messageThread != null)
            messageThread.interrupt();

        running = false;
        started = false;
        System.out.println("CWX | MouseHook (OFF)");
    }


    private LRESULT handle(int nCode, WPARAM wParam, MOUSEHOOKSTRUCT info) {
        final int x = info.pt.x, y = info.pt.y;
        //System.out.println("X: " + x + " Y: " + y);

        final int mouseEvent = wParam.intValue();

        switch (mouseEvent) {
            case WM_MOUSELUP:
            case WM_MOUSELDOWN:
                // Allow click to open project explorer button via automation (on start)
                if (!Automation.clickedButton) {
                    if (Windows.isOnButton(true, x, y)) {
                        break;
                    }
                }

                // Freeze mouse on start / Hijack click to select "Open" on project explorer
                boolean openedProject = false;

                if (Automation.freezeMouse || (openedProject = Windows.isOnButton(false, x, y))) {
                    if (openedProject) {
                        //TODO

                    }

                    return cancel;
                }

                break;
            case WM_MOUSEMUP:
            case WM_MOUSEMDOWN:
            case WM_MOUSERUP:
            case WM_MOUSERDOWN:
            case WM_MOUSESCROLL:
                if (Automation.freezeMouse)
                    return cancel;
                else
                    break;
            case WM_MOUSEMOVE:
                // Prevent moving mouse
                if (Automation.freezeMouse) {
                    // Unless move is to open project explorer button via automation (on start)
                    if (!Automation.movedMouse && Windows.isOnButton(true, x, y)) {
                        Automation.movedMouse = true;
                        break;
                    }
                    return cancel;
                }

                break;
        }

        // Detect double clicks
        if (mouseEvent == WM_MOUSELDOWN && CWX.windowFinder.isExplorerOpen) {
            // Disable double clicking while project explorer is open
            // (prevents click-to-select: which disallows us to poll project info)
            final long now = Instant.now().toEpochMilli();

            if (lastClick != -1L && now - lastClick < 500L) {
                if (Windows.isOnProject(x, y)) {
                    lastClick = Instant.now().toEpochMilli();
                    return cancel;
                }
            }

            lastClick = Instant.now().toEpochMilli();
        }

        Pointer ptr = info.getPointer();
        long peer = Pointer.nativeValue(ptr);
        return User32.INSTANCE.CallNextHookEx(nativeHook, nCode, wParam, new WinDef.LPARAM(peer));
    }


}