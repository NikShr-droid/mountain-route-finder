package com.routefinder.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single JDBC connection to the local SQLite file and runs
 * schema.sql on first startup if the tables don't exist yet.
 *
 * SQLite is a local file, not a server -- there's no connection pool to
 * manage, so one shared Connection for the app's lifetime is enough.
 * DAOs get their Connection from here; they should never open their own.
 */
public class DatabaseManager {

    private static final String DB_DIR  = "data";
    private static final String DB_FILE = DB_DIR + "/mountainroutes.db";
    private static final String DB_URL  = "jdbc:sqlite:" + DB_FILE;

    private static DatabaseManager instance;
    private final Connection connection;

    private DatabaseManager() {
        try {
            Files.createDirectories(Path.of(DB_DIR));
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            initializeSchemaIfNeeded();
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to initialize database at " + DB_FILE, e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Looks for the 'mountains' table; if it's missing, this is a fresh
     * .db file and we run schema.sql to create all five tables.
     */
    private void initializeSchemaIfNeeded() throws SQLException {
        if (tablesExist()) {
            System.out.println("[DatabaseManager] Existing database found at " + DB_FILE);
            return;
        }
        System.out.println("[DatabaseManager] No tables found — running schema.sql...");
        runScript("/schema.sql");
        System.out.println("[DatabaseManager] Schema applied.");
    }

    private boolean tablesExist() throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, "mountains", null)) {
            return rs.next();
        }
    }

    /**
     * Reads a .sql file off the classpath (src/main/resources) and executes
     * each statement one at a time. JDBC's Statement only runs a single
     * statement per execute() call, so we split naively on ';' -- this is
     * safe for schema.sql/seed-data.sql specifically since neither uses
     * semicolons inside string literals, CHECK constraints, or triggers.
     * (Verified against this exact schema.sql before shipping.)
     */
    private void runScript(String resourcePath) throws SQLException {
        try (InputStream in = DatabaseManager.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Could not find " + resourcePath + " on classpath");
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement stmt = connection.createStatement()) {
                for (String rawStatement : script.split(";")) {
                    String sql = rawStatement.trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + resourcePath, e);
        }
    }
}
