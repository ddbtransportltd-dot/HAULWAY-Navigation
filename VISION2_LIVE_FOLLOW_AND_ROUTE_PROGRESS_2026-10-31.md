# HAULWAY Vision 2 — live map follow and route progress

- Native Android GPS coordinates, speed and bearing are now passed into the Leaflet map from the same Android location listener.
- Vehicle marker and accuracy circle update from native GPS fixes; the map no longer depends solely on WebView geolocation.
- Map follows the vehicle while navigation is active. Dragging the map disables auto-follow; the new recenter control restores it.
- Vehicle marker rotates using the GPS bearing when available.
- A muted travelled-route line is drawn up to the closest route vertex while the remaining route stays blue.
- Added a recenter-on-vehicle control to the map overlay.
- Kept Vision 2 layout (70:30), six bottom tabs, route panel, map providers and splash resources.

Validation: inline JavaScript syntax checked with Node; XML resources parsed; drawable-nodpi resource names checked for duplicates. Android Gradle build could not run because Gradle 9.6.0 is not cached and the environment cannot resolve services.gradle.org.

Safety limitation: route calculation still uses public OSRM car routing and is not suitable as truck-legal HGV navigation. GPS map-follow and travelled-route visualization do not change that.
