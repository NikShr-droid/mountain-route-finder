package com.routefinder.export;

import com.routefinder.exception.ExportException;
import com.routefinder.model.PathResult;
import com.routefinder.model.Waypoint;

import java.nio.file.Path;
import java.util.List;

/**
 * Common interface defining the contract for exporting optimal mountain routes
 * into persistent external file formats.
 *
 * <p><b>OOP & Viva Note (Interface Design & Collections):</b>
 * <b>Why an interface and not an abstract class?</b> {@code RouteExporter} defines a purely
 * behavioral capability (exporting) across disparate formatters with zero shared state or
 * instance variables. Concrete implementations ({@link GpxExporter}, {@link CsvExporter})
 * independently implement the contract using collections ({@link java.util.HashMap} for $O(1)$
 * waypoint lookup, {@link java.util.ArrayList} for segment traversal). This design adheres to
 * the Interface Segregation Principle (ISP) and enables dynamic runtime polymorphism.</p>
 */
public interface RouteExporter {

    /**
     * Exports the computed ascent route and associated waypoint data to the target file.
     *
     * @param result      the computed route with ordered trail segments
     * @param waypoints   the list of mountain waypoints used to resolve geographic coordinates
     * @param destination the destination file path on disk
     * @throws ExportException if file I/O fails or inputs are invalid
     */
    void export(PathResult result, List<Waypoint> waypoints, Path destination) throws ExportException;

    /**
     * Returns the standard file extension including the leading dot (e.g. ".gpx", ".csv").
     */
    String getFileExtension();

    /**
     * Returns the human-friendly name of the export format (e.g. "GPX Track (.gpx)").
     */
    String getFormatName();
}
