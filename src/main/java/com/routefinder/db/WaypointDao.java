package com.routefinder.db;

import com.routefinder.model.Waypoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            throw new RuntimeException("Failed to fetch waypoints for mountain " + mountainId, e);
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
            throw new RuntimeException("Failed to fetch waypoint " + id, e);
        }
    }

    private Waypoint mapRow(ResultSet rs) throws SQLException {
        return new Waypoint(
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
