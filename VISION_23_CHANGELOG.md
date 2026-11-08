# HAULWAY Vision 23 — Route Guidance & Search

Base: `vision-22-navigation-ui-gps-units`

## Changes
- Driving screen uses the device status header again (Wi-Fi/mobile signal, location, Bluetooth, battery and charge state).
- Initial driving-map zoom is one level wider than the previous default.
- Approach zoom increases as the next route manoeuvre gets closer.
- Added a compact next-manoeuvre panel with roundabout exit parsing and suggested left/right positioning.
- Speed-limit lookup now requests nearby highway ways, prefers `maxspeed:hgv`, reads `maxspeed`, understands common GB national-speed-limit values, and applies HGV statutory defaults by mapped road class where appropriate.
- Road signs and speed-limit markers are fetched only when a route exists and are filtered to a 35 m route corridor.
- TomTom incident results in ROUTE mode are filtered to a 75 m route corridor; the traffic screen defaults to ROUTE and its map draws the active route.
- Address search is a near-full-screen translucent overlay with debounced street/place suggestions.
- Selecting a suggestion closes search, centres the map on the destination and opens a compact place card with Google Street View, SAVE LOCATION and GO.

## Required API secrets
- `TOMTOM_TRAFFIC_API_KEY` — required for real TomTom live incidents.
- `GOOGLE_MAPS_API_KEY` or `GOOGLE_STREETVIEW_API_KEY` — required for Google Street View imagery.
- Keep secret values in GitHub Actions secrets or local `local.properties`; do not commit them.

## Data limitations
- Lane positioning is currently a manoeuvre-derived suggestion (left/right/roundabout exit), not a lane-by-lane guarantee. The current ORS route response does not provide reliable lane-level guidance.
- Speed limits depend on mapped OSM values and the HGV defaults inferred from the mapped road class. Missing or ambiguous road classification can still leave the limit unavailable.
- This change wires route-filtered TomTom incidents; it does not yet add a separate coloured traffic-flow overlay.

## Build status
- Android version: `2.2.7-vision23-route-guidance-search` (`versionCode 31`).
- GitHub Actions has not yet reported a build for this branch. APK compilation and on-device verification remain outstanding.
