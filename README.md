# Mountain Route-Finder

A JavaFX desktop app that finds optimal ascent routes across five mountains
(Guru Shikhar, Kilimanjaro, Mont Blanc, Everest, Annapurna) using Dijkstra's
algorithm over a waypoint graph, with three selectable cost functions:
shortest distance, least elevation gain, and safest terrain.

## Stack
- Java 21, JavaFX
- SQLite via JDBC
- Maven

## Running
```
mvn javafx:run
```

## Structure
- `model/` — domain objects (Mountain, Waypoint, RouteSegment, NamedRoute, CostFunction)
- `algorithm/` — Graph + Dijkstra, UI-independent
- `db/` — DatabaseManager + one DAO per table
- `ui/` — JavaFX views (Browse, Route Finder, Map)
- `src/main/resources/schema.sql` — table definitions
- `src/main/resources/seed-data.sql` — hand-curated mountain data
