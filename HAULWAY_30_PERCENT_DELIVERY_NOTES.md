# HAULWAY – 30% field-test preparation (2026-11-01)

## Changes in this delivery
- Driving MAP and information rail now use 65:35 width weights inside the same row and therefore share the same top and bottom edges; the bottom navigation remains outside the map row.
- Default free map theme is CARTO Dark Matter (no API key); additional CARTO Voyager and Positron themes remain selectable with the map Layers control. Attribution is shown in the map.
- Follow mode keeps the map north-up and positions the vehicle marker at 70% of viewport height. Tile rendering and route overlays use the same anchor, preventing the vehicle and rendered map centre from disagreeing.
- Compass/heading changes rotate only the vehicle marker. The map scene is explicitly kept unpitched and unrotated. Device rotation is remapped for the current screen orientation; moving GPS bearing takes priority while moving.
- Map search UI and Nominatim result selection were retained; selected results are passed to the map viewport.
- Traffic screen retains all existing categories and adds traffic jams, temporary restrictions, height/weight limits and low bridges. These are category controls only: no live incident/traffic provider is configured, so the UI explicitly says the live feed is not connected. No fabricated incidents are drawn.
- Truck profile defaults now include ARTIC, RIGID, VAN, WAG & DRAG and BUS. Route calculation already reads saved dimensions, gross weight, axle load and hazardous-load setting and sends them to openrouteservice HGV routing.

## Important limitations before road use
- openrouteservice routing requires a valid `ORS_API_KEY` in `local.properties`. Its returned route must be checked against current UK road restrictions; do not treat this prototype as a certified truck navigation system.
- Live traffic events, road closures, height/weight restriction feeds, speed limits and camera data are not integrated. Traffic switches do not create live data.
- CARTO raster basemaps are a temporary no-key provider, not the planned self-hosted OpenFreeMap/Valhalla infrastructure.
- Build could not be run in this environment because Gradle 9.6 distribution download failed (`UnknownHostException: services.gradle.org`). JavaScript syntax was checked with Node.js; Kotlin/Android compilation and on-device GPS/compass testing remain outstanding.
