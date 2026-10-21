package com.routefinder.model;

import com.routefinder.db.UserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the currently active climber user profile session.
 *
 * <p>Enables seamless profile switching and reactive updates across
 * the sidebar, logbook, and route finder.</p>
 */
public final class UserSession {

    private static User activeUser;
    private static final List<Consumer<User>> listeners = new ArrayList<>();

    private UserSession() {}

    /**
     * Gets the current active user profile, initializing from the database if needed.
     */
    public static synchronized User getActiveUser() {
        if (activeUser == null) {
            UserDao userDao = new UserDao();
            List<User> users = userDao.getAllUsers();
            if (!users.isEmpty()) {
                activeUser = users.get(0);
            } else {
                activeUser = userDao.createUser("climber", "Alpine Explorer");
            }
        }
        return activeUser;
    }

    /**
     * Updates the active user profile and notifies listeners.
     */
    public static synchronized void setActiveUser(User user) {
        if (user != null) {
            activeUser = user;
            for (Consumer<User> listener : new ArrayList<>(listeners)) {
                listener.accept(activeUser);
            }
        }
    }

    public static synchronized void addListener(Consumer<User> listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static synchronized void removeListener(Consumer<User> listener) {
        listeners.remove(listener);
    }
}
