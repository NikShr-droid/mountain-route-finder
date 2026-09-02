package com.routefinder.model;

/**
 * Domain object for a mountain. Minimal for now — enough for the
 * Route Finder ComboBox to display and select a mountain by name.
 * Will grow (region, elevation, difficulty, season) once BrowseView
 * and MountainDao are built.
 */
public class Mountain {

    private final int id;
    private final String name;

    public Mountain(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        // ComboBox displays this by default
        return name;
    }
}
