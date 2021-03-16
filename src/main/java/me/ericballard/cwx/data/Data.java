package me.ericballard.cwx.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ericballard.cwx.gui.Controller;

import javax.swing.*;
import java.io.*;

public class Data {

    public static final String path = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + File.separator + "CWX" + File.separator;

    public static Settings settings;

    public static void save() {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting();
            Gson gson = gsonBuilder.create();

            Writer writer = new FileWriter(path + "settings.json");
            gson.toJson(settings, writer);

            writer.flush();
            writer.close();

            System.out.println("CWX | Saved data!");
        } catch (Exception e) {
            System.out.println("CWX | Failed to save data due to: " + e.getMessage());
        }
    }

    public static void load(Controller controller) {
        try {
            File file = new File(path + "settings.json");

            if (!file.exists()) {
                boolean success = new File(path).mkdirs();
                System.out.println("CWX | Creating directory for data file (" + success + ")");
                settings = new Settings();
                return;
            }

            // Parse from file
            Gson gson = new GsonBuilder().create();
            FileReader reader = new FileReader(file);
            settings = gson.fromJson(new BufferedReader(reader), Settings.class);
            reader.close();

            if (settings != null) {
                // Apply settings to GUI
                System.out.println("Successfully loaded user data!");

                //TODO
                return;
            } else {
                System.out.println("CWX | Detected saved settings are CORRUPT - oops!");
                settings = new Settings();
            }
        } catch (Exception e) {
            System.out.println("CWX | Failed to load data due to: " + e.getMessage());
        }
    }
}
