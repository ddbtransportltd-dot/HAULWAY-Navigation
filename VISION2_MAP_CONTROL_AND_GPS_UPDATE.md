# HAULWAY Vision 2 — map controls and GPS update

## Changes
- Added a map-layer switch control to toggle between CARTO Voyager road mapping and Esri satellite imagery with road labels.
- Passed the Android GPS accuracy radius to the map marker instead of always using a fixed 18 m circle.
- Passed heading as unavailable when Android has no bearing instead of pretending the vehicle is facing north.
- Made the recenter action event-based so Compose recompositions do not repeatedly seize map control from the user.
- Kept route matching monotonic within a bounded forward search window to reduce backward jumps caused by GPS jitter near junctions.
- The travelled route line is updated only when the GPS fix is within 120 m of the planned route; off-route fixes no longer paint a false travelled segment.
- Removed the duplicate browser geolocation watcher; Android LocationManager is the single location source for the embedded map.

## Validation
- JavaScript syntax checked with Node.js.
- XML resources parsed.
- ZIP archive integrity checked.
- Confirmed there are no `.txt` files anywhere in the project archive.
- Android build not verified: Gradle wrapper needs Gradle 9.6.0, and this environment cannot reach services.gradle.org.

## Limitations
- Routing is still based on the existing OSRM car profile and is not suitable as certified HGV routing.
- Legal speed limits, truck restrictions, live traffic and spoken turn guidance are not yet connected to authoritative data sources.
