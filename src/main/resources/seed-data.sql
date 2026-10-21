-- Mountain Route-Finder seed data (DEMO SET)
--
-- 3 "hero" mountains with branching routes (Kilimanjaro, Mont Blanc, Mt Fuji)
-- plus 8 browse-only dummy mountains (no waypoints/segments).
--
-- DATA HONESTY:
--  * Kilimanjaro's Machame Route figures (distance/gain/camp elevations) are
--    cross-checked against real trekking-operator descriptions.
--  * Everything else (Marangu, Lemosho, all of Mont Blanc and Mt Fuji) is
--    ILLUSTRATIVE demo data: plausible shapes and numbers, tuned so the three
--    cost functions pick different routes. Do not present it as surveyed.
--  * Waypoint lat/lng are approximate (only Uhuru Peak, Mont Blanc summit and
--    Kengamine Peak are close to real published values).
--  * geometry_json is generated (smooth wiggle between waypoints, fixed random
--    seed), not a real trail trace. Each segment's geometry runs from_waypoint -> to_waypoint.
--
-- MODELLING NOTE: RouteFinderView picks the FIRST 'trailhead' waypoint per
-- mountain, so each mountain has ONE base node (Moshi Base, Chamonix,
-- Kawaguchiko Base). Real gates/stations hang off it via vehicle-transfer
-- segments with 0 km, 0 m gain, safety 1 and NULL time. Every route pays the
-- same +1 safety, so rankings are unaffected, and hiking totals stay clean.
--
-- Expected results (distance / least gain / safest):
--   Kilimanjaro: Marangu / Lemosho / Machame
--   Mont Blanc : Goûter / Trois Monts / Trois Monts
--   Mt Fuji    : Fujinomiya / Fujinomiya / Yoshida

INSERT INTO mountains (name, region, elevation_m, difficulty, season, description, image_file) VALUES
('Kilimanjaro', 'Tanzania', 5895, 'moderate', 'Jun-Oct, Jan-Mar',
 'Africa''s highest free-standing peak. Three routes seeded: Machame (real figures), plus Lemosho and Marangu (illustrative demo data).', 'kilimanjaro.jpg'),
('Mont Blanc', 'France/Italy', 4806, 'hard', 'Jun-Sep',
 'The highest peak in the Alps. Two routes seeded as illustrative demo data: the Goûter Route and the Trois Monts Route.', 'mont-blanc.jpg'),
('Mt Fuji', 'Japan', 3776, 'moderate', 'Jul-Sep',
 'Japan''s highest and most iconic volcano. Three trails seeded as illustrative demo data: Yoshida, Fujinomiya and Gotemba.', 'mt-fuji.jpg'),
('Everest', 'Nepal/Tibet', 8849, 'extreme', 'Apr-May',
 'Earth''s highest mountain above sea level, on the Nepal-Tibet border.', 'everest.jpg'),
('Annapurna', 'Nepal', 8091, 'extreme', 'Apr-May, Oct-Nov',
 'Annapurna I was the first 8,000 m peak ever climbed, in 1950.', 'annapurna.jpg'),
('Guru Shikhar', 'Rajasthan, India', 1722, 'easy', 'Oct-Mar',
 'The highest point of the Aravalli Range, near Mount Abu.', 'guru-shikhar.jpg'),
('Denali', 'Alaska, USA', 6190, 'hard', 'May-Jul',
 'The highest peak in North America, known for extreme cold and a huge vertical rise from base to summit.', 'denali.jpg'),
('Aconcagua', 'Argentina', 6961, 'hard', 'Dec-Feb',
 'The highest peak outside Asia, in the Argentine Andes.', 'aconcagua.jpg'),
('Matterhorn', 'Switzerland/Italy', 4478, 'hard', 'Jul-Sep',
 'The pyramid-shaped Alpine peak on the Swiss-Italian border.', 'matterhorn.jpg'),
('Elbrus', 'Russia', 5642, 'moderate', 'Jun-Sep',
 'The highest peak in the Caucasus and usually counted as Europe''s highest.', 'elbrus.jpg'),
('Stok Kangri', 'Ladakh, India', 6153, 'moderate', 'Jul-Sep',
 'A popular high-altitude trekking peak above Leh in Ladakh.', 'stok-kangri.jpg');

