package com.routefinder.model;

/**
 * Domain object for a hand-curated "classic" named route (e.g. "Machame
 * Route"), mirroring the `named_routes` table. Stretch-goal feature.
 */
public class NamedRoute {

    private final int id;
    private final int mountainId;
    private final String name;
    private final String description;

    public NamedRoute(int id, int mountainId, String name, String description) {
        this.id = id;
        this.mountainId = mountainId;
        this.name = name;
        this.description = description;
    }

    public int getId() { return id; }
    public int getMountainId() { return mountainId; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return name;
    }
}
