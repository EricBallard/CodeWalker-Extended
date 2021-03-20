package me.ericballard.cwx.machine;

import mmarquee.automation.AutomationException;
import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.*;

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

                        if (b != null &&b.isEnabled()) {
                            b.click();
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
