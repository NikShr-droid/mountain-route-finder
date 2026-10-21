package com.routefinder.model;

/**
 * Domain entity representing a personal route entry in a climber's mountaineering logbook.
 *
 * <p>Tracks planned wishlist expeditions ({@link Status#SAVED}) and conquered
 * peaks ({@link Status#COMPLETED}), alongside favorite likes and custom expedition notes.</p>
 *
 * <p><b>OOP Note (Encapsulation):</b>
 * Immutable domain model with private final fields, strict validation, and constructor overloading.</p>
 */
public class SavedRoute {

    public enum Status {
        SAVED("📌 Planned"),
        COMPLETED("🏔️ Summited");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final int id;
    private final int userId;
    private final int mountainId;
    private final String mountainName;
    private final int mountainElevationM;
    private final CostFunction costFunction;
    private final Status status;
    private final boolean liked;
    private final String notes;
    private final String loggedAt;

    public SavedRoute(int id, int userId, int mountainId, String mountainName, int mountainElevationM,
                      CostFunction costFunction, Status status, boolean liked, String notes, String loggedAt) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive: " + userId);
        }
        if (mountainId <= 0) {
            throw new IllegalArgumentException("Mountain ID must be positive: " + mountainId);
        }
        if (costFunction == null) {
            throw new IllegalArgumentException("Cost function cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Route status cannot be null");
        }

        this.id = id;
        this.userId = userId;
        this.mountainId = mountainId;
        this.mountainName = (mountainName != null && !mountainName.isBlank()) ? mountainName : "Mountain #" + mountainId;
        this.mountainElevationM = mountainElevationM;
        this.costFunction = costFunction;
        this.status = status;
        this.liked = liked;
        this.notes = notes != null ? notes.trim() : "";
        this.loggedAt = loggedAt != null ? loggedAt : "Recently";
    }

    /**
     * Overloaded constructor for saving a new route entry before database ID assignment.
     */
    public SavedRoute(int userId, int mountainId, String mountainName, int mountainElevationM,
                      CostFunction costFunction, Status status, boolean liked, String notes) {
        this(0, userId, mountainId, mountainName, mountainElevationM, costFunction, status, liked, notes, null);
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getMountainId() {
        return mountainId;
    }

    public String getMountainName() {
        return mountainName;
    }

    public int getMountainElevationM() {
        return mountainElevationM;
    }

    public CostFunction getCostFunction() {
        return costFunction;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isLiked() {
        return liked;
    }

    public String getNotes() {
        return notes;
    }

    public String getLoggedAt() {
        return loggedAt;
    }

    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    public boolean isSaved() {
        return status == Status.SAVED;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", mountainName, costFunction.name(), status.name());
    }
}
