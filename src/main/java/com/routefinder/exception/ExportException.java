package com.routefinder.exception;

/**
 * Checked exception thrown when exporting a route to external file storage fails.
 *
 * <p><b>OOP & Viva Note:</b>
 * This is a <i>checked exception</i> because I/O failures (file permission denials,
 * disk space exhaustion, locked files) are recoverable environmental problems.
 * Callers (e.g. GUI buttons) are obligated by the compiler to catch this exception
 * and display actionable feedback to the user.</p>
 */
public class ExportException extends Exception {

    public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
