package com.routefinder.ui;

import com.routefinder.exception.ImageLoadException;
import com.routefinder.model.Mountain;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads, caches, and renders mountain photos from application resources.
 *
 * <p><b>Resilient Exception Handling & Fallback:</b>
 * If a mountain photo is missing from {@code src/main/resources/images/mountains/},
 * an {@link ImageLoadException} is caught internally, logged, and gracefully resolved:
 * <ol>
 *   <li>Attempts to fall back to the bundled {@code placeholder.jpg}.</li>
 *   <li>If {@code placeholder.jpg} is also unavailable or corrupt, dynamically generates
 *       an in-memory vector graphic fallback using a JavaFX {@link Canvas}.</li>
 * </ol>
 * This guarantees the user interface never crashes, freezes, or displays broken image boxes.</p>
 */
public final class MountainImages {

    private static final String BASE_PATH = "/images/mountains/";
    private static final String DEFAULT_PLACEHOLDER = "placeholder.jpg";

    // Memory cache mapping "filename@widthxheight" -> decoded Image
    private static final Map<String, Image> CACHE = new HashMap<>();

    private MountainImages() {
        // Prevent instantiation of utility class
    }

    /**
     * Loads the mountain's image scaled to fit within width x height.
     * Guaranteed never to return null or throw unhandled exceptions.
     */
    public static Image load(Mountain mountain, double width, double height) {
        if (mountain == null) {
            return generateFallbackImage("Mountain", width, height);
        }

        String fileName = (mountain.getImageFile() != null && !mountain.getImageFile().isBlank())
                ? mountain.getImageFile()
                : DEFAULT_PLACEHOLDER;

        String cacheKey = fileName + "@" + (int) width + "x" + (int) height;
        Image cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Image loadedImage = null;
        try {
            loadedImage = attemptLoadResource(fileName, width, height);
        } catch (ImageLoadException e) {
            System.err.println("[MountainImages Warning] " + e.getMessage() + " -- falling back to default placeholder.");
            try {
                loadedImage = attemptLoadResource(DEFAULT_PLACEHOLDER, width, height);
            } catch (ImageLoadException fallbackEx) {
                System.err.println("[MountainImages Warning] Default placeholder unavailable. Generating programmatic canvas fallback.");
                loadedImage = generateFallbackImage(mountain.getName(), width, height);
            }
        }

        if (loadedImage == null) {
            loadedImage = generateFallbackImage(mountain.getName(), width, height);
        }

        CACHE.put(cacheKey, loadedImage);
        return loadedImage;
    }

    /**
     * Attempts to resolve and decode an image from the classpath.
     *
     * @throws ImageLoadException if the file is missing or corrupted
     */
    private static Image attemptLoadResource(String resourceName, double width, double height) throws ImageLoadException {
        URL resourceUrl = MountainImages.class.getResource(BASE_PATH + resourceName);
        if (resourceUrl == null) {
            throw new ImageLoadException(resourceName, "Resource not found at classpath path: " + BASE_PATH + resourceName);
        }

        try {
            // preserveRatio = true, smooth = true, backgroundLoading = false (synchronous for reliable display)
            Image img = new Image(resourceUrl.toExternalForm(), width, height, true, true, false);
            if (img.isError()) {
                throw new ImageLoadException(resourceName, "Corrupt or unreadable image format", img.getException());
            }
            return img;
        } catch (Exception e) {
            throw new ImageLoadException(resourceName, "Error decoding image stream", e);
        }
    }

    /**
     * Dynamically generates an in-memory bitmap graphic using PixelWriter.
     * Thread-safe and executable on any thread (including headless test runners).
     */
    private static Image generateFallbackImage(String mountainName, double width, double height) {
        int w = (int) Math.max(width, 100);
        int h = (int) Math.max(height, 80);

        WritableImage image = new WritableImage(w, h);
        PixelWriter writer = image.getPixelWriter();

        Color skyColor = Color.web("#334155");
        Color mountainColor = Color.web("#1e293b");
        Color snowCap = Color.web("#e2e8f0");

        int peakX = w / 2;
        int peakY = (int) (h * 0.25);
        double slope = (double) (h - peakY) / (w / 2.0);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double minElevationY = peakY + Math.abs(x - peakX) * slope;
                if (y >= minElevationY) {
                    // Snow cap near peak
                    if (y < peakY + (h * 0.15)) {
                        writer.setColor(x, y, snowCap);
                    } else {
                        writer.setColor(x, y, mountainColor);
                    }
                } else {
                    writer.setColor(x, y, skyColor);
                }
            }
        }

        return image;
    }

    /**
     * Returns a ready-to-display {@link ImageView} for cards and headers.
     */
    public static ImageView view(Mountain mountain, double width, double height) {
        ImageView view = new ImageView(load(mountain, width, height));
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        view.setSmooth(true);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        view.setClip(clip);
        return view;
    }

    /**
     * Returns an {@link ImageView} formatted for widescreen landscape hero banners.
     */
    public static ImageView viewLandscape(Mountain mountain, double width, double height) {
        ImageView view = new ImageView(load(mountain, width, height));
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        view.setSmooth(true);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        view.setClip(clip);
        return view;
    }
}
