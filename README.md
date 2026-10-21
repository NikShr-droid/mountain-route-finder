# Mountain Route-Finder

A desktop application built in Java 21 and JavaFX that calculates, compares, and exports optimal trailhead-to-summit mountain ascent routes over directed waypoint graphs.

Optimal routes are computed under three independent optimization criteria:
1. **Shortest Distance** (minimizing total horizontal and trail kilometers)
2. **Least Elevation Gain** (minimizing vertical climbing effort)
3. **Safest Terrain** (minimizing hazard ratings across trail segments)

---

## 1. Problem Statement & Scope

### Problem
Planning a mountain ascent requires balancing competing constraints—distance, vertical elevation, and terrain hazard. Route information in the mountaineering world is typically scattered across operator brochures or prose guidebooks, making direct side-by-side trade-off analysis difficult.

### Solution
Mountain Route-Finder models each mountain as a directed graph where vertices represent terrain waypoints (trailheads, checkpoints, camps, junctions, and summits) and edges represent uphill trail segments. Using multi-objective pathfinding algorithms (**Dijkstra's Algorithm** and **A\* Search**), the application simultaneously calculates optimal ascents across all three criteria and displays them side by side.

### Scope
- **Hero Mountains (Full Branching Route Networks)**:
  - **Mount Kilimanjaro** (Tanzania, 5,895m) — Machame, Lemosho, and Marangu routes
  - **Mont Blanc** (France/Italy, 4,806m) — Goûter and Trois Monts routes
  - **Mount Fuji** (Japan, 3,776m) — Yoshida, Fujinomiya, and Gotemba trails
- **Catalogue Mountains**: Additional peaks available for browsing in the mountain index.
- **Route Exporting**: Exporting computed ascent itineraries to industry-standard **GPX 1.1 tracks** (for GPS devices and Strava) and **CSV spreadsheets** (for Excel/Google Sheets).
- **Out of Scope**: Real-time GPS tracking, live weather telemetry, descent routes, and gear checklists.

---

## 2. Technology Stack & Prerequisites

- **Language**: Java 21 (LTS)
- **GUI Framework**: JavaFX 21 (`javafx-controls`, `javafx-fxml`, `javafx-web`)
- **Database**: SQLite via JDBC (`org.xerial:sqlite-jdbc`)
- **Build Tool**: Apache Maven 3.9+
- **Testing**: JUnit 5 (`junit-jupiter`)

---

## 3. How to Run

### Command Line (Recommended)
```bash
mvn clean javafx:run
```

### Running Tests
```bash
mvn test
```

> [!NOTE]
> When running from IntelliJ IDEA, always use the Maven tool window (`Plugins -> javafx -> javafx:run`) or execute `mvn javafx:run` from the built-in terminal. Clicking IntelliJ's plain green "Run App.main()" button will fail with *"JavaFX runtime components are missing"* unless VM options (`--module-path`) are manually configured.

---

## 4. Object-Oriented Architecture & Rubric Alignment

The application is structured to demonstrate sound Object-Oriented Programming (OOP) principles:

```
src/main/java/com/routefinder/
├── App.java                             # JavaFX application entry point
├── algorithm/                           # UI-independent pathfinding algorithms
│   ├── PathfindingAlgorithm.java        # Abstract base algorithm class
│   ├── Dijkstra.java                    # Dijkstra's shortest path implementation
│   ├── AStarAlgorithm.java              # A* search with spherical Haversine heuristic
│   └── Graph.java                       # Adjacency list graph with overloaded constructors
├── model/                               # Encapsulated domain entities
│   ├── Waypoint.java                    # Abstract waypoint base class
│   ├── Trailhead.java                   # Concrete waypoint subtype
│   ├── Camp.java                        # Concrete waypoint subtype
│   ├── Checkpoint.java                  # Concrete waypoint subtype
│   ├── Junction.java                    # Concrete waypoint subtype
│   ├── Summit.java                      # Concrete waypoint subtype
│   ├── WaypointFactory.java             # Factory Pattern for waypoint instantiation
│   ├── Mountain.java                    # Mountain entity with overloaded constructors
│   ├── RouteSegment.java                # Directed trail edge with defensive validation
│   ├── PathResult.java                  # Immutable path result with defensive copying
│   ├── CostFunction.java                # Polymorphic enum (constant-specific class bodies)
│   ├── User.java                        # Climber user profile entity
│   ├── SavedRoute.java                  # Personal logbook entry (saved wishlist & summited)
│   ├── UserSession.java                 # Active climber session manager
│   └── NamedRoute.java                  # Classic named trail sequence
├── export/                              # Persistent file handling and export
│   ├── RouteExporter.java               # Interface defining export contract
│   ├── GpxExporter.java                 # Standard GPX 1.1 XML track generator
│   └── CsvExporter.java                 # Structured tabular CSV generator
├── exception/                           # Custom domain exception hierarchy
│   ├── RouteNotFoundException.java      # Checked exception (unreachable target)
│   ├── ExportException.java             # Checked exception (file I/O failure)
│   ├── DataAccessException.java         # Unchecked exception (DAO database failure)
│   ├── ImageLoadException.java          # Unchecked exception (asset loading & decoding)
│   ├── UnseededMountainException.java   # Unchecked exception (unseeded catalog peak interaction)
│   └── FeatureNotImplementedException.java # Unchecked exception (future milestone guard)
├── db/                                  # Persistence and Data Access Objects (DAOs)
│   ├── DatabaseManager.java             # Singleton JDBC manager; auto-seeds SQLite
│   ├── MountainDao.java                 # Mountain table access
│   ├── WaypointDao.java                 # Waypoint table access with route data validation
│   ├── RouteSegmentDao.java             # Route segment table access
│   ├── NamedRouteDao.java               # Classic named route access
│   ├── UserDao.java                     # User profile CRUD operations
│   └── SavedRouteDao.java               # Personal logbook CRUD operations
└── ui/                                  # JavaFX presentation layer
    ├── MainView.java                    # Navigation container, climber profile & theme toggle
    ├── RouteFinderView.java             # Interactive route finder with live algorithm switcher & save/summit actions
    ├── BrowseView.java                  # Mountain catalogue browser & route availability gates
    ├── MapView.java                     # Map and elevation profile view with error states
    ├── LogbookView.java                 # Climber logbook, expedition tracker & summit statistics
    ├── AlertHelper.java                 # Theme-aware modal alerts & headless-safe dialogs
    ├── ThemeManager.java                # Live Alpine Snow / Midnight Sky styling engine
    └── MountainImages.java              # Local photo loader, cache, and headless-safe fallback
```

### OOP Rubric Mapping

| OOP Concept | Project Implementation |
| :--- | :--- |
| **Encapsulation & Constructors** | Private data members with getters; defensive copying in [`PathResult`](src/main/java/com/routefinder/model/PathResult.java); strict constructor validation in [`RouteSegment`](src/main/java/com/routefinder/model/RouteSegment.java) and [`Waypoint`](src/main/java/com/routefinder/model/Waypoint.java); constructor overloading on [`Mountain`](src/main/java/com/routefinder/model/Mountain.java) and [`Graph`](src/main/java/com/routefinder/algorithm/Graph.java). |
| **Inheritance & Abstraction** | Two distinct domain hierarchies: (1) [`PathfindingAlgorithm`](src/main/java/com/routefinder/algorithm/PathfindingAlgorithm.java) $\rightarrow$ [`Dijkstra`](src/main/java/com/routefinder/algorithm/Dijkstra.java) & [`AStarAlgorithm`](src/main/java/com/routefinder/algorithm/AStarAlgorithm.java); (2) [`Waypoint`](src/main/java/com/routefinder/model/Waypoint.java) $\rightarrow$ [`Trailhead`](src/main/java/com/routefinder/model/Trailhead.java), [`Camp`](src/main/java/com/routefinder/model/Camp.java), [`Checkpoint`](src/main/java/com/routefinder/model/Checkpoint.java), [`Junction`](src/main/java/com/routefinder/model/Junction.java), [`Summit`](src/main/java/com/routefinder/model/Summit.java). |
| **Polymorphism** | **Method Overloading**: Overloaded `findRoute(...)` in algorithm classes. **Method Overriding**: Subclass method implementations of `findRoute()`, `describe()`, and `toString()`. **Runtime Polymorphism**: Dynamic dispatch via algorithm selector in `RouteFinderView`; enum constant-specific class bodies in [`CostFunction`](src/main/java/com/routefinder/model/CostFunction.java). |
| **Interfaces & Collections** | Custom [`RouteExporter`](src/main/java/com/routefinder/export/RouteExporter.java) interface implemented by [`GpxExporter`](src/main/java/com/routefinder/export/GpxExporter.java) and [`CsvExporter`](src/main/java/com/routefinder/export/CsvExporter.java). Functional [`Comparator`](src/main/java/com/routefinder/ui/BrowseView.java) strategies for multi-criteria catalog sorting (difficulty ranking, elevation, route availability, name). Extensive collection usage: `HashMap`, `EnumMap`, `LinkedHashSet`, `ArrayList`, `LinkedList`, `PriorityQueue`, and `Set`. |
| **Exception & File Handling** | Custom checked exceptions ([`RouteNotFoundException`](src/main/java/com/routefinder/exception/RouteNotFoundException.java), [`ExportException`](src/main/java/com/routefinder/exception/ExportException.java)) and unchecked exceptions ([`DataAccessException`](src/main/java/com/routefinder/exception/DataAccessException.java), [`ImageLoadException`](src/main/java/com/routefinder/exception/ImageLoadException.java), [`FeatureNotImplementedException`](src/main/java/com/routefinder/exception/FeatureNotImplementedException.java)). Dual persistent storage: local SQLite DB (`data/mountainroutes.db`) and stream-based GPX/CSV file output via `java.nio.file.Files`. |

---

## 5. Defensive Exception Handling & Comprehensive Error States

To ensure the application is robust during real-world execution, viva demonstrations, and partner collaboration:

1. **Missing Image Assets & Procedural Fallback**:
   - The [`MountainImages`](src/main/java/com/routefinder/ui/MountainImages.java) service catches [`ImageLoadException`](src/main/java/com/routefinder/exception/ImageLoadException.java) whenever an image file is absent or corrupted.
   - If a mountain's photo does not exist in `src/main/resources/images/mountains/`, it logs a warning and automatically falls back to `placeholder.jpg`.
   - If `placeholder.jpg` is also missing, an in-memory procedural graphic (alpine gradient generated via `WritableImage` and `PixelWriter`) is rendered dynamically. This ensures thread safety during headless Maven Surefire testing and guarantees that the UI never displays broken image frames or throws `NullPointerException`.

2. **Unseeded Mountain Data & Interactive Warning States**:
   - Only 3 "hero" peaks (Kilimanjaro, Mont Blanc, Mt Fuji) currently have full waypoint and trail segment networks seeded in SQLite. The remaining 8 peaks are catalog-only.
   - **Catalog View ([`BrowseView`](src/main/java/com/routefinder/ui/BrowseView.java))**:
     - Peaks with route data display a green `✓ SEEDED` badge and active `Plan Route →` button.
     - Unseeded peaks display an amber `⚠️ CATALOG` badge and a muted `Route Pending ⚠️` button.
     - Clicking an unseeded peak card or button intercepts navigation and presents an informative modal dialog via [`AlertHelper`](src/main/java/com/routefinder/ui/AlertHelper.java), explaining that route data has not been seeded yet and directing the user to the hero peaks.
   - **Route Finder View ([`RouteFinderView`](src/main/java/com/routefinder/ui/RouteFinderView.java))**:
     - Selecting an unseeded mountain from the dropdown displays an inline warning banner, disables the `Find Routes` and `Export Route` buttons, and renders an empty state card explaining that route calculation is disabled for this peak.
     - Defensive domain validation via [`WaypointDao.validateMountainSeeded()`](src/main/java/com/routefinder/db/WaypointDao.java) throws [`UnseededMountainException`](src/main/java/com/routefinder/exception/UnseededMountainException.java) if route computation is triggered programmatically on unseeded peaks.
   - **Elevation Profile View ([`MapView`](src/main/java/com/routefinder/ui/MapView.java))**:
     - Selecting an unseeded peak disables the `Plot Profile` button and renders an inline "Topographic Elevation Profile Unavailable" card.

3. **General Error States**:
   - **Disconnected Graphs / Route Failure**: If a graph cannot find a path between trailhead and summit, [`RouteNotFoundException`](src/main/java/com/routefinder/exception/RouteNotFoundException.java) is caught, and an explicit error state card ("No Traversable Route Found") is displayed in the itinerary panel and status bar.
   - **File Export Errors**: If disk I/O fails during GPX or CSV generation, [`ExportException`](src/main/java/com/routefinder/exception/ExportException.java) is caught, displaying a red status bar alert and modal error dialog with the specific file path failure details.
   - **Headless Unit Test Safety**: [`AlertHelper`](src/main/java/com/routefinder/ui/AlertHelper.java) verifies `Platform.isFxApplicationThread()`, gracefully logging alerts to standard error during headless JUnit test execution without throwing `IllegalStateException`.

4. **Future Feature Guards**:
   - Features currently scheduled for future milestones throw [`FeatureNotImplementedException`](src/main/java/com/routefinder/exception/FeatureNotImplementedException.java) if invoked programmatically, preserving architectural boundaries.

---

## 6. Future Enhancements & Roadmap (Features to be Explored Later)

The following features represent architectural stretch goals and future milestones:

1. **Complete High-Resolution Mountain Photo Asset Pack**:
   - Sourcing and bundling verified high-resolution photography for all 11 catalogued mountains (replacing dynamic vector/placeholder fallbacks).
2. **Named Historic Route Overlays**:
   - Implementing UI toggle switches for classic named trekking routes (e.g., the historical Machame or Goûter routes from the `named_routes` table) to overlay alongside Dijkstra-computed optimal routes.
3. **2D Organic Trail Map (WebView + Leaflet.js)**:
   - Integrating a JavaFX `WebView` embedding OpenStreetMap / Leaflet to draw organic GPS trail lines using the stored `geometry_json` coordinates.
4. **Descent Planning & Multi-Day Camp Scheduling**:
   - Extending the graph model to calculate optimal descent itineraries, daily hiking hours, and overnight camp acclimatization intervals.
5. **Real-Time Weather & Avalanche Hazard Telemetry**:
   - Connecting to open meteorology APIs (e.g. Open-Meteo) to dynamically adjust `safetyScore` edge weights based on real-time wind speed, freezing levels, and precipitation.
6. **Additional Export Formats**:
   - Adding `GeoJsonExporter` and `KmlExporter` implementations to [`RouteExporter`](src/main/java/com/routefinder/export/RouteExporter.java) for Google Earth 3D flyovers.

---

## 7. Documentation & Diagrams

- **UI Design System & Stitch Specification**: [docs/DESIGN.md](docs/DESIGN.md)
- **Formal UML Class Diagram**: [docs/class-diagram.md](docs/class-diagram.md) (Interactive Mermaid) and [docs/class-diagram.puml](docs/class-diagram.puml) (PlantUML)
- **Grading Rubric Breakdown**: [MARKING_SCHEME.md](MARKING_SCHEME.md)
- **Viva Preparation Guide**: [docs/VIVA_PREPARATION.md](docs/VIVA_PREPARATION.md)
