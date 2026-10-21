package com.routefinder.ui;

import javafx.scene.Scene;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages alpine-inspired visual themes across the application.
 *
 * <ul>
 *   <li><b>Alpine Light ("Alpine Snow & Glacial Ice"):</b>
 *       Clean snow-white surfaces, crisp ice-blue accents (#0284c7), and cool mountain stone typography.</li>
 *   <li><b>Alpine Dark ("Midnight Peak & Deep Alpine Blues"):</b>
 *       Deep starlit midnight blues (#070b14, #111928), dark granite shadows, and glowing glacial cyan (#38bdf8).</li>
 * </ul>
 */
public final class ThemeManager {

    public enum Theme {
        LIGHT("☀️ Alpine Snow", "/styles/alpine-light.css"),
        DARK("🌙 Midnight Sky", "/styles/alpine-dark.css");

        private final String displayName;
        private final String stylesheetPath;

        Theme(String displayName, String stylesheetPath) {
            this.displayName = displayName;
            this.stylesheetPath = stylesheetPath;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getStylesheetPath() {
            return stylesheetPath;
        }
    }

    private static Theme currentTheme = Theme.LIGHT;
    private static final List<Consumer<Theme>> listeners = new ArrayList<>();

    private ThemeManager() {}

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static boolean isDark() {
        return currentTheme == Theme.DARK;
    }

    public static void addListener(Consumer<Theme> listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(Consumer<Theme> listener) {
        listeners.remove(listener);
    }

    /**
     * Applies the specified theme to the given JavaFX Scene and notifies listeners.
     */
    public static void applyTheme(Scene scene, Theme theme) {
        if (theme == null) {
            return;
        }
        currentTheme = theme;

        if (scene != null) {
            URL url = ThemeManager.class.getResource(theme.getStylesheetPath());
            if (url != null) {
                scene.getStylesheets().setAll(url.toExternalForm());
            }
        }

        for (Consumer<Theme> listener : new ArrayList<>(listeners)) {
            listener.accept(theme);
        }
    }

    /**
     * Toggles between Alpine Light and Alpine Dark mode.
     */
    public static Theme toggleTheme(Scene scene) {
        Theme next = isDark() ? Theme.LIGHT : Theme.DARK;
        applyTheme(scene, next);
        return next;
    }

    // Dynamic Alpine Color Palette Accessors
    public static String getBgPrimary() {
        return isDark() ? "#070b14" : "#f8fafc";
    }

    public static String getBgSurface() {
        return isDark() ? "#111928" : "#ffffff";
    }

    public static String getTextTitle() {
        return isDark() ? "#f8fafc" : "#0f172a";
    }

    public static String getTextBody() {
        return isDark() ? "#cbd5e1" : "#334155";
    }

    public static String getTextMuted() {
        return isDark() ? "#94a3b8" : "#64748b";
    }

    public static String getBorderSubtle() {
        return isDark() ? "#1e293b" : "#e2e8f0";
    }

    public static String getIceAccent() {
        return isDark() ? "#38bdf8" : "#0284c7";
    }
}