-- ===== KILIMANJARO waypoints
INSERT INTO waypoints (mountain_id, name, waypoint_type, latitude, longitude, elevation_m) VALUES
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Moshi Base', 'trailhead', -3.3349, 37.3407, 810),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Machame Gate', 'checkpoint', -3.2192, 37.3254, 1640),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Machame Camp', 'camp', -3.193, 37.335, 2850),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Shira Camp', 'camp', -3.165, 37.33, 3840),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Barranco Camp', 'camp', -3.123, 37.355, 3950),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Karanga Camp', 'camp', -3.108, 37.36, 3995),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Barafu Camp', 'camp', -3.087, 37.363, 4673),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Uhuru Peak', 'summit', -3.0758, 37.3533, 5895),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Marangu Gate', 'checkpoint', -3.233, 37.49, 1860),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Mandara Hut', 'camp', -3.215, 37.455, 2720),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Horombo Hut', 'camp', -3.15, 37.43, 3720),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Saddle Junction', 'junction', -3.125, 37.41, 4200),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Kibo Hut', 'camp', -3.108, 37.391, 4700),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Gilman''s Point', 'checkpoint', -3.085, 37.37, 5685),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Londorot Gate', 'checkpoint', -3.19, 37.2, 2100),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Shira 1 Camp', 'camp', -3.155, 37.265, 3500);

-- ===== KILIMANJARO route segments (directed, uphill only)
INSERT INTO route_segments
    (mountain_id, from_waypoint_id, to_waypoint_id, distance_km, elevation_gain_m, safety_score, estimated_time_min, geometry_json)
