package com.routefinder.export;

import com.routefinder.exception.ExportException;
import com.routefinder.model.Camp;
import com.routefinder.model.PathResult;
import com.routefinder.model.RouteSegment;
import com.routefinder.model.Summit;
import com.routefinder.model.Trailhead;
import com.routefinder.model.Waypoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link RouteExporter} implementations, file I/O operations, and exception handling (R4, R5).
 */
public class RouteExporterTest {

    @TempDir
    Path tempDir;

    private PathResult sampleRoute;
    private List<Waypoint> sampleWaypoints;

    @BeforeEach
    void setUp() {
        sampleWaypoints = List.of(
                new Trailhead(1, 1, "Moshi Base", -3.334, 37.340, 810),
                new Camp(2, 1, "Machame Camp", -3.193, 37.335, 2850),
                new Summit(3, 1, "Uhuru Peak", -3.075, 37.353, 5895)
        );

        List<RouteSegment> segments = List.of(
                new RouteSegment(1, 1, 1, 2, 11.0, 2040, 2, 360, "[[-3.334, 37.340], [-3.193, 37.335]]"),
                new RouteSegment(2, 1, 2, 3, 5.0, 3045, 4, 420, "[[-3.193, 37.335], [-3.075, 37.353]]")
        );

        sampleRoute = new PathResult(segments, 16.0);
    }

    @Test
    void csvExporterCreatesValidFileWithHeadersAndRows() throws Exception {
        RouteExporter exporter = new CsvExporter();
        Path target = tempDir.resolve("route_export.csv");

        exporter.export(sampleRoute, sampleWaypoints, target);

        assertTrue(Files.exists(target), "Export file should exist on disk");
        List<String> lines = Files.readAllLines(target);
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).startsWith("Step,From_Waypoint,To_Waypoint"));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Moshi Base") && l.contains("Machame Camp")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Uhuru Peak")));
    }

    @Test
    void gpxExporterCreatesValidXmlWithWaypointsAndTracks() throws Exception {
        RouteExporter exporter = new GpxExporter();
        Path target = tempDir.resolve("route_export.gpx");

        exporter.export(sampleRoute, sampleWaypoints, target);

        assertTrue(Files.exists(target), "GPX file should exist on disk");
        String content = Files.readString(target);
        assertTrue(content.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(content.contains("<gpx version=\"1.1\""));
        assertTrue(content.contains("<wpt lat=\"-3.334000\" lon=\"37.340000\">"));
        assertTrue(content.contains("<name>Moshi Base</name>"));
        assertTrue(content.contains("<trk>"));
        assertTrue(content.contains("<trkpt lat=\"-3.334000\" lon=\"37.340000\" />"));
    }

    @Test
    void exportersRejectEmptyOrNullRoutes() {
        RouteExporter csv = new CsvExporter();
        RouteExporter gpx = new GpxExporter();

        Path target = tempDir.resolve("empty.txt");

        assertThrows(ExportException.class, () ->
                csv.export(null, sampleWaypoints, target));

        assertThrows(ExportException.class, () ->
                gpx.export(new PathResult(Collections.emptyList(), 0.0), sampleWaypoints, target));
    }

    @Test
    void exportersHandlePolymorphicCollections() {
        // Demonstrates interface polymorphism: List<RouteExporter>
        List<RouteExporter> exporters = List.of(new CsvExporter(), new GpxExporter());

        for (RouteExporter exporter : exporters) {
            Path target = tempDir.resolve("test_poly" + exporter.getFileExtension());
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    exporter.export(sampleRoute, sampleWaypoints, target));
            assertTrue(Files.exists(target));
        }
    }
}
