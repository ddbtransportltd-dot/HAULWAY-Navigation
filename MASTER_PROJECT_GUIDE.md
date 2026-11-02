# HAULWAY Navigation — Master project guide

## Project identity

- App: HAULWAY Navigation
- Android application ID / namespace: `com.czerman.haulway_navigation`
- Language/UI: Kotlin + Jetpack Compose
- Project path on the developer's PC: `C:\\Users\\dczer\\AndroidStudioProjects\\HAULWAYNavigation`
- Intended test devices: Lenovo Tab M8 TB300FU and Samsung Galaxy Tab A11 SM-X135F (4 GB RAM)

## Product goals

Build an accessible HGV navigation app that remains usable on modest Android tablets, not only expensive/high-performance devices. Keep unnecessary background work and animation costs low, avoid excessive memory use, and test both devices for map rendering, GPS responsiveness, screen transitions, route planning, and profile editing.

## Vision scope

### Vision 1 — HOME

Retain the existing branded home screen and launcher/startup artwork. Do not redesign this screen unless explicitly requested.

### Vision 2 — MAP

Retain the driving screen and navigation controls. The current source uses an OSRM public driving route endpoint; this is a car route and is not suitable as authoritative HGV guidance. Do not imply truck safety, bridge clearance, or legal access until a proper HGV engine and restriction-data validation are in place.

### Vision 3 — ROUTE

- Maintain the 50:50 route-list/map layout and address-search overlay.
- Do not prefill fictional start/destination addresses; the user chooses real addresses.
- Support multiple address stops and long-press drag reordering.
- Every break is a separate stop row, independently editable/deletable (e.g. 15-minute and 30-minute breaks can coexist).
- Add each break duration exactly once to total journey time. Break rows are not geocoded and do not contribute to road distance.
- Show road distance/time between real address stops when routing data is available.
- Do not advertise route-avoidance features as functional unless implemented by the routing engine.

### Vision 4 — TRUCK PROFILE

- Support ARTIC, RIGID, VAN, WAG & DRAG, and BUS.
- Preserve each profile's editable dimensions, gross weight, axle weight, and vehicle settings.
- Profile list must scroll vertically, allow deleting an individual profile, and allow adding profiles after deletion.
- Keep the 3D vehicle artwork and display the currently edited dimensions.
- Vehicle dimensions/weights are currently stored locally using Android SharedPreferences. The route engine does not yet consume them for HGV routing.

## Routing-engine direction

The selected initial candidate is openrouteservice with the `driving-hgv` profile. Its request should receive the active profile's height, width, length, gross weight, axle load and relevant vehicle options. Keep routing behind an adapter/interface so a future self-hosted Valhalla or ORS backend can replace the public API. No API key is included in this project. Review free-tier quotas and commercial terms before any public release.

## Changes made during master consolidation

- Recovered the latest archived project from 31 October 2026 at 03:41 UTC, `HAULWAYNavigation_VISION4_FIVE_VEHICLE_TYPES_3D_FINAL.zip`.
- Preserved the Vision 3 split-break implementation and the five vehicle illustrations.
- Updated the Vision 4 profile list to scroll independently.
- Added a delete control to each profile row. Deleting a profile compacts the saved index-based dimension/weight values and keeps at least one profile, while the Add Profile control remains available.
- Rewrote the README to accurately describe current functionality and limitations.

## Verification status

- Source archive integrity: checked before editing.
- Final archive integrity: must be checked after packaging.
- `.txt` files: none are intended in the final archive; verify recursively after packaging.
- Android/Gradle build: not verified. `./gradlew assembleDebug --offline` attempted to fetch Gradle 9.6.0 but failed because `services.gradle.org` could not be resolved in this environment.
- Device tests: not yet performed on either tablet after consolidation.

## 360° Street Imagery

A first multi-provider preview has been added under the `360° Street View` home tile. Setup and provider limitations are documented in `STREET_IMAGERY_360_SETUP.md`. Configure `GOOGLE_STREETVIEW_API_KEY` and `MAPILLARY_ACCESS_TOKEN` in local.properties before testing; KartaView is used as a public imagery fallback.


## CARTO key handling / OSM fallback (2 November 2026)

- CARTO raster basemaps now require a CARTO Basemaps API key. An empty key can still return HTTP 200 tile images carrying an `API KEY REQUIRED` watermark, so HTTP error fallback alone cannot detect this.
- The map renderer now selects CARTO styles only when `CARTO_API_KEY` is present in the root `local.properties`.
- When no CARTO key is configured, the renderer uses the OpenStreetMap raster tile service instead, so the base map does not depend on the Street View API keys or CARTO credentials.
- This fallback is intended for development/testing. Before public-scale production, select a tile provider and hosting/usage plan whose terms and capacity fit HAULWAY's commercial/global deployment.
