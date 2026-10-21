package com.routefinder.exception;

import com.routefinder.db.NamedRouteDao;
import com.routefinder.db.WaypointDao;
import com.routefinder.model.Mountain;
import com.routefinder.model.NamedRoute;
import com.routefinder.ui.AlertHelper;
import com.routefinder.ui.MountainImages;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests verifying robust exception handling, image fallback behavior,
 * and future feature guards.
 */
public class ExceptionAndFallbackTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Test
    void missingImageFallsBackGracefullyWithoutCrashing() {
        Mountain phantomMountain = new Mountain(
                9999,
                "Phantom Peak",
                "Nowhere",
                4500,
                "hard",
                "May-Sep",
                "A peak with a deliberately non-existent image file",
                "completely-missing-image-12345.jpg"
        );

        // Verify that loading never throws unhandled exceptions
        Image image = assertDoesNotThrow(() ->
                MountainImages.load(phantomMountain, 200, 150));

        assertNotNull(image, "Fallback mechanism must return a valid non-null Image");

        ImageView view = assertDoesNotThrow(() ->
                MountainImages.view(phantomMountain, 200, 150));

        assertNotNull(view);
        assertNotNull(view.getImage());
    }

    @Test
    void nullMountainYieldsValidFallbackGraphic() {
        Image image = assertDoesNotThrow(() ->
                MountainImages.load(null, 150, 100));

        assertNotNull(image);
    }

    @Test
    void imageLoadExceptionStoresContextDetails() {
        ImageLoadException ex = new ImageLoadException("bad.png", "File corrupted");
        assertEquals("bad.png", ex.getImageName());
        assertTrue(ex.getMessage().contains("bad.png"));
        assertTrue(ex.getMessage().contains("File corrupted"));
    }

    @Test
    void featureNotImplementedExceptionStoresFeatureContext() {
        FeatureNotImplementedException ex = new FeatureNotImplementedException("Weather Telemetry", "Requires sensor API");
        assertEquals("Weather Telemetry", ex.getFeatureName());
        assertTrue(ex.getMessage().contains("Weather Telemetry"));
    }

    @Test
    void namedRouteDaoQueriesWithoutErrors() {
        NamedRouteDao dao = new NamedRouteDao();
        List<NamedRoute> routes = assertDoesNotThrow(() -> dao.getNamedRoutesForMountain(1));
        assertNotNull(routes);
    }

    @Test
    void unseededMountainExceptionStoresContextDetails() {
        UnseededMountainException ex = new UnseededMountainException(4, "Mount Everest");
        assertEquals(4, ex.getMountainId());
        assertEquals("Mount Everest", ex.getMountainName());
        assertTrue(ex.getMessage().contains("Mount Everest"));
        assertTrue(ex.getMessage().contains("#4"));
    }

    @Test
    void waypointDaoDetectsSeededAndUnseededMountains() {
        WaypointDao dao = new WaypointDao();

        // Verify seeded hero mountains (IDs 1, 2, 3)
        assertTrue(dao.hasRouteData(1), "Kilimanjaro must have seeded route data");
        assertTrue(dao.hasRouteData(2), "Mont Blanc must have seeded route data");
        assertTrue(dao.hasRouteData(3), "Mt Fuji must have seeded route data");

        Set<Integer> seededIds = dao.getSeededMountainIds();
        assertTrue(seededIds.contains(1));
        assertTrue(seededIds.contains(2));
        assertTrue(seededIds.contains(3));

        // Verify unseeded peaks return false
        assertFalse(dao.hasRouteData(4), "Mountain ID 4 should be catalog-only");
        assertFalse(dao.hasRouteData(9999), "Non-existent peak must return false");
        assertFalse(seededIds.contains(4));

        // Verify validation method throws UnseededMountainException
        Mountain unseededPeak = new Mountain(4, "Denali", "Alaska", 6190, "extreme", "May-Jul", "Catalog peak", "denali.jpg");
        assertThrows(UnseededMountainException.class, () -> dao.validateMountainSeeded(unseededPeak));

        Mountain seededPeak = new Mountain(1, "Kilimanjaro", "Tanzania", 5895, "moderate", "Dec-Mar", "Seeded peak", "kilimanjaro.jpg");
        assertDoesNotThrow(() -> dao.validateMountainSeeded(seededPeak));
    }

    @Test
    void alertHelperHandlesHeadlessEnvironmentWithoutCrashing() {
        Mountain peak = new Mountain(4, "Denali", "Alaska", 6190, "extreme", "May-Jul", "Catalog peak", "denali.jpg");

        // Alerts called outside FX Application thread must log safely and not throw IllegalStateException
        assertDoesNotThrow(() -> AlertHelper.showUnseededMountainWarning(peak));
        assertDoesNotThrow(() -> AlertHelper.showWarningAlert("Test Title", "Test Header", "Test Content"));
        assertDoesNotThrow(() -> AlertHelper.showErrorAlert("Test Error", "Test Header", "Test Content"));
        assertDoesNotThrow(() -> AlertHelper.showInfoAlert("Test Info", "Test Header", "Test Content"));
    }

    @Test
    void overlayHostRegistrationAndHeadlessSafeInteractions() {
        boolean[] dismissed = new boolean[]{false};
        AlertHelper.OverlayHost testHost = new AlertHelper.OverlayHost() {
            @Override
            public void showOverlay(javafx.scene.Node modalNode, Runnable onDismiss) {}

            @Override
            public void hideOverlay() {
                dismissed[0] = true;
            }

            @Override
            public boolean isOverlayActive() {
                return false;
            }
        };

        AlertHelper.setOverlayHost(testHost);
        AlertHelper.hideCurrentOverlay();
        assertTrue(dismissed[0], "hideCurrentOverlay must delegate to registered OverlayHost");

        // Verify overlay prompts are headless safe and don't throw
        assertDoesNotThrow(() -> AlertHelper.showTextInputOverlay("Title", "Header", "Notes", val -> {}));
        assertDoesNotThrow(() -> AlertHelper.showProfilePickerOverlay(java.util.List.of(), null, u -> {}, name -> {}));

        // Reset host
        AlertHelper.setOverlayHost(null);
    }
}
