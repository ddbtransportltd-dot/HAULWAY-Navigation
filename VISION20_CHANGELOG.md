# Vision 20 — Address autocomplete, in-app road preview, map cleanup

- Adds an exact UK postcode lookup against OpenStreetMap address objects tagged with addr:postcode, merging house-number/street results with Photon suggestions.
- Removes the vehicle-location bias from postcode queries so GPS position does not suppress requested postcode results.
- Prioritizes house-number/street suggestions when available.
- ROAD PREVIEW now opens a large in-app WebView instead of launching the external Google Maps app. It uses Google Maps Embed API Street View when GOOGLE_MAPS_API_KEY is configured; otherwise it loads the Street View web URL inside the WebView.
- Filters common bus-stop/bus-station/public-transport symbols out of the MapLibre base-style layers while keeping other POIs.
- Keeps the existing GPS recenter control and route planner behavior.
- Build version: 2.2.0-vision20-address-preview-map-cleanup (versionCode 24).

## Data limitations
House-level postcode suggestions depend on address objects actually being mapped with addr:postcode, addr:housenumber, and addr:street in OpenStreetMap. Some UK postcodes will still have fewer suggestions where those address tags are missing. Google Street View coverage and API key/API enablement also affect the in-app preview.
