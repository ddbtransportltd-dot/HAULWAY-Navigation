# HAULWAY VISION 19 — Route Intelligence

## Branch
- Branch: `vision-19-route-intelligence`
- Base: tested Vision 18 branch `vision-18-search-overlay-layout`
- Android versionName: `2.1.9-vision19-route-intelligence`
- versionCode: `23`

## Changes committed
- Road context lookup now requests road geometry and compares the vehicle position to road vertices instead of relying only on a way's centre point. Lookup radius increased to 120 m.
- Road-name overlay is hidden when the service returns an unavailable/placeholder name.
- Road alerts are queried from sampled points along the active route ahead of the current GPS position, filtered to objects close to the route, instead of querying a broad 650 m circle around the vehicle.
- Removed the separate DURATION tile from the map trip summary; Distance Left, ETA and Arrival remain. Duration calculations remain available internally for ETA.
- Photon autocomplete now requests more results and formats house number + street before locality/postcode, improving the usefulness of detailed address suggestions.
- Added a ROAD PREVIEW action to search suggestions. It opens Google Maps Street View panorama for the selected result coordinates where imagery is available.
- Manual map gestures now suspend GPS follow and preserve the user's current camera. The existing recenter button remains the way to resume follow mode.
- Android version metadata bumped to 2.1.9 / versionCode 23.

## Known limitations / verification still required
- Speed limits are only displayed when the nearest matched OSM road has usable `maxspeed` data. The change improves road matching but does not invent legal limits where the map data is missing; those cases may still show a dash.
- Road signs, traffic lights and cameras depend on mapped OpenStreetMap data and public Overpass availability. This is route-ahead filtering, not a guarantee that every real-world sign exists in the database.
- ROAD PREVIEW opens the external Google Maps Street View experience; coverage and in-app behaviour depend on Google Maps availability on the device.
- Photon is still the public autocomplete provider. A full postcode does not guarantee every house number is mapped; verify postcode results on the target device.
- Android build and Galaxy Tab A11 device tests have not yet been run for this branch. Do not consider this release device-approved until the APK builds and the updated app is tested.
