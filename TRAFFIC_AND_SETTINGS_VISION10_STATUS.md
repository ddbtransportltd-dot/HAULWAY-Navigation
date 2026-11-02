# HAULWAY – Settings Vision 10 and Traffic update

## Included in this update
- Settings Vision 10 category navigation remains in the split-panel layout.
- Map appearance selections and the POI / traffic / 3D toggles are saved locally with Android SharedPreferences.
- Map style selector now cycles CARTO labels: Voyager, Dark Matter, Positron.
- Traffic filter list expanded to 24 incident / road-condition categories and filter selections persist locally.
- Traffic UI explicitly indicates that a live incident feed is not connected. No fake accident, closure, or congestion markers are drawn.

## Important integration boundary
CARTO Basemaps provides map tiles and styling; it does not provide live traffic incidents, accident reports, roadworks, closures, speed-camera feeds, or weather hazard reports. The traffic filters are UI preferences only until a separate traffic-data provider is configured and its API response is connected to the map. A CARTO API key alone cannot activate live traffic data.

The settings style choice is stored locally; connecting it to the live WebView map-style state and applying every toggle to map rendering is a separate integration step. Build could not be verified in this environment because Gradle distribution download is blocked by unavailable network access.
