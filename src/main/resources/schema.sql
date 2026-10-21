-- Mountain Route-Finder database schema
-- SQLite. Five tables: mountains, waypoints, route_segments, named_routes, named_route_segments.
--
-- Design notes:
--  - route_segments are DIRECTED (from lower waypoint to higher), matching
--    ascent-only trailhead -> summit routing. No need to model descent edges.
--  - geometry_json stores an ordered list of [lat, lng] points as JSON text,
--    used to draw the organic Strava-style trail line for a segment.
--  - safety_score is hand-curated (1 = safest, 5 = most dangerous), not derived.
--  - mountains.image_file is just a filename inside src/main/resources/images/mountains/

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------------
CREATE TABLE mountains (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    region      TEXT,
    elevation_m INTEGER NOT NULL,
    difficulty  TEXT    NOT NULL CHECK (difficulty IN ('easy', 'moderate', 'hard', 'extreme')),
    season      TEXT,                       -- e.g. "May-Sep"
    description TEXT,
    image_file  TEXT                        -- e.g. "kilimanjaro.jpg" (NULL -> placeholder in UI)
);

-- ---------------------------------------------------------------------------
CREATE TABLE waypoints (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    mountain_id    INTEGER NOT NULL REFERENCES mountains(id) ON DELETE CASCADE,
    name           TEXT    NOT NULL,
    waypoint_type  TEXT    NOT NULL CHECK (waypoint_type IN
                       ('trailhead', 'camp', 'checkpoint', 'junction', 'summit')),
    latitude       REAL    NOT NULL,
    longitude      REAL    NOT NULL,
    elevation_m    INTEGER NOT NULL
);

CREATE INDEX idx_waypoints_mountain ON waypoints(mountain_id);

-- ---------------------------------------------------------------------------
-- Directed edge: from_waypoint -> to_waypoint, uphill direction only.
CREATE TABLE route_segments (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    mountain_id        INTEGER NOT NULL REFERENCES mountains(id)  ON DELETE CASCADE,
    from_waypoint_id   INTEGER NOT NULL REFERENCES waypoints(id)  ON DELETE CASCADE,
    to_waypoint_id     INTEGER NOT NULL REFERENCES waypoints(id)  ON DELETE CASCADE,
    distance_km        REAL    NOT NULL,
    elevation_gain_m   INTEGER NOT NULL,
    safety_score       INTEGER NOT NULL CHECK (safety_score BETWEEN 1 AND 5),  -- 1 = safest
    estimated_time_min INTEGER,
    geometry_json       TEXT   NOT NULL,   -- JSON array of [lat, lng] points, from_waypoint -> to_waypoint
    CHECK (from_waypoint_id != to_waypoint_id)
);

CREATE INDEX idx_segments_mountain ON route_segments(mountain_id);
CREATE INDEX idx_segments_from     ON route_segments(from_waypoint_id);
CREATE INDEX idx_segments_to       ON route_segments(to_waypoint_id);

-- ---------------------------------------------------------------------------
-- A hand-curated, named "classic" route (e.g. "Machame Route") made up of
-- an ordered sequence of route_segments. Stretch feature: toggle these on
-- the map alongside the Dijkstra-computed optimal route.
CREATE TABLE named_routes (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    mountain_id INTEGER NOT NULL REFERENCES mountains(id) ON DELETE CASCADE,
    name        TEXT    NOT NULL,
    description TEXT
);

CREATE INDEX idx_named_routes_mountain ON named_routes(mountain_id);

-- ---------------------------------------------------------------------------
-- Join table: which route_segments make up a named_route, in what order.
CREATE TABLE named_route_segments (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    named_route_id   INTEGER NOT NULL REFERENCES named_routes(id)   ON DELETE CASCADE,
    route_segment_id INTEGER NOT NULL REFERENCES route_segments(id) ON DELETE CASCADE,
    sequence_order   INTEGER NOT NULL,      -- 1, 2, 3... position within the route
    UNIQUE (named_route_id, sequence_order)
);

CREATE INDEX idx_nrs_named_route ON named_route_segments(named_route_id);

-- ---------------------------------------------------------------------------
-- User profiles and personal mountaineering logbook
CREATE TABLE users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT    UNIQUE NOT NULL,
    display_name TEXT   NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- Saved, liked, and completed routes logged by users
CREATE TABLE user_routes (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mountain_id   INTEGER NOT NULL REFERENCES mountains(id) ON DELETE CASCADE,
    cost_function TEXT    NOT NULL CHECK (cost_function IN ('DISTANCE', 'ELEVATION_GAIN', 'SAFETY')),
    status        TEXT    NOT NULL CHECK (status IN ('SAVED', 'COMPLETED')),
    is_liked      INTEGER NOT NULL DEFAULT 0 CHECK (is_liked IN (0, 1)),
    notes         TEXT,
    logged_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_routes_user     ON user_routes(user_id);
CREATE INDEX idx_user_routes_mountain ON user_routes(mountain_id);