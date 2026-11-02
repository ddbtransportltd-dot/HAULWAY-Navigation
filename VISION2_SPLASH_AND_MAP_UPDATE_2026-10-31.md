# HAULWAY — Vision 2 + Splash update (2026-10-31)

## Splash screen
- Removed the baked-in static progress bar from `haulway_splash_background.jpg`.
- Added a live Compose progress track and percentage overlay aligned to the original artwork.
- Progress now animates smoothly to reflect completed startup checks.
- Kept the artwork label below the bar; the bar panel covers the old baked glow so it cannot look like a frozen progress indicator.
- Increased minimum branded intro presentation to 3.6 seconds.
- Android 12+ still displays its mandatory system splash first; the native H mark is expected platform behavior.

## Map
- Carto Voyager is now the first road-map provider, closer to the supplied Vision 2 reference.
- Esri satellite imagery with road-label overlay is the second provider, followed by OSM and OSM France.
- Provider fallback now considers both base map tile errors and Esri road-overlay errors/timeouts.
- Existing 70:30 layout, route casing, zoom controls, GPS marker, right-side manoeuvre rail and bottom six-tab menu are retained.
- WebView is not reloaded on Compose recomposition.

## Validation
- Inline map JavaScript passed `node --check`.
- Android resource-name collision scan found no duplicates.
- ZIP archive integrity checked after packaging.
- Android APK compilation was attempted with `./gradlew --offline assembleDebug` but the wrapper tried to download Gradle 9.6.0 and failed because `services.gradle.org` is unreachable in this environment. Build is therefore NOT verified.

## Important limitations
- The map and route services require internet.
- OSRM routes are car routes, not verified HGV-safe routes. Do not use them for real truck navigation until HGV restrictions and a suitable routing provider are integrated.
