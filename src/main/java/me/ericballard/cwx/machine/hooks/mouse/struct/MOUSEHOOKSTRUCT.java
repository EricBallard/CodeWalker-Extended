package me.ericballard.cwx.machine.hooks.mouse.struct;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.POINT;


public class MOUSEHOOKSTRUCT extends Structure {

    public POINT pt;
    public HWND hwnd;
    public int wHitTestCode;
    public ULONG_PTR dwExtraInfo;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pt", "hwnd", "wHitTestCode", "dwExtraInfo");
    }
}