package me.ericballard.cwx.threads;

import me.ericballard.cwx.data.Data;
import me.ericballard.cwx.data.Settings;
import me.ericballard.cwx.machine.Apps;
import me.ericballard.cwx.machine.Automation;

public class AutoInteract extends Thread {

    // Handles interacting with external GUIs via Win32 UIAutomation lib/java wrapper
    private boolean enabledDLC, openedProject, openedEditor, openedTools;


    @Override
    public void run() {

        while (!Thread.interrupted()) {

            try {
                final Settings settings = Data.settings;

                if (!openedTools) {
                    openedTools = true;

                    // User disabled
                    if (!settings.openTools) {
                        openedEditor = true;
                        enabledDLC = true;
                        continue;
                    }

                    boolean success;

                    // Open Tools Panel
                    success = Automation.interact(Apps.getWindow(true), Automation.ControlType.BUTTON, "ToolsPanelShowButton");
                    System.out.println("CWX | Opened Panel: " + success);

                    // Open Tool Bar
                    //TODO Press 'T' old version does not have this ui box

                    //success = Automation.interact(Apps.getWindow(true), Automation.ControlType.CHECKBOX, "ShowToolbarCheckBox");
                    //System.out.println("CWX | Opened Toolbar: " + success);

                    // Select "Selection" Tab
                    success = Automation.interact(Apps.getWindow(true), Automation.ControlType.TAB, "Selection");
                    System.out.println("CWX | Opened Selection Tab: " + success);

                    // Enable Mouse Selection
                    success = Automation.interact(Apps.getWindow(true), Automation.ControlType.CHECKBOX, "MouseSelectCheckBox");
                    System.out.println("CWX | Enabled Selection: " + success);

                    // Select "View" Tab - Need to Enable DLC
                    success = Automation.interact(Apps.getWindow(true), Automation.ControlType.TAB, "View");
                    System.out.println("CWX | Opened View Tab: " + success);
                } else {
                    if (!enabledDLC) {
                        enabledDLC = Automation.interact(Apps.getWindow(true), Automation.ControlType.CHECKBOX, "EnableDlcCheckBox");
                        System.out.println("CWX | Enabled DLC: " + enabledDLC);

                        Thread.sleep(2500);
                    } else if (!openedEditor) {
                        openedEditor = Automation.interact(Apps.getWindow(true), Automation.ControlType.TOOLBAR_BUTTON, "Open...");
                        System.out.println("CWX | Opened Editor: " + openedEditor);

                        Thread.sleep(2500);
                    } else if (!openedProject) {
                        //TODO
                        
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }

    }

}
