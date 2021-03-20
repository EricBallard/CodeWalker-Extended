package me.ericballard.cwx.machine.apps;

import com.sun.jna.platform.win32.User32;
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

    private static ToolBar toolbar = null;

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
                                 input and SetCursorPos


                            //final WinDef.LPARAM lParam = makeLParam(0, 0);
                            // final HWND hwnd = open.getNativeWindowHandle();
                            //User32.INSTANCE.SendMessage(hwnd, 0x0201, null, lParam);
                            //User32.INSTANCE.SendMessage(hwnd, 0x0202, null, lParam);
                            */

                            final Rectangle bounds = b.getBoundingRectangle().toRectangle();
                            final Point currentMousePos = MouseInfo.getPointerInfo().getLocation();

                            // Action
                            try {
                                CWX.blockMouseInput = true;
                                Thread.sleep(50);

                                User32.INSTANCE.SetCursorPos((bounds.x & 0xFFFF), (bounds.y & 0xFFFF));
                                Thread.sleep(25);

                                //Mouse.mouseLeftClick(0, 0);
                                Thread.sleep(25);

                                User32.INSTANCE.SetCursorPos((currentMousePos.x & 0xFFFF), (currentMousePos.y & 0xFFFF));
                                CWX.blockMouseInput = false;
                            } catch (InterruptedException ignored) {
                                // TODO - handle + dialog?
                                return false;
                            }

                            return true;
                        }
                    }

                    break;
            }
        } catch (AutomationException ignored) {
            //TODO - handle or something
        }

        return false;

    }


    /*
      ToolBar toolbar = applicationWindow.getToolBar(Search.getBuilder(0).build());
      logger.info("Toolbar name is " + toolbar.name()); // Blank in default WPF

      ToolBarButton btn0 = toolbar.getToolbarButton(Search.getBuilder(0).build());

      if (btn0.isEnabled()) {
        logger.info("btn0 Enabled");
        logger.info(btn1.name());
        btn1.click();
      }
     */
}
