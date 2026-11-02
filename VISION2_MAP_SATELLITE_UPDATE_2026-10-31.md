# HAULWAY — Vision 2 map update (2026-10-31)

## Changes
- Switched the primary map base layer to Esri World Imagery with the Esri World Transportation road/label overlay to better match the supplied satellite-style Vision 2 reference.
- Retained CARTO Voyager, OpenStreetMap and OSM France as fallbacks.
- Improved tile-load/fallback handling: the loading overlay is dismissed after the first three successfully loaded base tiles; provider fallback occurs after repeated tile errors or a 9-second timeout.
- Updated the live vehicle marker to a blue directional pointer with an accuracy circle.
- Kept the route polyline/casing and fit-to-route behavior; invalid route geometry now produces a visible error rather than silently failing.
- Hardened WebView image/network settings and enabled hardware rendering for the map view.
- Kept the 70:30 map/panel layout and six bottom navigation tabs unchanged.
- Confirmed there is only one `haulway_splash_background` resource (JPG) in this project copy, avoiding the duplicate JPG/PNG merge error from the supplied build log.

## Limitations
- Map layers require an internet connection and are subject to provider availability, terms and attribution.
- OSRM remains ordinary-car routing, not HGV-safe routing. The truck profile is not yet enforced by the route engine.
- No APK build or on-device Lenovo test was performed for this update.
