package com.routefinder.db;

import com.routefinder.exception.DataAccessException;
import com.routefinder.exception.UnseededMountainException;
import com.routefinder.model.Mountain;
import com.routefinder.model.Waypoint;
import com.routefinder.model.WaypointFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Access Object for `waypoints`.
 */
public class WaypointDao {

    private final Connection connection;

    public WaypointDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public List<Waypoint> getWaypointsForMountain(int mountainId) {
        String sql = "SELECT id, mountain_id, name, waypoint_type, latitude, longitude, elevation_m " +
                "FROM waypoints WHERE mountain_id = ?";
        List<Waypoint> waypoints = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mountainId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    waypoints.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch waypoints for mountain " + mountainId, e);
        }
        return waypoints;
    }

    public Waypoint getById(int id) {
        String sql = "SELECT id, mountain_id, name, waypoint_type, latitude, longitude, elevation_m " +
                "FROM waypoints WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch waypoint " + id, e);
        }
    }

    /**
     * Retrieves the distinct mountain IDs that have seeded waypoints and trail data.
     *
     * @return a Set of mountain IDs with seeded route data
     */
    public Set<Integer> getSeededMountainIds() {
        String sql = "SELECT DISTINCT mountain_id FROM waypoints";
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("mountain_id"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch seeded mountain IDs", e);
        }
        return ids;
    }

    /**
     * Checks whether a specific mountain has seeded waypoint and route data.
     *
     * @param mountainId the ID of the mountain to check
     * @return true if at least one waypoint exists for this mountain
     */
    public boolean hasRouteData(int mountainId) {
        String sql = "SELECT 1 FROM waypoints WHERE mountain_id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mountainId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to verify route data for mountain " + mountainId, e);
        }
    }

    /**
     * Asserts that route data exists for the given mountain; throws {@link UnseededMountainException}
     * if no waypoints or route segments have been seeded yet.
     *
     * @param mountain the mountain entity to validate
     * @throws UnseededMountainException if the mountain has no seeded waypoints
     */
    public void validateMountainSeeded(Mountain mountain) {
        if (mountain == null) {
            throw new UnseededMountainException(-1, "null");
        }
        if (!hasRouteData(mountain.getId())) {
            throw new UnseededMountainException(mountain.getId(), mountain.getName());
        }
    }

    private Waypoint mapRow(ResultSet rs) throws SQLException {
        return WaypointFactory.create(
                rs.getInt("id"),
                rs.getInt("mountain_id"),
                rs.getString("name"),
                rs.getString("waypoint_type"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getInt("elevation_m")
        );
    }
}
