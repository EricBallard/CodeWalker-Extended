package me.ericballard.cwx.machine.apps;

import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.*;
import me.ericballard.cwx.CWX;
import mmarquee.automation.AutomationException;
import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.*;
import mmarquee.automation.controls.Button;
import mmarquee.automation.controls.Window;

import java.awt.*;

public class Automation {

    // Win32 UIAutomation (Java Wrapper) Instance
    private static UIAutomation automation = null;

    public static UIAutomation get() {
        return automation == null ? (automation = UIAutomation.getInstance()) : automation;
    }

    // UI Controllable Entities
    public enum ControlType {
        BUTTON, CHECKBOX, TAB, TITLE_BAR, TOOLBAR_BUTTON
    }

    private static ToolBar toolbar;

    // Hook control
    public static boolean freezeMouse, movedMouse, clickedButton;

    // Interact
    public static boolean interact(Window window, ControlType control, String id) {
        try {
            AutomationBase element;

            switch (control) {
                case BUTTON:
                    if ((element = window.getButtonByAutomationId(id)) != null) {
                        ((Button) element).click();
                        return true;
                    }
                    break;
                case CHECKBOX:
                    CheckBox cb = window.getCheckBoxByAutomationId(id);

                    if (cb != null && cb.isEnabled()) {
                        cb.toggle();
                        return true;
                    }

                    break;
                case TAB:
                    if ((element = window.getTab(Search.getBuilder(0).build())) != null) {
                        ((Tab) element).selectTabPage(id);
                        return true;
                    }
                    break;
                case TOOLBAR_BUTTON:
                    if (toolbar != null || (toolbar = window.getToolBar(0)) != null) {
                        Button b = toolbar.getButton(id);

                        if (b != null && b.isEnabled()) {
                            // This opens dialog which is blocking from some reason :/
                            //b.click();

                             /*  - TODO -

                                 UI Automation blocks when clicking button that opens Dialog...
                                 Not able to find a proper solution so the "temporary" solution
                                 at this time, the work-around is to invoke the control manually.

                                 And even so I'd prefer to use SendMessage but I can't get it to work?
                                 Additionally SendInput is an option but the results are inconsistent?
                                 Furthermore I've jerry-rigged a solution by temporarily blocking user
                                 input and SetCursorPos + SendInput


                            //final WinDef.LPARAM lParam = makeLParam(0, 0);
                            // final HWND hwnd = open.getNativeWindowHandle();
                            //User32.INSTANCE.SendMessage(hwnd, 0x0201, null, lParam);
                            //User32.INSTANCE.SendMessage(hwnd, 0x0202, null, lParam);
                            */

                            // Disable mouse + cache position
                            CWX.windowFinder.projectButton = b;

                            if (!CWX.mouseHook.started) {
                                CWX.mouseHook.hook();
                                freezeMouse = true;
                            } else if (CWX.mouseHook.running) {
                                Thread.sleep(50);
                                b.focus();

                                final Rectangle bounds = b.getBoundingRectangle().toRectangle();
                                final Point currentMousePos = MouseInfo.getPointerInfo().getLocation();

                                // Mouse to button
                                User32.INSTANCE.SetCursorPos((bounds.x & 0xFFFF), (bounds.y & 0xFFFF));

                                // Click open
                                // (Double-click ensures success - if user is holding LM when block starts single click does not invoke button)
                                for (int clicks = 0; clicks < 2; clicks++) {
                                    mouseAction(2);
                                    mouseAction(4);
                                }

                                // Restore mouse position
                                User32.INSTANCE.SetCursorPos((currentMousePos.x & 0xFFFF), (currentMousePos.y & 0xFFFF));
                                return true;
                            }

                            return false;
                        }
                    }

                    break;
            }
        } catch (AutomationException | InterruptedException ignored) {
            //TODO - handle or something
        }
        return false;
    }

    private static void mouseAction(int flags) {
        WinUser.INPUT input = new WinUser.INPUT();

        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_MOUSE);
        input.input.setType("mi");

        input.input.mi.dx = new WinDef.LONG(0);
        input.input.mi.dy = new WinDef.LONG(0);

        input.input.mi.time = new WinDef.DWORD(0);
        input.input.mi.dwExtraInfo = new BaseTSD.ULONG_PTR(0);
        input.input.mi.dwFlags = new WinDef.DWORD(flags);
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), new WinUser.INPUT[]{input}, input.size());
    }
}
