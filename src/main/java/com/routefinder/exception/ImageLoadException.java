package com.routefinder.exception;

/**
 * Unchecked domain exception thrown or caught when an image asset fails to load,
 * is missing from application resources, or contains corrupt binary data.
 *
 * <p><b>OOP & Exception Handling Note:</b>
 * Allows the presentation layer to distinguish asset loading failures from general
 * I/O or database issues, ensuring the UI can gracefully substitute a dynamic
 * placeholder graphic rather than crashing or leaving broken UI views.</p>
 */
public class ImageLoadException extends RuntimeException {

    private final String imageName;

    public ImageLoadException(String imageName, String message) {
        super(String.format("Failed to load image asset '%s': %s", imageName, message));
        this.imageName = imageName;
    }

    public ImageLoadException(String imageName, String message, Throwable cause) {
        super(String.format("Failed to load image asset '%s': %s", imageName, message), cause);
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }
}
