# HAULWAY – corrections based on the 31 Oct screen recording

## Observed in the recording
- A long black interval appears before the branded splash artwork.
- The splash lasts only briefly and its baked-in loading bar/caption is duplicated by the Compose overlay.
- The map screen opens with a flat green canvas and no visible road tiles.
- The driving screen in the recording still shows fixed sample values (60 mph limit, 56 mph, 68 mi) and a fixed blue position arrow, which are not live data.

## Changes in this update
- Replaced the cropped truck-grille Android system splash bitmap with a simple HAULWAY road-mark vector, so Android 12+ has a visible, clean brand mark instead of a blank/black icon slot.
- Removed the disposable WebView warm-up and its 7-second wait. It created and destroyed a different WebView and did not warm the map instance used later.
- Optimised the full splash artwork to a 1280x800 JPEG, retaining the artwork and baked-in loading caption/track.
- The Compose progress fill now overlays the existing track; removed the duplicate second caption/card that was colliding with the artwork.
- Map background is now dark slate rather than green, so missing tiles no longer look like a valid map.
- CARTO Voyager is attempted first, then OpenStreetMap and OSM France. The loading overlay only disappears after adequate tile coverage; partial or failed map loading remains clearly reported.
- Added WebView HTTP/resource error logging to Logcat under `HAULWAY_MAP`.
- Removed the always-visible fake blue position arrow; the vehicle marker is only drawn after an actual GPS fix.
- Replaced the empty red speed-limit badge with a neutral `LIMIT —` indicator until a real road-speed-limit source is connected. GPS speed remains unavailable until a real GPS speed reading arrives.

## Limitations
- These changes are source-level; Android compilation and tablet runtime were not available for verification in this environment.
- Map tiles still require internet access. If all tile providers are blocked/offline, the app now displays an explicit map-unavailable message rather than a misleading blank green canvas.
- Routing remains ordinary-car OSRM routing, not certified HGV routing. Do not use it for truck guidance until HGV restrictions and a suitable routing engine are integrated.
