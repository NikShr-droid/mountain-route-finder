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
            seedIfEmpty();
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
     * Loads seed-data.sql if the mountains table is empty. Runs every time
     * getInstance() is first called in a session, so any code path that
     * touches the database (App startup, DAOs, tests) is guaranteed real
     * data to work with -- not just an empty schema.
     */
    private void seedIfEmpty() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM mountains")) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("[DatabaseManager] Mountains table already has data — skipping seed-data.sql");
                return;
            }
        }
        System.out.println("[DatabaseManager] mountains table empty — running seed-data.sql...");
        runScript("/seed-data.sql");
        System.out.println("[DatabaseManager] Seed data loaded.");
    }

    /**
     * Reads a .sql file off the classpath (src/main/resources) and executes
     * each statement one at a time. JDBC's Statement only runs a single
     * statement per execute() call, so we split naively on ';'.
     *
     * Full-line '--' comments are stripped first specifically because a
     * semicolon inside a comment (e.g. "-- do X; then Y") would otherwise
     * split mid-statement and corrupt the parse -- this happened once
     * already in seed-data.sql, so it's handled defensively now rather
     * than trusting every future comment to stay semicolon-free.
     */
    private void runScript(String resourcePath) throws SQLException {
        try (InputStream in = DatabaseManager.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Could not find " + resourcePath + " on classpath");
            }
            String rawScript = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            String script = rawScript.lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");

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