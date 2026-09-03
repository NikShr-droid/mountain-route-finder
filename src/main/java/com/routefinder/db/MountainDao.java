package com.routefinder.db;

import com.routefinder.model.Mountain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for `mountains`. Only this class contains SQL/JDBC
 * for that table -- UI and algorithm code call these methods and get
 * back Mountain objects.
 */
public class MountainDao {

    private final Connection connection;

    public MountainDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public List<Mountain> getAllMountains() {
        String sql = "SELECT id, name, region, elevation_m, difficulty, season, description " +
                "FROM mountains ORDER BY name";
        List<Mountain> mountains = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mountains.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch mountains", e);
        }
        return mountains;
    }

    public Mountain getById(int id) {
        String sql = "SELECT id, name, region, elevation_m, difficulty, season, description " +
                "FROM mountains WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch mountain " + id, e);
        }
    }

    private Mountain mapRow(ResultSet rs) throws SQLException {
        return new Mountain(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("region"),
                rs.getInt("elevation_m"),
                rs.getString("difficulty"),
                rs.getString("season"),
                rs.getString("description")
        );
    }
}