VALUES
-- Vehicle transfer to the Machame gate. 0 km / 0 m so route totals stay hiking-only.
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Moshi Base' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Machame Gate' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 0.0, 0, 1, NULL, '[[-3.3349,37.3407],[-3.2192,37.3254]]'),
-- Vehicle transfer to the Marangu gate.
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Moshi Base' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Marangu Gate' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 0.0, 0, 1, NULL, '[[-3.3349,37.3407],[-3.233,37.49]]'),
-- Vehicle transfer to the Londorot gate (Lemosho start).
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Moshi Base' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Londorot Gate' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 0.0, 0, 1, NULL, '[[-3.3349,37.3407],[-3.19,37.2]]'),
-- Machame Day 1: rainforest, maintained trail
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Machame Gate' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Machame Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 11.0, 1210, 1, 330, '[[-3.2192,37.3254],[-3.21705,37.32854],[-3.21423,37.32986],[-3.21122,37.33066],[-3.20729,37.32895],[-3.20391,37.32872],[-3.20094,37.32964],[-3.19812,37.33095],[-3.19581,37.33365],[-3.193,37.335]]'),
-- Machame Day 2: steep moorland, short but taxing
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Machame Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Shira Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 5.0, 990, 2, 300, '[[-3.193,37.335],[-3.18952,37.33652],[-3.18668,37.33442],[-3.18387,37.3322],[-3.18097,37.33046],[-3.17792,37.32955],[-3.17446,37.33095],[-3.1712,37.33123],[-3.16763,37.33327],[-3.165,37.33]]'),
-- Day 3 via Lava Tower acclimatization detour (net camp-to-camp gain only)
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Shira Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Barranco Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 10.0, 135, 3, 360, '[[-3.165,37.33],[-3.16276,37.33685],[-3.15669,37.33728],[-3.15048,37.33746],[-3.14391,37.33704],[-3.13907,37.33952],[-3.13634,37.34555],[-3.13305,37.35064],[-3.12962,37.35551],[-3.123,37.355]]'),
-- Day 4: Barranco Wall scramble, low net gain, high exposure
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Barranco Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Karanga Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 5.5, 45, 3, 270, '[[-3.123,37.355],[-3.12105,37.3547],[-3.11909,37.35438],[-3.11756,37.35535],[-3.1162,37.35681],[-3.11475,37.35804],[-3.11338,37.35946],[-3.1119,37.36057],[-3.11002,37.36051],[-3.108,37.36]]'),
-- Day 5: exposed rock, colder, approaching summit zone
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Karanga Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Barafu Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 3.5, 640, 3, 270, '[[-3.108,37.36],[-3.10502,37.36059],[-3.10192,37.36028],[-3.09871,37.35925],[-3.09578,37.36016],[-3.09299,37.36204],[-3.09023,37.36418],[-3.087,37.363]]'),
-- Summit night: scree/snow/ice, extreme altitude, night ascent
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Barafu Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Uhuru Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 5.0, 1296, 4, 390, '[[-3.087,37.363],[-3.08532,37.36243],[-3.08435,37.36103],[-3.08355,37.35944],[-3.08278,37.35781],[-3.08161,37.35666],[-3.07972,37.35632],[-3.07829,37.35546],[-3.07643,37.35509],[-3.0758,37.3533]]'),
-- Marangu: rainforest, wide hut trail
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Marangu Gate' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Mandara Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 8.0, 860, 2, 280, '[[-3.233,37.49],[-3.23246,37.48536],[-3.23185,37.48075],[-3.23106,37.47624],[-3.22734,37.47324],[-3.22345,37.47032],[-3.2197,37.46733],[-3.2154,37.46463],[-3.21362,37.46063],[-3.215,37.455]]'),
-- Marangu: moorland, steady gradient
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Mandara Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Horombo Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 11.0, 1000, 2, 360, '[[-3.215,37.455],[-3.20567,37.45771],[-3.19838,37.45511],[-3.19187,37.45047],[-3.18648,37.44294],[-3.17968,37.43905],[-3.17367,37.43312],[-3.16649,37.43024],[-3.1581,37.43051],[-3.15,37.43]]'),
-- Marangu: alpine desert, rising altitude risk
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Horombo Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Saddle Junction' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 5.0, 480, 3, 170, '[[-3.15,37.43],[-3.14743,37.42751],[-3.14605,37.42355],[-3.14331,37.42128],[-3.13965,37.42016],[-3.13612,37.41887],[-3.13163,37.41879],[-3.12876,37.41669],[-3.12637,37.41398],[-3.125,37.41]]'),
-- Marangu: barren saddle crossing
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Saddle Junction' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Kibo Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 5.0, 500, 3, 170, '[[-3.125,37.41],[-3.12375,37.40731],[-3.12294,37.40424],[-3.1213,37.40191],[-3.11912,37.40006],[-3.11539,37.39959],[-3.11247,37.3984],[-3.11015,37.39668],[-3.10837,37.39447],[-3.108,37.391]]'),
-- Marangu: summit night, steep scree, little acclimatization
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Kibo Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Gilman''s Point' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 4.0, 985, 5, 240, '[[-3.108,37.391],[-3.10293,37.38995],[-3.10211,37.38426],[-3.09984,37.38014],[-3.09657,37.37712],[-3.09162,37.37595],[-3.08675,37.37468],[-3.085,37.37]]'),
-- Marangu: crater rim to the summit
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Gilman''s Point' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Uhuru Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 2.5, 210, 4, 80, '[[-3.085,37.37],[-3.08253,37.36773],[-3.08256,37.36409],[-3.08207,37.36073],[-3.07893,37.35883],[-3.07678,37.35639],[-3.0758,37.3533]]'),
-- Lemosho: remote forest and moorland, long evacuation distance
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Londorot Gate' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Shira 1 Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 14.0, 1400, 2, 480, '[[-3.19,37.2],[-3.18888,37.20871],[-3.1799,37.21319],[-3.17288,37.21873],[-3.17068,37.22686],[-3.16904,37.23529],[-3.16849,37.24431],[-3.16676,37.2527],[-3.16292,37.25995],[-3.155,37.265]]'),
-- Lemosho: Shira plateau, merges with Machame
((SELECT id FROM mountains WHERE name='Kilimanjaro'),
 (SELECT id FROM waypoints WHERE name='Shira 1 Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 (SELECT id FROM waypoints WHERE name='Shira Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')),
 8.0, 340, 2, 200, '[[-3.155,37.265],[-3.16195,37.27132],[-3.16018,37.27899],[-3.16057,37.28632],[-3.15634,37.29437],[-3.15493,37.30198],[-3.15411,37.3095],[-3.15763,37.31635],[-3.16503,37.3226],[-3.165,37.33]]');

-- ===== MONT BLANC waypoints
INSERT INTO waypoints (mountain_id, name, waypoint_type, latitude, longitude, elevation_m) VALUES
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Chamonix', 'trailhead', 45.9237, 6.8694, 1035),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Nid d''Aigle', 'checkpoint', 45.889, 6.766, 2372),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Tête Rousse Hut', 'camp', 45.859, 6.804, 3167),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Goûter Hut', 'camp', 45.8528, 6.8266, 3835),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Vallot Hut', 'camp', 45.8383, 6.853, 4362),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Aiguille du Midi', 'checkpoint', 45.8788, 6.8873, 3842),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Mont Blanc du Tacul', 'checkpoint', 45.8567, 6.886, 4248),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Col Maudit', 'junction', 45.8539, 6.88, 4345),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Mont Maudit', 'checkpoint', 45.8497, 6.8731, 4465),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Bosses Ridge', 'junction', 45.8344, 6.8605, 4600),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Mont Blanc Summit', 'summit', 45.8326, 6.8652, 4806);

