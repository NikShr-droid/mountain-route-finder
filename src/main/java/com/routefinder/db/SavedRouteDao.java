package com.routefinder.db;

import com.routefinder.exception.DataAccessException;
import com.routefinder.model.CostFunction;
import com.routefinder.model.SavedRoute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for `user_routes` table.
 *
 * <p>Provides full CRUD persistence for personal saved routes, wishlist expeditions,
 * and completed summits logged by climbers.</p>
 */
public class SavedRouteDao {

    private final Connection connection;

    public SavedRouteDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Retrieves all logged routes for a given user, joined with mountain specifications.
     */
    public List<SavedRoute> getRoutesForUser(int userId) {
        String sql = """
            SELECT ur.id, ur.user_id, ur.mountain_id, m.name AS mountain_name, m.elevation_m,
                   ur.cost_function, ur.status, ur.is_liked, ur.notes, ur.logged_at
            FROM user_routes ur
            JOIN mountains m ON ur.mountain_id = m.id
            WHERE ur.user_id = ?
            ORDER BY ur.id DESC
        """;
        List<SavedRoute> routes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch routes for user ID " + userId, e);
        }
        return routes;
    }

    /**
     * Retrieves routes for a user filtered by status (SAVED or COMPLETED).
     */
    public List<SavedRoute> getRoutesByStatus(int userId, SavedRoute.Status status) {
        String sql = """
            SELECT ur.id, ur.user_id, ur.mountain_id, m.name AS mountain_name, m.elevation_m,
                   ur.cost_function, ur.status, ur.is_liked, ur.notes, ur.logged_at
            FROM user_routes ur
            JOIN mountains m ON ur.mountain_id = m.id
            WHERE ur.user_id = ? AND ur.status = ?
            ORDER BY ur.id DESC
        """;
        List<SavedRoute> routes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch " + status + " routes for user ID " + userId, e);
        }
        return routes;
    }

    /**
     * Retrieves routes favorited by the user (is_liked = 1).
     */
    public List<SavedRoute> getLikedRoutes(int userId) {
        String sql = """
            SELECT ur.id, ur.user_id, ur.mountain_id, m.name AS mountain_name, m.elevation_m,
                   ur.cost_function, ur.status, ur.is_liked, ur.notes, ur.logged_at
            FROM user_routes ur
            JOIN mountains m ON ur.mountain_id = m.id
            WHERE ur.user_id = ? AND ur.is_liked = 1
            ORDER BY ur.id DESC
        """;
        List<SavedRoute> routes = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch liked routes for user ID " + userId, e);
        }
        return routes;
    }

    /**
     * Checks if a route configuration is already logged for this user.
     */
    public boolean isRouteLogged(int userId, int mountainId, CostFunction costFunction) {
        String sql = "SELECT 1 FROM user_routes WHERE user_id = ? AND mountain_id = ? AND cost_function = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, mountainId);
            stmt.setString(3, costFunction.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check route existence for user ID " + userId, e);
        }
    }

    /**
     * Persists a new route entry in the climber's logbook.
     */
    public SavedRoute saveRoute(int userId, int mountainId, CostFunction costFunction,
                                SavedRoute.Status status, boolean isLiked, String notes) {
        String sql = """
            INSERT INTO user_routes (user_id, mountain_id, cost_function, status, is_liked, notes)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, mountainId);
            stmt.setString(3, costFunction.name());
            stmt.setString(4, status.name());
            stmt.setInt(5, isLiked ? 1 : 0);
            stmt.setString(6, notes != null ? notes.trim() : "");
            stmt.executeUpdate();

            int id = 0;
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }

            // Return hydrated model
            return getById(id);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to log route for mountain " + mountainId, e);
        }
    }

    public SavedRoute getById(int id) {
        String sql = """
            SELECT ur.id, ur.user_id, ur.mountain_id, m.name AS mountain_name, m.elevation_m,
                   ur.cost_function, ur.status, ur.is_liked, ur.notes, ur.logged_at
            FROM user_routes ur
            JOIN mountains m ON ur.mountain_id = m.id
            WHERE ur.id = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch saved route with ID " + id, e);
        }
    }

    public boolean updateStatus(int routeId, SavedRoute.Status status) {
        String sql = "UPDATE user_routes SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, routeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update status for route ID " + routeId, e);
        }
    }

    public boolean updateNotes(int routeId, String notes) {
        String sql = "UPDATE user_routes SET notes = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, notes != null ? notes.trim() : "");
            stmt.setInt(2, routeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update notes for route ID " + routeId, e);
        }
    }

    public boolean toggleLiked(int routeId, boolean isLiked) {
        String sql = "UPDATE user_routes SET is_liked = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, isLiked ? 1 : 0);
            stmt.setInt(2, routeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to toggle like for route ID " + routeId, e);
        }
    }

    public boolean deleteRoute(int routeId) {
        String sql = "DELETE FROM user_routes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, routeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete route entry ID " + routeId, e);
        }
    }

    private SavedRoute mapRow(ResultSet rs) throws SQLException {
        CostFunction cf = CostFunction.valueOf(rs.getString("cost_function"));
        SavedRoute.Status status = SavedRoute.Status.valueOf(rs.getString("status"));
        boolean liked = rs.getInt("is_liked") == 1;

        return new SavedRoute(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("mountain_id"),
                rs.getString("mountain_name"),
                rs.getInt("elevation_m"),
                cf,
                status,
                liked,
                rs.getString("notes"),
                rs.getString("logged_at")
        );
    }
}
