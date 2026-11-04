# HAULWAY VISION 14 — Native MapLibre + Globe

## Changes
- Replaced the JavaScript/WebView tile renderer in the driving map with the native MapLibre Android MapView used by the earlier HGV Navigator project.
- Replaced the Route Planner's WebView map with the same native MapLibre renderer.
- Uses OpenFreeMap Liberty vector style, with a Carto Dark Matter alternate style from the existing map-layer control.
- Adds an in-map GLOBE/MAP switch using MapLibre Native's globe projection build.
- Keeps route polyline, live vehicle position, search marker, native pinch/drag gestures, and the existing HAULWAY Compose interface.
- MapLibre Android OpenGL ES globe build pinned to `2026.10.19-globe`.

## Important test notes
- This is an experimental MapLibre Native globe release; verify both Globe and Map modes on the Samsung tablet.
- The first migration pass restores native base-map rendering, route line, GPS marker, and search marker. The former WebView-only OSM road-sign/POI marker overlays still need to be ported to native GeoJSON layers in a follow-up pass.
- A successful CI build is not a substitute for on-device rendering and globe interaction tests.
