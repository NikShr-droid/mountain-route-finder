package com.routefinder.db;

import com.routefinder.model.RouteSegment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for `route_segments`.
 */
public class RouteSegmentDao {

    private final Connection connection;

    public RouteSegmentDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public List<RouteSegment> getSegmentsForMountain(int mountainId) {
        String sql = "SELECT id, mountain_id, from_waypoint_id, to_waypoint_id, distance_km, " +
                "elevation_gain_m, safety_score, estimated_time_min, geometry_json " +
                "FROM route_segments WHERE mountain_id = ?";
        List<RouteSegment> segments = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mountainId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    segments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch route segments for mountain " + mountainId, e);
        }
        return segments;
    }

    private RouteSegment mapRow(ResultSet rs) throws SQLException {
        int estimatedTimeMin = rs.getInt("estimated_time_min");
        Integer estimatedTimeMinBoxed = rs.wasNull() ? null : estimatedTimeMin;
        return new RouteSegment(
                rs.getInt("id"),
                rs.getInt("mountain_id"),
                rs.getInt("from_waypoint_id"),
                rs.getInt("to_waypoint_id"),
                rs.getDouble("distance_km"),
                rs.getInt("elevation_gain_m"),
                rs.getInt("safety_score"),
                estimatedTimeMinBoxed,
                rs.getString("geometry_json")
        );
    }
}