-- ===== MONT BLANC route segments (directed, uphill only)
INSERT INTO route_segments
    (mountain_id, from_waypoint_id, to_waypoint_id, distance_km, elevation_gain_m, safety_score, estimated_time_min, geometry_json)
VALUES
-- Tramway (Mont Blanc Tramway) to Nid d'Aigle. 0 km / 0 m so totals stay hiking-only.
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Chamonix' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Nid d''Aigle' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 0.0, 0, 1, NULL, '[[45.9237,6.8694],[45.889,6.766]]'),
-- Cable car to the Aiguille du Midi.
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Chamonix' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Aiguille du Midi' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 0.0, 0, 1, NULL, '[[45.9237,6.8694],[45.8788,6.8873]]'),
-- Goûter: rocky path to Tête Rousse
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Nid d''Aigle' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Tête Rousse Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 3.5, 795, 2, 200, '[[45.889,6.766],[45.88787,6.77392],[45.88315,6.779],[45.87885,6.78442],[45.87077,6.78685],[45.86387,6.79022],[45.85913,6.79529],[45.859,6.804]]'),
-- Goûter: Grand Couloir crossing, rockfall exposure
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Tête Rousse Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Goûter Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 2.0, 668, 5, 150, '[[45.859,6.804],[45.858,6.80859],[45.85904,6.81373],[45.85636,6.81786],[45.8524,6.82163],[45.8528,6.8266]]'),
-- Goûter: Dôme du Goûter snow slopes, altitude
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Goûter Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Vallot Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 3.0, 527, 3, 140, '[[45.8528,6.8266],[45.85039,6.831],[45.85031,6.83669],[45.84676,6.84046],[45.84299,6.84412],[45.83885,6.84757],[45.8383,6.853]]'),
-- Goûter: approach to the Bosses ridge
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Vallot Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Bosses Ridge' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 1.2, 238, 3, 60, '[[45.8383,6.853],[45.83729,6.85486],[45.83682,6.857],[45.83519,6.85853],[45.8344,6.8605]]'),
-- Trois Monts: glacier, crevasses, includes about 300 m re-ascent from Col du Midi
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Aiguille du Midi' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Mont Blanc du Tacul' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 4.0, 710, 3, 190, '[[45.8788,6.8873],[45.87576,6.88505],[45.87262,6.88457],[45.86937,6.88599],[45.86613,6.88728],[45.86288,6.88863],[45.85978,6.88748],[45.8567,6.886]]'),
-- Trois Monts: broad glacial shoulder
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Mont Blanc du Tacul' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Col Maudit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 2.0, 100, 2, 50, '[[45.8567,6.886],[45.85582,6.88495],[45.85496,6.88389],[45.85485,6.88248],[45.85483,6.88103],[45.8539,6.88]]'),
-- Trois Monts: steepening snow slope
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Col Maudit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Mont Maudit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 1.5, 120, 2, 50, '[[45.8539,6.88],[45.85207,6.87875],[45.85152,6.87672],[45.85127,6.87451],[45.8497,6.8731]]'),
-- Trois Monts: Mur de la Côte, includes re-ascent after the Col de la Brenva
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Mont Maudit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Bosses Ridge' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 3.0, 300, 4, 100, '[[45.8497,6.8731],[45.84641,6.87189],[45.8435,6.87024],[45.84116,6.86788],[45.8397,6.86445],[45.83792,6.86142],[45.8344,6.8605]]'),
-- Shared: exposed summit ridge
((SELECT id FROM mountains WHERE name='Mont Blanc'),
 (SELECT id FROM waypoints WHERE name='Bosses Ridge' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 (SELECT id FROM waypoints WHERE name='Mont Blanc Summit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')),
 1.3, 206, 4, 60, '[[45.8344,6.8605],[45.83342,6.86147],[45.83364,6.8629],[45.83335,6.86414],[45.8326,6.8652]]');

