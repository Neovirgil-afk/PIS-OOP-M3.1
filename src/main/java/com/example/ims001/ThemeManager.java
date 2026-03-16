package com.example.ims001;

import javafx.scene.Scene;

import java.util.Objects;

public class ThemeManager {

    private static boolean darkMode = true;

    private static final String BASE_CSS = Objects.requireNonNull(
            ThemeManager.class.getResource("/styles.css")
    ).toExternalForm();

    private static final String DARK_CSS = Objects.requireNonNull(
            ThemeManager.class.getResource("/dark.css")
    ).toExternalForm();

    private static final String LIGHT_CSS = Objects.requireNonNull(
            ThemeManager.class.getResource("/light.css")
    ).toExternalForm();

    private ThemeManager() {
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
    }

    public static void apply(Scene scene) {
        if (scene == null) return;

        // keep base stylesheet
        if (!scene.getStylesheets().contains(BASE_CSS)) {
            scene.getStylesheets().add(BASE_CSS);
        }

        // remove only theme stylesheets
        scene.getStylesheets().remove(DARK_CSS);
        scene.getStylesheets().remove(LIGHT_CSS);

        // add the active theme
        scene.getStylesheets().add(darkMode ? DARK_CSS : LIGHT_CSS);
    }

    public static void toggle(Scene scene) {
        darkMode = !darkMode;
        apply(scene);
    }
}