package com.routefinder.export;

import com.routefinder.exception.ExportException;
import com.routefinder.model.PathResult;
import com.routefinder.model.RouteSegment;
import com.routefinder.model.Waypoint;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exports computed mountain routes to comma-separated tabular files (.csv).
 *
 * <p>Compatible with spreadsheet applications (Excel, Google Sheets) and data analysis tools.
 * Utilizes a {@link HashMap} for $O(1)$ waypoint resolution and try-with-resources with
 * {@link BufferedWriter} for guaranteed deterministic resource cleanup.</p>
 */
public class CsvExporter implements RouteExporter {

    @Override
    public String getFileExtension() {
        return ".csv";
    }

    @Override
    public String getFormatName() {
        return "Spreadsheet CSV (.csv)";
    }

    @Override
    public void export(PathResult result, List<Waypoint> waypoints, Path destination) throws ExportException {
        if (result == null || result.getSegments().isEmpty()) {
            throw new ExportException("Cannot export an empty or null route");
        }
        if (waypoints == null) {
            throw new ExportException("Waypoints list cannot be null");
        }
        if (destination == null) {
            throw new ExportException("Destination path cannot be null");
        }

        // Build a lookup map of waypoints for constant-time name and elevation queries
        Map<Integer, Waypoint> waypointMap = new HashMap<>();
        for (Waypoint wp : waypoints) {
            waypointMap.put(wp.getId(), wp);
        }

        try {
            if (destination.getParent() != null) {
                Files.createDirectories(destination.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
                // Write CSV Header
                writer.write("Step,From_Waypoint,To_Waypoint,Distance_km,Elevation_Gain_m,Safety_Score,Cumulative_Distance_km,Cumulative_Gain_m");
                writer.newLine();

                double cumDistance = 0.0;
                int cumGain = 0;
                int step = 1;

                for (RouteSegment segment : result.getSegments()) {
                    cumDistance += segment.getDistanceKm();
                    cumGain += segment.getElevationGainM();

                    Waypoint from = waypointMap.get(segment.getFromWaypointId());
                    Waypoint to = waypointMap.get(segment.getToWaypointId());

                    String fromName = from != null ? from.getName() : "Waypoint #" + segment.getFromWaypointId();
                    String toName = to != null ? to.getName() : "Waypoint #" + segment.getToWaypointId();

                    writer.write(String.format("%d,\"%s\",\"%s\",%.2f,%d,%d,%.2f,%d",
                            step++,
                            escapeCsv(fromName),
                            escapeCsv(toName),
                            segment.getDistanceKm(),
                            segment.getElevationGainM(),
                            segment.getSafetyScore(),
                            cumDistance,
                            cumGain
                    ));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new ExportException("Failed writing CSV export to: " + destination, e);
        }
    }

    private String escapeCsv(String text) {
        return text.replace("\"", "\"\"");
    }

    @Override
    public String toString() {
        return getFormatName();
    }
}