-- ===== MT FUJI waypoints
INSERT INTO waypoints (mountain_id, name, waypoint_type, latitude, longitude, elevation_m) VALUES
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Kawaguchiko Base', 'trailhead', 35.4986, 138.7683, 860),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Yoshida 5th Station', 'checkpoint', 35.399, 138.733, 2300),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Yoshida 7th Station', 'camp', 35.388, 138.731, 2700),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Yoshida 8th Station', 'camp', 35.376, 138.7295, 3250),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Yoshida 9th Station', 'camp', 35.368, 138.7285, 3600),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Fujinomiya 5th Station', 'checkpoint', 35.3248, 138.7334, 2400),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Fujinomiya 7th Station', 'camp', 35.337, 138.731, 3000),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Fujinomiya 9th Station', 'camp', 35.352, 138.729, 3500),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Gotemba 5th Station', 'checkpoint', 35.3187, 138.7686, 1440),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Gotemba 6th Station', 'checkpoint', 35.33, 138.76, 2050),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Gotemba 7th Station', 'camp', 35.34, 138.745, 2850),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Gotemba 9th Station', 'camp', 35.352, 138.734, 3500),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Kengamine Peak', 'summit', 35.3606, 138.7274, 3776);

-- ===== MT FUJI route segments (directed, uphill only)
INSERT INTO route_segments
    (mountain_id, from_waypoint_id, to_waypoint_id, distance_km, elevation_gain_m, safety_score, estimated_time_min, geometry_json)
