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
    private final String imageFile; // filename in resources/images/mountains/, or null -> placeholder

    public Mountain(int id, String name, String region, int elevationM,
                    String difficulty, String season, String description, String imageFile) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Mountain name cannot be null or blank");
        }
        if (elevationM < 0) {
            throw new IllegalArgumentException("Elevation cannot be negative: " + elevationM);
        }

        this.id = id;
        this.name = name;
        this.region = region;
        this.elevationM = elevationM;
        this.difficulty = difficulty;
        this.season = season;
        this.description = description;
        this.imageFile = imageFile;
    }

    /** Same as above without an image (keeps older call sites compiling). */
    public Mountain(int id, String name, String region, int elevationM,
                    String difficulty, String season, String description) {
        this(id, name, region, elevationM, difficulty, season, description, null);
    }

    /** Convenience constructor for quick placeholder use (e.g. before DB wiring). */
    public Mountain(int id, String name) {
        this(id, name, null, 0, null, null, null, null);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRegion() { return region; }
    public int getElevationM() { return elevationM; }
    public String getDifficulty() { return difficulty; }
    public String getSeason() { return season; }
    public String getDescription() { return description; }
    public String getImageFile() { return imageFile; }

    @Override
    public String toString() {
        return name; // ComboBox/TableView display
    }
}