# HAULWAY Vision 2 — live GPS speed update

Date: 2026-10-31

## Implemented
- The driving screen now requests location permission from one place rather than prompting separately from the map WebView.
- The driving screen subscribes to Android LocationManager updates while it is visible.
- Vehicle speed is displayed in mph only when the location provider supplies a real speed measurement (`Location.hasSpeed()`).
- If permission, provider, or a speed measurement is unavailable, the screen shows `GPS speed unavailable` rather than a fabricated speed.
- Location updates are removed when the driving screen leaves composition.
- The existing Vision 2 layout, 70:30 map/right rail, splash screen, branding, route planner and truck profile are retained.

## Limitations
- This is a live GPS speed readout, not map-matched road speed or a legal speed-limit feed.
- The speed-limit badge remains unavailable until a trusted road restriction/speed-limit data source is integrated.
- Route calculation is still standard OSRM car routing and is not HGV-safe.
- Android build was attempted with `:app:compileDebugKotlin --offline`, but Gradle 9.6.0 was not cached and the environment could not reach `services.gradle.org`; compilation therefore remains unverified.
