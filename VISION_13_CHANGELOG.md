# HAULWAY VISION 13 — OSM road signs and POI overlay

Base: user-supplied HAULWAY-Navigation source archive (VISION 12).

## Changes
- Added a map overlay that queries OpenStreetMap data through the Overpass API when the map is zoomed in (zoom 13+).
- Added recognizable map symbols for mapped STOP signs, GIVE WAY signs, traffic-signal locations, road speed-limit tags, parking/rest areas, fuel, HGV-designated parking where tagged, toilets, restaurants/fast food, car repair, car wash and weighbridges.
- Added viewport-based marker placement and a 45-second request throttle; only a small bounding area around the map center is requested.
- Added a fallback Overpass endpoint.
- Updated Android version to 1.3.0-vision13 / versionCode 4.

## Important data limitations
- Coverage depends on what OpenStreetMap contributors have mapped in the area. Missing objects will not appear.
- OSM traffic-signal markers show the location of a signal only. They do not indicate the live red/amber/green phase.
- This update does not provide live traffic jams, accident feeds, road closures or live speed-camera alerts. Those require a separate licensed/current data provider.
- A road's `maxspeed` tag can describe the applicable road limit; it is not necessarily the surveyed position of a physical speed-limit sign.
- The HGV parking category is inferred only where OSM tags explicitly indicate HGV/truck/lorry access or designation.

## Verification
- Source archive extracted and edited.
- A full Android build could not be run in this environment because the Gradle wrapper distribution could not be downloaded (network DNS unavailable). GitHub Actions must complete successfully before installing the APK.
- No on-device map test has been performed.
