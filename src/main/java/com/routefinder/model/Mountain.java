package com.routefinder.model;

/**
 * Domain object for a mountain, mirroring the `mountains` table.
 */
public class Mountain {

    private final int id;
    private final String name;
    private final String region;
    private final int elevationM;
    private final String difficulty;
    private final String season;
    private final String description;

    public Mountain(int id, String name, String region, int elevationM,
                     String difficulty, String season, String description) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.elevationM = elevationM;
        this.difficulty = difficulty;
        this.season = season;
        this.description = description;
    }

    /** Convenience constructor for quick placeholder use (e.g. before DB wiring). */
    public Mountain(int id, String name) {
        this(id, name, null, 0, null, null, null);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRegion() { return region; }
    public int getElevationM() { return elevationM; }
    public String getDifficulty() { return difficulty; }
    public String getSeason() { return season; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return name; // ComboBox/TableView display
    }
}
