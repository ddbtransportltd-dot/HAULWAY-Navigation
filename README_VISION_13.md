# HAULWAY VISION 13 — Route signs and POI

This update adds an OpenStreetMap/Overpass overlay to the existing HAULWAY navigation map.

- STOP, GIVE WAY, traffic-signal locations, mapped speed limits and speed cameras are shown only when associated with OSM road geometry matching the active route.
- Mapped POIs are shown separately within 1.2 km of the active route: fuel, explicitly HGV-designated parking, other parking/rest areas, toilets, food, shops, repair, wash, weighbridges and accommodation.
- Requests are made at zoom level 13 or closer and throttled to avoid repeatedly querying Overpass.

Coverage depends on OSM data. Signal phase, live traffic, live camera alerts and live parking availability are not supplied by this overlay. See `VISION_13_CHANGELOG.md` for details.

Build using the repository's GitHub Actions workflow and test on the target tablet before installing the APK.
