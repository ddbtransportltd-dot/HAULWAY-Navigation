# HAULWAY Navigation — Shared Map Engine Fix V3

Changes in this test package:

- Route Planner preview and calculated-route preview now use the same embedded OpenStreetMap tile renderer as the live navigation map.
- Removed the Route Planner's separate Leaflet CDN and ArcGIS satellite WebViews, eliminating the dependency that could leave the Route panel as an empty blue rectangle when the external Leaflet script failed to load.
- Calculated routes are drawn on the shared map with a blue route line and visible green start / red destination markers.
- Adjacent raster tiles overlap by 2 CSS pixels to hide hairline seams caused by WebView fractional-pixel compositing.
- Zoom, GPS and follow-triggered map redraws are coalesced to one animation frame to avoid duplicate redraws when Android sends several updates together.

Validation:
- Embedded map JavaScript extracted from the Kotlin raw string and passed `node --check`.
- ZIP integrity checked after packaging.
- Android Gradle compilation could not be run in this environment because the Gradle 9.6.0 distribution is not cached and network access to services.gradle.org is unavailable.
- Device testing is still required on the Samsung Galaxy Tab A11.
