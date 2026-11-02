# HAULWAY – OpenRouteService HGV integration

## API key setup

The project reads the key from the root `local.properties` file. Add this line:

```properties
ORS_API_KEY=PASTE_YOUR_HEIGIT_KEY_HERE
```

Do not commit or share `local.properties`. It is excluded by `.gitignore`.

In Android Studio, use **File → Sync Project with Gradle Files** after adding or changing the key.

## What this integration changes

- Replaces the OSRM `driving` route request with HeiGIT OpenRouteService `driving-hgv`.
- Uses the selected vehicle profile's height, width, length, gross weight and axle load.
- Converts weight values from kg to tonnes for ORS.
- Sends hazardous-load routing when the selected load type is `Hazardous`.
- Requests route geometry, segment summaries and text turn instructions.
- `SHORTEST` uses ORS shortest preference; `FASTEST` uses fastest; `AVOID` requests avoidance of ferries and toll roads.

## Important limitations

- HGV restrictions depend on the quality and completeness of OpenStreetMap road attributes. A returned route is not a legal guarantee that every bridge, road or access restriction is suitable for the vehicle.
- Public API quotas and enabled services are controlled by the HeiGIT account dashboard.
- For development, the key is injected into Android `BuildConfig`. A key embedded in an APK can be extracted. A production/commercial release should call a private backend proxy rather than expose a long-lived key in the app.
- The current route planner still uses the existing Nominatim address geocoder. This change updates route calculation, not the address-search provider.

## Endpoint

`https://api.heigit.org/openrouteservice/v2/directions/driving-hgv/geojson`