VALUES
-- Bus transfer to the Yoshida 5th station.
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Kawaguchiko Base' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Yoshida 5th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 0.0, 0, 1, NULL, '[[35.4986,138.7683],[35.399,138.733]]'),
-- Bus transfer to the Fujinomiya 5th station.
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Kawaguchiko Base' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Fujinomiya 5th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 0.0, 0, 1, NULL, '[[35.4986,138.7683],[35.3248,138.7334]]'),
-- Bus transfer to the Gotemba 5th station.
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Kawaguchiko Base' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Gotemba 5th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 0.0, 0, 1, NULL, '[[35.4986,138.7683],[35.3187,138.7686]]'),
-- Yoshida: well-marked trail, many huts
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Yoshida 5th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Yoshida 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 2.8, 400, 1, 120, '[[35.399,138.733],[35.39735,138.73166],[35.39542,138.73185],[35.3934,138.73257],[35.3915,138.73259],[35.38976,138.73174],[35.388,138.731]]'),
-- Yoshida: rocky switchbacks
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Yoshida 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Yoshida 8th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 1.8, 550, 2, 130, '[[35.388,138.731],[35.38572,138.7297],[35.38323,138.7302],[35.38074,138.73059],[35.37824,138.73107],[35.376,138.7295]]'),
-- Yoshida: steeper section, crowding
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Yoshida 8th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Yoshida 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 1.2, 350, 2, 80, '[[35.376,138.7295],[35.37401,138.72918],[35.37191,138.72974],[35.37003,138.72852],[35.368,138.7285]]'),
-- Yoshida: final push to the crater rim
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Yoshida 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Kengamine Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 0.8, 176, 3, 40, '[[35.368,138.7285],[35.36625,138.72757],[35.36427,138.72815],[35.36234,138.7284],[35.3606,138.7274]]'),
-- Fujinomiya: steep loose volcanic rock
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Fujinomiya 5th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Fujinomiya 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 3.0, 600, 3, 150, '[[35.3248,138.7334],[35.32697,138.73368],[35.32874,138.73198],[35.33075,138.73146],[35.33279,138.73108],[35.33514,138.7323],[35.337,138.731]]'),
-- Fujinomiya: steepest stretch, rockfall risk
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Fujinomiya 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Fujinomiya 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 1.8, 500, 4, 120, '[[35.337,138.731],[35.3401,138.73136],[35.34289,138.72936],[35.34577,138.72811],[35.34897,138.72917],[35.352,138.729]]'),
-- Fujinomiya: summit approach
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Fujinomiya 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Kengamine Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 0.7, 276, 3, 60, '[[35.352,138.729],[35.35419,138.72884],[35.35617,138.72752],[35.35841,138.72759],[35.3606,138.7274]]'),
-- Gotemba: long forest and scrub approach
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Gotemba 5th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Gotemba 6th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 6.5, 610, 2, 220, '[[35.3187,138.7686],[35.32064,138.76854],[35.32145,138.767],[35.32226,138.76546],[35.32288,138.76368],[35.32437,138.76302],[35.32585,138.76236],[35.32759,138.76204],[35.32938,138.76178],[35.33,138.76]]'),
-- Gotemba: loose volcanic sand, few huts
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Gotemba 6th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Gotemba 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 4.5, 800, 3, 210, '[[35.33,138.76],[35.33303,138.75931],[35.33387,138.75716],[35.33397,138.75452],[35.33402,138.75184],[35.33517,138.74991],[35.33682,138.7483],[35.33883,138.74693],[35.34,138.745]]'),
-- Gotemba: sparse facilities
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Gotemba 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Gotemba 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 2.5, 650, 3, 150, '[[35.34,138.745],[35.34111,138.74219],[35.34285,138.74008],[35.34592,138.73942],[35.34884,138.73858],[35.35094,138.73686],[35.352,138.734]]'),
-- Gotemba: joins the rim near the summit
((SELECT id FROM mountains WHERE name='Mt Fuji'),
 (SELECT id FROM waypoints WHERE name='Gotemba 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 (SELECT id FROM waypoints WHERE name='Kengamine Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')),
 1.0, 276, 3, 60, '[[35.352,138.734],[35.3548,138.7332],[35.35612,138.73047],[35.35792,138.72836],[35.3606,138.7274]]');

-- ===== Named routes (classic routes, for the stretch toggle)
INSERT INTO named_routes (mountain_id, name, description) VALUES
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Machame Route', 'The "Whiskey Route" - most popular ascent, known for scenic diversity and strong acclimatization profile.'),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Marangu Route', 'The "Coca-Cola Route" - hut-based, the shortest way up and the only route with dormitory huts.'),
((SELECT id FROM mountains WHERE name='Kilimanjaro'), 'Lemosho Route', 'Remote western approach, longer and quieter, joining the Machame line at Shira.'),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Goûter Route', 'The normal route: tramway, Goûter hut, then the Bosses ridge.'),
((SELECT id FROM mountains WHERE name='Mont Blanc'), 'Trois Monts Route', 'Glacier route from the Aiguille du Midi over Tacul and Maudit.'),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Yoshida Trail', 'The most popular trail, with the most huts and facilities.'),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Fujinomiya Trail', 'Highest starting point, so the shortest but steepest climb.'),
((SELECT id FROM mountains WHERE name='Mt Fuji'), 'Gotemba Trail', 'The longest trail, starting low with few huts.');

