# HAULWAY VISION 12 — source update

Base: HAULWAY VISION 11 source archive supplied by the user.

## Changes in this source update
- GPS marker is larger and has a stronger white/blue contrast outline.
- Vehicle position and bearing are interpolated between GPS fixes in the embedded map renderer instead of relying on a short CSS transition.
- FOLLOW control explicitly recentres and re-enables follow after manual map panning.
- Added a 2D/3D control. 3D is a perspective tilt applied to the existing raster-tile map scene; it is not a full vector-map camera engine.
- Remaining distance is calculated from the current GPS fix along the route geometry to the route endpoint.
- Remaining duration is recalculated proportionally from the original route duration and remaining route distance. This is not live traffic-aware ETA.
- Turn icons are selected from the actual route instruction text instead of always showing a right turn.
- Removed the second/duplicate small trip summary from the left side.
- Reduced the right rail to one active manoeuvre card.
- Changed spoken approach guidance to one 500 m announcement; the old 800 m / 300 m / NOW stages are removed.
- Added HAULWAY branding and a digital clock in the top header.
- App version updated to versionCode 3 / versionName 1.2.0-vision12.

## Known limitation requiring a separate data source
The current project does not contain a verified road speed-limit data provider. The circular speed-limit sign therefore remains a dash (unknown) rather than displaying an invented limit. GPS vehicle speed remains separate.

## Verification
- Source archive structure and CRC integrity checked.
- Kotlin compiler parser check completed with no syntax/parser errors.
- A full Android Gradle build and on-device GPS/map test have NOT been run in this environment. Run the repository GitHub Actions workflow before installing.
