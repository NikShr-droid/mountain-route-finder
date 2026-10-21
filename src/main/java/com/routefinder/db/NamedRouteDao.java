package com.routefinder.db;

import com.routefinder.exception.DataAccessException;
import com.routefinder.model.NamedRoute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for `named_routes`.
 *
 * <p>Supports querying hand-curated classic named routes (e.g. "Machame Route", "Marangu Route")
 * that climbers can toggle and compare alongside Dijkstra-computed optimal paths.</p>
 */
public class NamedRouteDao {

    private final Connection connection;

    public NamedRouteDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Fetches all named classic routes associated with the given mountain.
     */
    public List<NamedRoute> getNamedRoutesForMountain(int mountainId) {
        String sql = "SELECT id, mountain_id, name, description FROM named_routes WHERE mountain_id = ? ORDER BY id";
        List<NamedRoute> routes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mountainId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(new NamedRoute(
                            rs.getInt("id"),
                            rs.getInt("mountain_id"),
                            rs.getString("name"),
                            rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch named routes for mountain " + mountainId, e);
        }
        return routes;
    }
}