-- Which segments make up each named route, in order (transfer segments excluded)
INSERT INTO named_route_segments (named_route_id, route_segment_id, sequence_order) VALUES
((SELECT id FROM named_routes WHERE name='Machame Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Machame Gate' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Machame Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 1),
((SELECT id FROM named_routes WHERE name='Machame Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Machame Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Shira Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 2),
((SELECT id FROM named_routes WHERE name='Machame Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Shira Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Barranco Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 3),
((SELECT id FROM named_routes WHERE name='Machame Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Barranco Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Karanga Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 4),
((SELECT id FROM named_routes WHERE name='Machame Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Karanga Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Barafu Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 5),
((SELECT id FROM named_routes WHERE name='Machame Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Barafu Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Uhuru Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 6);

INSERT INTO named_route_segments (named_route_id, route_segment_id, sequence_order) VALUES
((SELECT id FROM named_routes WHERE name='Marangu Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Marangu Gate' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Mandara Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 1),
((SELECT id FROM named_routes WHERE name='Marangu Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Mandara Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Horombo Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 2),
((SELECT id FROM named_routes WHERE name='Marangu Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Horombo Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Saddle Junction' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 3),
((SELECT id FROM named_routes WHERE name='Marangu Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Saddle Junction' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Kibo Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 4),
((SELECT id FROM named_routes WHERE name='Marangu Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Kibo Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Gilman''s Point' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 5),
((SELECT id FROM named_routes WHERE name='Marangu Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Gilman''s Point' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Uhuru Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 6);

INSERT INTO named_route_segments (named_route_id, route_segment_id, sequence_order) VALUES
((SELECT id FROM named_routes WHERE name='Lemosho Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Londorot Gate' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Shira 1 Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 1),
((SELECT id FROM named_routes WHERE name='Lemosho Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Shira 1 Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Shira Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 2),
((SELECT id FROM named_routes WHERE name='Lemosho Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Shira Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Barranco Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 3),
((SELECT id FROM named_routes WHERE name='Lemosho Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Barranco Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Karanga Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 4),
((SELECT id FROM named_routes WHERE name='Lemosho Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Karanga Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Barafu Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 5),
((SELECT id FROM named_routes WHERE name='Lemosho Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Barafu Camp' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Uhuru Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Kilimanjaro'))),
 6);

INSERT INTO named_route_segments (named_route_id, route_segment_id, sequence_order) VALUES
((SELECT id FROM named_routes WHERE name='Goûter Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Nid d''Aigle' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Tête Rousse Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 1),
((SELECT id FROM named_routes WHERE name='Goûter Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Tête Rousse Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Goûter Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 2),
((SELECT id FROM named_routes WHERE name='Goûter Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Goûter Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Vallot Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 3),
((SELECT id FROM named_routes WHERE name='Goûter Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Vallot Hut' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Bosses Ridge' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 4),
((SELECT id FROM named_routes WHERE name='Goûter Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Bosses Ridge' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Mont Blanc Summit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 5);

INSERT INTO named_route_segments (named_route_id, route_segment_id, sequence_order) VALUES
((SELECT id FROM named_routes WHERE name='Trois Monts Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Aiguille du Midi' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Mont Blanc du Tacul' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 1),
((SELECT id FROM named_routes WHERE name='Trois Monts Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Mont Blanc du Tacul' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Col Maudit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 2),
((SELECT id FROM named_routes WHERE name='Trois Monts Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Col Maudit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Mont Maudit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 3),
((SELECT id FROM named_routes WHERE name='Trois Monts Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Mont Maudit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Bosses Ridge' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 4),
((SELECT id FROM named_routes WHERE name='Trois Monts Route'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Bosses Ridge' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Mont Blanc Summit' AND mountain_id=(SELECT id FROM mountains WHERE name='Mont Blanc'))),
 5);

INSERT INTO named_route_segments (named_route_id, route_segment_id, sequence_order) VALUES
((SELECT id FROM named_routes WHERE name='Yoshida Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Yoshida 5th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Yoshida 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 1),
((SELECT id FROM named_routes WHERE name='Yoshida Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Yoshida 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Yoshida 8th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 2),
((SELECT id FROM named_routes WHERE name='Yoshida Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Yoshida 8th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Yoshida 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 3),
((SELECT id FROM named_routes WHERE name='Yoshida Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Yoshida 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Kengamine Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 4);

INSERT INTO named_route_segments (named_route_id, route_segment_id, sequence_order) VALUES
((SELECT id FROM named_routes WHERE name='Fujinomiya Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Fujinomiya 5th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Fujinomiya 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 1),
((SELECT id FROM named_routes WHERE name='Fujinomiya Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Fujinomiya 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Fujinomiya 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 2),
((SELECT id FROM named_routes WHERE name='Fujinomiya Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Fujinomiya 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Kengamine Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 3);

INSERT INTO named_route_segments (named_route_id, route_segment_id, sequence_order) VALUES
((SELECT id FROM named_routes WHERE name='Gotemba Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Gotemba 5th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Gotemba 6th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 1),
((SELECT id FROM named_routes WHERE name='Gotemba Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Gotemba 6th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Gotemba 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 2),
((SELECT id FROM named_routes WHERE name='Gotemba Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Gotemba 7th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Gotemba 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 3),
((SELECT id FROM named_routes WHERE name='Gotemba Trail'),
 (SELECT rs.id FROM route_segments rs
    WHERE rs.mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji')
      AND rs.from_waypoint_id=(SELECT id FROM waypoints WHERE name='Gotemba 9th Station' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))
      AND rs.to_waypoint_id=(SELECT id FROM waypoints WHERE name='Kengamine Peak' AND mountain_id=(SELECT id FROM mountains WHERE name='Mt Fuji'))),
 4);