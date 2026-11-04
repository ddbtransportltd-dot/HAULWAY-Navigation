# HAULWAY VISION 13 — route signs and POI overlay

Base: `HAULWAY_MASTER_2_1_2_STREET_NAME_BUBBLE_FIX.zip` (version 2.1.2, versionCode 16).

## Integrated changes
- Retains the existing HAULWAY map renderer, GPS marker/follow, route display, street-name bubble, map styles and routing code.
- Regulatory road signs are filtered to OSM road ways that geometrically match the active route. Side-road signs are not displayed as route signs.
- Adds recognizable STOP, GIVE WAY, traffic-signal location, speed-limit and mapped speed-camera symbols.
- Adds a separate POI corridor overlay for mapped fuel, HGV-designated parking, parking/rest areas, toilets, food, shops, repair, wash, weighbridge and accommodation categories, within 1.2 km of the active route.
- Uses Overpass API with a fallback endpoint, viewport requests, zoom threshold 13+, and a 45-second request throttle.
- Bumps Android version to `2.1.3-vision13-route-overlay` / versionCode 17.

## Data limitations
- Coverage depends on OpenStreetMap mapping and Overpass availability.
- Traffic-signal symbols indicate location only; they do not show live red/amber/green phases.
- `maxspeed` is a mapped road limit, not proof of the exact physical sign position.
- HGV parking is shown only when OSM tags explicitly indicate truck/HGV/lorry designation or access; a generic parking POI is not presented as HGV parking.
- This does not add live traffic, verified live camera alerts, live parking availability or guaranteed truck-legal route data.

## Verification status
- Source changes applied to the recovered 2.1.2 project.
- Build and on-device route tests are still required. Do not treat the feature as verified until GitHub Actions succeeds and the overlay is checked on the target tablet.
