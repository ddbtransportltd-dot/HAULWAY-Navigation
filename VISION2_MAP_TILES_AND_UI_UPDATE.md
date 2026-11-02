# HAULWAY Navigation — Vision 2 map update

This update continues the existing full Android Studio project and preserves the HOME screen, branding, splash screen, 70:30 navigation layout, route planner, and truck profile.

## Changes in this stage

- Added a raster map tile fallback chain: OpenStreetMap standard tiles, CARTO Voyager, then OSM France.
- The map loading overlay now remains visible until a tile actually loads and reports a useful connection error if all providers fail.
- Added delayed map-size invalidation and resize handling for Android WebView layout changes.
- Kept route rendering on top of the map and retained the GPS marker/accuracy circle.
- Zoom controls now set a predictable zoom relative to the base map zoom instead of accumulating offsets incorrectly.
- Removed fabricated next-turn distance/road details from the top manoeuvre panel when no route has been calculated.
- Removed fabricated speed, speed-limit, ETA and distance values from the left panel; these now show unknown / route summary until real sources exist.
- Right-side route steps now show the parsed distance and instruction separately.
- Route overview now says “No active route” until an actual route result exists.

## Important limitations

- The map uses public raster tile providers and requires internet access. Public tile providers can rate-limit or be unavailable; this update tries fallbacks but does not provide offline maps.
- OSRM route planning is still ordinary car routing, not HGV-legal routing. Do not use it as a truck-safe route.
- Speed, legal speed limit, live ETA, turn-by-turn voice guidance, live traffic and HGV restrictions are not yet connected to authoritative live services.
- Android APK compilation has not been verified in this environment because Android SDK and the required Gradle distribution are not installed here.
