# HAULWAY map follow and compass correction V1.3

- The map canvas is strictly 2D. Removed map pitch, perspective and map rotation.
- FOLLOW recentres the 2D map around the live vehicle position while keeping the vehicle marker in the lower map area.
- Compass/heading rotates the vehicle arrow only; it does not rotate or tilt the map.
- Existing 65:35 map/information layout is preserved.

## Map provider status
The current tile URLs are still OpenStreetMap/CARTO. They have deliberately not been silently replaced with another provider because this project does not contain a TomTom Maps API key or another confirmed licensed tile provider. A provider change is a separate integration and needs a provider/key.
