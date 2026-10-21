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
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exports computed mountain routes to standard GPS Exchange Format (GPX 1.1) XML.
 *
 * <p>Compatible with handheld GPS devices, mobile hiking navigation apps (Gaia GPS, AllTrails),
 * and outdoor mapping platforms (Strava, CalTopo, Google Earth).
 * Writes both {@code <wpt>} waypoint markers and continuous {@code <trkpt>} track lines.</p>
 */
public class GpxExporter implements RouteExporter {

    private static final Pattern COORD_PATTERN =
            Pattern.compile("\\[\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*\\]");

    @Override
    public String getFileExtension() {
        return ".gpx";
    }

    @Override
    public String getFormatName() {
        return "GPS Exchange Track (.gpx)";
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

        Map<Integer, Waypoint> waypointMap = new HashMap<>();
        for (Waypoint wp : waypoints) {
            waypointMap.put(wp.getId(), wp);
        }

        try {
            if (destination.getParent() != null) {
                Files.createDirectories(destination.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                writer.newLine();
                writer.write("<gpx version=\"1.1\" creator=\"Mountain Route-Finder\" " +
                        "xmlns=\"http://www.topografix.com/GPX/1/1\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 " +
                        "http://www.topografix.com/GPX/1/1/gpx.xsd\">");
                writer.newLine();

                // Metadata
                writer.write("  <metadata>");
                writer.newLine();
                writer.write("    <name>Mountain Ascent Optimal Route</name>");
                writer.newLine();
                writer.write("    <time>" + Instant.now() + "</time>");
                writer.newLine();
                writer.write("  </metadata>");
                writer.newLine();

                // Distinct waypoints along this path
                Set<Integer> visitedIds = new LinkedHashSet<>();
                for (RouteSegment seg : result.getSegments()) {
                    visitedIds.add(seg.getFromWaypointId());
                    visitedIds.add(seg.getToWaypointId());
                }

                for (int id : visitedIds) {
                    Waypoint wp = waypointMap.get(id);
                    if (wp != null) {
                        writer.write(String.format("  <wpt lat=\"%.6f\" lon=\"%.6f\">", wp.getLatitude(), wp.getLongitude()));
                        writer.newLine();
                        writer.write("    <ele>" + wp.getElevationM() + "</ele>");
                        writer.newLine();
                        writer.write("    <name>" + escapeXml(wp.getName()) + "</name>");
                        writer.newLine();
                        writer.write("    <type>" + escapeXml(wp.getWaypointType()) + "</type>");
                        writer.newLine();
                        writer.write("  </wpt>");
                        writer.newLine();
                    }
                }

                // Track and continuous track points
                writer.write("  <trk>");
                writer.newLine();
                writer.write("    <name>Ascent Track</name>");
                writer.newLine();
                writer.write("    <trkseg>");
                writer.newLine();

                for (RouteSegment segment : result.getSegments()) {
                    String json = segment.getGeometryJson();
                    boolean wrotePoints = false;

                    if (json != null && !json.isBlank()) {
                        Matcher matcher = COORD_PATTERN.matcher(json);
                        while (matcher.find()) {
                            double lat = Double.parseDouble(matcher.group(1));
                            double lon = Double.parseDouble(matcher.group(2));
                            writer.write(String.format("      <trkpt lat=\"%.6f\" lon=\"%.6f\" />", lat, lon));
                            writer.newLine();
                            wrotePoints = true;
                        }
                    }

                    // Fallback to start/end waypoints if no detailed geometry trace was stored
                    if (!wrotePoints) {
                        Waypoint from = waypointMap.get(segment.getFromWaypointId());
                        Waypoint to = waypointMap.get(segment.getToWaypointId());
                        if (from != null) {
                            writer.write(String.format("      <trkpt lat=\"%.6f\" lon=\"%.6f\"><ele>%d</ele></trkpt>",
                                    from.getLatitude(), from.getLongitude(), from.getElevationM()));
                            writer.newLine();
                        }
                        if (to != null) {
                            writer.write(String.format("      <trkpt lat=\"%.6f\" lon=\"%.6f\"><ele>%d</ele></trkpt>",
                                    to.getLatitude(), to.getLongitude(), to.getElevationM()));
                            writer.newLine();
                        }
                    }
                }

                writer.write("    </trkseg>");
                writer.newLine();
                writer.write("  </trk>");
                writer.newLine();
                writer.write("</gpx>");
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ExportException("Failed writing GPX export to: " + destination, e);
        }
    }

    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @Override
    public String toString() {
        return getFormatName();
    }
}
