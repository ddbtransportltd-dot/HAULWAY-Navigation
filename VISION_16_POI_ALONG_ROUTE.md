# VISION 16 — POI search along route

## Added in 2.0.4 master preview
- Services & POI and Truck Parking now offer two search scopes: Near Me and Along Route.
- Along Route samples up to six points from the currently calculated route geometry and searches a 2.5 km radius around each sample point.
- Results from overlapping search circles are de-duplicated by OpenStreetMap element ID and sorted by distance to the nearest sampled point.
- If no route is calculated, Along Route reports that a route must be calculated first.
- Near Me remains a 12 km search around the current GPS fix.

## Limitations
- This is a sampled corridor search, not continuous coverage of every metre of the route. Short POIs between sample points can be missed.
- Distance shown for Along Route is distance to the nearest sampled route point, not exact perpendicular distance to the route or driving detour.
- OSM categories and attributes are incomplete; a generic parking node is not proof that HGVs are allowed or that a space is available.
- Uses the public Overpass API; availability, rate limits and response times are outside HAULWAY's control.
- No APK build verification was possible in the offline environment.
