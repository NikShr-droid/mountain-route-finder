package com.routefinder.db;

import com.routefinder.exception.DataAccessException;
import com.routefinder.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for `users` table.
 *
 * <p>Provides full CRUD operations for climber user profiles.</p>
 */
public class UserDao {

    private final Connection connection;

    public UserDao() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public List<User> getAllUsers() {
        String sql = "SELECT id, username, display_name FROM users ORDER BY id ASC";
        List<User> users = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch all users", e);
        }
        return users;
    }

    public User getById(int id) {
        String sql = "SELECT id, username, display_name FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch user with ID " + id, e);
        }
    }

    public User getByUsername(String username) {
        String sql = "SELECT id, username, display_name FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch user with username " + username, e);
        }
    }

    public User createUser(String username, String displayName) {
        String sql = "INSERT INTO users (username, display_name) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username.trim().toLowerCase());
            stmt.setString(2, displayName.trim());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new User(id, username.trim().toLowerCase(), displayName.trim());
                }
            }
            throw new DataAccessException("Failed to retrieve generated user ID");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user '" + username + "'", e);
        }
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete user with ID " + id, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("display_name")
        );
    }
}
