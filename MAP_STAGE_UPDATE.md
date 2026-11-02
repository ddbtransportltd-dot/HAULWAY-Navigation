# Navigation map stage update

## Implemented
- Real interactive OpenStreetMap tiles rendered through Leaflet in Android WebView.
- Pan and pinch zoom through Leaflet; plus/minus controls in the app call the map zoom API.
- Runtime location permission request (fine/coarse) and WebView geolocation bridge.
- Live current-position marker and accuracy circle when Android location is granted and available.
- Visible status if the Leaflet library or map tiles cannot be loaded.
- Existing navigation UI overlays, 70:30 layout, side rail and six bottom tabs retained.

## Still demo / not yet production navigation
- Leeds–Hull route geometry is a visual sample line, not a routing-engine result.
- Distances, speed limit, speed, ETA, next manoeuvres and services in the panels remain sample values.
- No truck profile constraints are applied to routing yet.
- No voice turn-by-turn instructions or live traffic integration yet.

Map data © OpenStreetMap contributors. Public OSM tiles require internet access and must be used in accordance with the tile usage policy. For production-scale deployment, choose a tile provider/service plan suitable for the app's traffic.
