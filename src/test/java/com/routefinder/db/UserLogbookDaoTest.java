package com.routefinder.db;

import com.routefinder.model.CostFunction;
import com.routefinder.model.SavedRoute;
import com.routefinder.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests verifying complete CRUD operations, database persistence,
 * and domain model validation for climber profiles and the mountaineering logbook.
 */
public class UserLogbookDaoTest {

    private UserDao userDao;
    private SavedRouteDao savedRouteDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        savedRouteDao = new SavedRouteDao();
    }

    @Test
    void userModelValidatesFields() {
        assertThrows(IllegalArgumentException.class, () -> new User(1, "", "Display Name"));
        assertThrows(IllegalArgumentException.class, () -> new User(1, "username", "  "));
        assertThrows(IllegalArgumentException.class, () -> new User(1, null, "Display Name"));
    }

    @Test
    void savedRouteModelValidatesFields() {
        assertThrows(IllegalArgumentException.class, () ->
                new SavedRoute(0, 1, "Kilimanjaro", 5895, CostFunction.DISTANCE, SavedRoute.Status.SAVED, false, "Notes"));

        assertThrows(IllegalArgumentException.class, () ->
                new SavedRoute(1, 0, "Kilimanjaro", 5895, CostFunction.DISTANCE, SavedRoute.Status.SAVED, false, "Notes"));

        assertThrows(IllegalArgumentException.class, () ->
                new SavedRoute(1, 1, "Kilimanjaro", 5895, null, SavedRoute.Status.SAVED, false, "Notes"));

        assertThrows(IllegalArgumentException.class, () ->
                new SavedRoute(1, 1, "Kilimanjaro", 5895, CostFunction.DISTANCE, null, false, "Notes"));
    }

    @Test
    void userCrudOperationsPersistCorrectly() {
        String testUsername = "climber_test_" + System.currentTimeMillis();
        User created = userDao.createUser(testUsername, "Test Mountaineer");

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(testUsername, created.getUsername());
        assertEquals("Test Mountaineer", created.getDisplayName());

        // Read by ID and by Username
        User byId = userDao.getById(created.getId());
        assertNotNull(byId);
        assertEquals("Test Mountaineer", byId.getDisplayName());

        User byUsername = userDao.getByUsername(testUsername);
        assertNotNull(byUsername);
        assertEquals(created.getId(), byUsername.getId());

        // Delete user
        boolean deleted = userDao.deleteUser(created.getId());
        assertTrue(deleted);
        assertNull(userDao.getById(created.getId()));
    }

    @Test
    void logbookCrudOperationsAndStatusUpdates() {
        String testUsername = "logbook_user_" + System.currentTimeMillis();
        User user = userDao.createUser(testUsername, "Expedition Leader");

        // 1. Create (Save route)
        SavedRoute saved = savedRouteDao.saveRoute(
                user.getId(), 1, CostFunction.DISTANCE, SavedRoute.Status.SAVED, false, "Planning for next July");
        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
        assertEquals("Kilimanjaro", saved.getMountainName());
        assertEquals(CostFunction.DISTANCE, saved.getCostFunction());
        assertEquals(SavedRoute.Status.SAVED, saved.getStatus());
        assertFalse(saved.isLiked());

        // 2. Read (Query routes for user)
        List<SavedRoute> routes = savedRouteDao.getRoutesForUser(user.getId());
        assertEquals(1, routes.size());
        assertEquals("Planning for next July", routes.get(0).getNotes());

        // 3. Update Status (Mark as completed)
        boolean statusUpdated = savedRouteDao.updateStatus(saved.getId(), SavedRoute.Status.COMPLETED);
        assertTrue(statusUpdated);

        SavedRoute updated = savedRouteDao.getById(saved.getId());
        assertEquals(SavedRoute.Status.COMPLETED, updated.getStatus());
        assertTrue(updated.isCompleted());

        // 4. Update Notes & Like
        assertTrue(savedRouteDao.updateNotes(saved.getId(), "Summited Uhuru Peak in 6 days!"));
        assertTrue(savedRouteDao.toggleLiked(saved.getId(), true));

        SavedRoute likedRoute = savedRouteDao.getById(saved.getId());
        assertTrue(likedRoute.isLiked());
        assertEquals("Summited Uhuru Peak in 6 days!", likedRoute.getNotes());

        List<SavedRoute> likedList = savedRouteDao.getLikedRoutes(user.getId());
        assertEquals(1, likedList.size());

        // 5. Delete route
        assertTrue(savedRouteDao.deleteRoute(saved.getId()));
        assertNull(savedRouteDao.getById(saved.getId()));

        // Cleanup user
        userDao.deleteUser(user.getId());
    }

    @Test
    void foreignKeyCascadeDeletesRoutesWhenUserDeleted() {
        String testUsername = "cascade_user_" + System.currentTimeMillis();
        User user = userDao.createUser(testUsername, "Cascade Climber");

        SavedRoute route = savedRouteDao.saveRoute(
                user.getId(), 2, CostFunction.SAFETY, SavedRoute.Status.SAVED, true, "Mont Blanc attempt");
        assertNotNull(route);

        // Deleting user must cascade delete route entries
        userDao.deleteUser(user.getId());
        assertNull(savedRouteDao.getById(route.getId()), "Route must be deleted on user cascade");
    }
}
