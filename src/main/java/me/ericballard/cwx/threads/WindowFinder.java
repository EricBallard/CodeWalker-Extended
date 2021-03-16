package me.ericballard.cwx.threads;

import me.ericballard.cwx.machine.Apps;

import java.text.DecimalFormat;
import java.time.Instant;

public class WindowFinder extends Thread {

    final static DecimalFormat df = new DecimalFormat("##.##");

    private static boolean foundWalker, foundEditor ;

    private static long startTime;

    @Override
    public void run() {
        // Init time elapsed
        startTime = Instant.now().toEpochMilli();

        // While app hasn't been closed
        while (!isInterrupted()) {
            // Look for window handles
            if (!foundWalker) {
                if (foundWalker = (Apps.getWindow(true) != null))
                    continue;
            } else if (!foundEditor) {
                if (foundEditor = (Apps.getWindow(false) != null))
                    continue;
            } else {
                // Found all the windows - time to get hacky
                final double timeElapsed = (Instant.now().toEpochMilli() - startTime) / 1000.0;
                System.out.println("CWX | Identified CodeWalker and Editor in " + df.format(timeElapsed) + "s");
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
