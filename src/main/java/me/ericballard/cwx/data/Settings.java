package me.ericballard.cwx.data;

import java.awt.*;

public class Settings {
    public boolean openTools, openEditor, enableDLC, saveWindowPosAndSize;

    public long backupInterval, backupExpiration;

    public int numOfDelBackup;

    public Rectangle wBounds, eBounds;

    public Settings() {
        // Defaults
        openTools = true;
        openEditor = true;

        enableDLC = true;
        saveWindowPosAndSize = true;

        numOfDelBackup = 20;

        // Hourly
        backupInterval = 3600000L;

        // 1 Week
        backupExpiration = 604800000L;
    }
}
