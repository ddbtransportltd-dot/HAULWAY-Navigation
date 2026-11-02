# HAULWAY Navigation — Vision 2 map polish

This update keeps the existing full Android Studio project and refines the driving-map screen.

## Changes
- Preserved the 70:30 map/sidebar split and six-item bottom navigation.
- Made zoom controls bounded and deterministic: zoom buttons now set the map zoom relative to a fixed base level instead of cumulatively applying an ever-growing delta.
- Replaced the ambiguous second plus-like control with an explicit minus glyph.
- On the first successful GPS fix, the map recentres on the device position; subsequent fixes update the position/accuracy marker without repeatedly stealing map control.
- Improved map loading/error message visibility.
- Kept OpenStreetMap tiles and attribution.

## Still demonstration data
The M62 route geometry, manoeuvre distances, speed limit, current speed, ETA, and route overview are still demonstration values. This is not yet truck-aware route guidance. The map uses real OSM tiles when internet access is available; GPS position requires Android location permission and a working location provider.

## Build status
The project ZIP was packaged and archive integrity checked. A full Gradle/Android build was not run in this environment.
