package com.example.ims001;

import javafx.scene.Scene;

public class ThemeManager {

    private static boolean darkMode = true; // default (change if you want)

    private ThemeManager() {}

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
    }

    public static void apply(Scene scene) {
        if (scene == null) return;

        scene.getStylesheets().clear();

        String css = darkMode ? "/dark.css" : "/light.css";
        var url = ThemeManager.class.getResource(css);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        }
    }

    public static void toggle(Scene scene) {
        darkMode = !darkMode;
        apply(scene);
    }
}
