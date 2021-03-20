package me.ericballard.cwx.machine;


import mmarquee.automation.UIAutomation;

public class Input {

    private UIAutomation automation;

    private void init() {
        automation = UIAutomation.getInstance();

        // Build the application details up, ready for launching
//        Application application =
//                new Application(
//                        new ElementBuilder()
//                                .automation(automation)
//                                .applicationPath("apps\\Project1.exe"));

    }

}
