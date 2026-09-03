-- Hand-curated mountain/waypoint/route-segment data
-- for Guru Shikhar, Kilimanjaro, Mont Blanc, Everest, Annapurna.
--
-- KILIMANJARO (Machame Route) is seeded first, as the pilot mountain.
-- Distance/elevation-gain/camp-elevation figures are cross-checked across
-- several real trekking-operator route descriptions (Kilimanjaro Sunrise,
-- Mount Kilimanjaro Guide, Sunny Safaris, Kilimanjaro National Park) --
-- minor variance between them (a few meters/km) reflects normal real-world
-- discrepancy between operators, not error.
--
-- KNOWN LIMITATIONS (flagged honestly, to revisit):
--  1. Waypoint lat/lng are APPROXIMATE placeholders interpolated along the
--     known route corridor (only Uhuru Peak's coordinate is a confirmed
--     published value). Real GPX traces exist publicly -- swap these in
--     once one is downloaded and processed.
--  2. This is currently a SINGLE linear path (no branching), so Dijkstra
--     will return the same route for all three cost functions right now.
--     Adding a second real route (e.g. Lemosho merging at Shira Camp) is
--     the next step to make the three cost functions actually diverge.
--  3. safety_score is hand-assigned using our 1-5 rubric, not derived from
--     any dataset -- that part is supposed to be a judgment call.
--  4. geometry_json is a straight-line [from,to] placeholder, not a real
--     trail trace, pending GPX processing.

INSERT INTO mountains (name, region, elevation_m, difficulty, season, description) VALUES
('Kilimanjaro', 'Tanzania', 5895, 'moderate', 'Jun-Oct, Jan-Mar',
 'Africa''s highest free-standing peak. Machame Route seeded here: rainforest to alpine desert to glacier, roughly 62km round trip.');

INSERT INTO waypoints (mountain_id, name, waypoint_type, latitude, longitude, elevation_m) VALUES
((SELECT id FROM mountains WHERE name = 'Kilimanjaro'), 'Machame Gate',  'trailhead', -3.2192, 37.3254, 1640),
((SELECT id FROM mountains WHERE name = 'Kilimanjaro'), 'Machame Camp',  'camp',      -3.1930, 37.3350, 2850),
((SELECT id FROM mountains WHERE name = 'Kilimanjaro'), 'Shira Camp',    'camp',      -3.1650, 37.3300, 3840),
((SELECT id FROM mountains WHERE name = 'Kilimanjaro'), 'Barranco Camp', 'camp',      -3.1230, 37.3550, 3950),
((SELECT id FROM mountains WHERE name = 'Kilimanjaro'), 'Karanga Camp',  'camp',      -3.1080, 37.3600, 3995),
((SELECT id FROM mountains WHERE name = 'Kilimanjaro'), 'Barafu Camp',   'camp',      -3.0870, 37.3630, 4673),
((SELECT id FROM mountains WHERE name = 'Kilimanjaro'), 'Uhuru Peak',    'summit',    -3.0758, 37.3533, 5895);

-- Directed, uphill only. distance_km/elevation_gain_m sourced from
-- operator day-by-day itineraries -- safety_score is hand-assigned.
INSERT INTO route_segments
    (mountain_id, from_waypoint_id, to_waypoint_id, distance_km, elevation_gain_m, safety_score, estimated_time_min, geometry_json)
VALUES
-- Day 1: rainforest, maintained trail
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Machame Gate'  AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Machame Camp'  AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 11.0, 1210, 1, 330, '[[-3.2192,37.3254],[-3.1930,37.3350]]'),

-- Day 2: steep moorland, short but taxing
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Machame Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Shira Camp'    AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 5.0, 990, 2, 300, '[[-3.1930,37.3350],[-3.1650,37.3300]]'),

-- Day 3: via Lava Tower acclimatization detour (net camp-to-camp gain is
-- modest, but actual ascent+descent over Lava Tower is much higher -- a
-- known simplification until we model Lava Tower as its own waypoint)
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Shira Camp'    AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Barranco Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 10.0, 135, 3, 360, '[[-3.1650,37.3300],[-3.1230,37.3550]]'),

-- Day 4: Barranco Wall scramble, low net gain, high exposure
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Barranco Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Karanga Camp'  AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 5.5, 45, 3, 270, '[[-3.1230,37.3550],[-3.1080,37.3600]]'),

-- Day 5: exposed rock, colder, approaching summit zone
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Karanga Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Barafu Camp'  AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 3.5, 640, 3, 270, '[[-3.1080,37.3600],[-3.0870,37.3630]]'),

-- Summit night: scree/snow/ice, extreme altitude, night ascent
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Barafu Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Uhuru Peak'  AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 5.0, 1296, 4, 390, '[[-3.0870,37.3630],[-3.0758,37.3533]]');

-- Kilimanjaro's real "classic" route, for the (stretch-goal) named-routes toggle.
INSERT INTO named_routes (mountain_id, name, description) VALUES
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Machame Route',
 'The "Whiskey Route" -- most popular ascent, known for scenic diversity and strong acclimatization profile.');

INSERT INTO named_route_segments (named_route_id, route_segment_id, sequence_order)
SELECT
    (SELECT id FROM named_routes WHERE name='Machame Route'),
    rs.id,
    ROW_NUMBER() OVER (ORDER BY rs.id)
FROM route_segments rs
WHERE rs.mountain_id = (SELECT id FROM mountains WHERE name='Kilimanjaro');

-- ---------------------------------------------------------------------------
-- TODO: Guru Shikhar, Mont Blanc, Everest, Annapurna -- not yet seeded.