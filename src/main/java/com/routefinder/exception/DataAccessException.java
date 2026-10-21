package com.routefinder.exception;

/**
 * Unchecked domain exception thrown when a persistence or database operation fails.
 *
 * <p><b>OOP & Architecture Note:</b>
 * Following the standard Data Access Object (DAO) pattern, low-level {@link java.sql.SQLException}
 * instances are wrapped in this cohesive unchecked domain exception. This prevents relational/SQL
 * implementation details from leaking through business and UI layers while still preserving
 * the underlying root cause.</p>
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
