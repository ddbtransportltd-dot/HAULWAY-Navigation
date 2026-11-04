# HAULWAY VISION18 — Search Overlay & Driving Layout

## Version
- Android versionName: 2.1.8-vision18-search-overlay-layout
- versionCode: 22
- Base: vision-17-navigation-layout-alerts
- Branch: vision-18-search-overlay-layout

## Implemented in source
- Enlarged upcoming manoeuvre cards from 40dp to 54dp.
- Increased manoeuvre icon size, distance text and road/action text for quick glance readability.
- Disabled MapLibre's built-in compass control to prevent overlap with the voice control.
- Increased voice control surface from 38dp to 44dp and icon from 19dp to 25dp, matching the map search button dimensions.
- Replaced the two-column trip summary with four equal-width metrics: Distance left, ETA (remaining travel time), Duration (planned full route duration), and Arrival (estimated local clock time).
- Changed map address search from a centered dialog to a left-side drawer overlay. The map remains visible to the right while the user searches.
- Existing Photon type-ahead search and HGV route calculation are retained for this build; no unverified provider migration or new API key requirement is introduced.

## Search provider comparison (decision note)
### Photon / OpenStreetMap
- Already integrated in HAULWAY and supports address/place search with type-ahead.
- No new paid API key is needed for the current implementation.
- Address completeness depends on OpenStreetMap coverage and the public Photon service; it cannot guarantee a complete list of every building in a postcode.
- Best low-cost baseline for development, but public infrastructure is not an enterprise SLA.

### TomTom Search / Places Search
- TomTom's current documentation recommends Places Search for new integrations; its Search API supports fuzzy address/POI search and an Autocomplete endpoint.
- Strong candidate for a commercial navigation product with dedicated geocoding/search products and a documented free monthly allowance on some products.
- Requires a TomTom API key and a separate integration/test pass; check the exact Places Search SKU allowance and commercial terms before committing.

### Google Places API (New)
- Mature address autocomplete and place details.
- Current pricing list shows a 10,000 free-usage cap for Autocomplete Requests, then usage-based pricing; billing must be enabled and field masks/session tokens should be implemented correctly.
- Potentially strong result quality, but requires Google Cloud configuration, billing controls and careful cost monitoring.

### Nominatim public endpoint
- Not recommended for HAULWAY autocomplete. The OpenStreetMap Foundation explicitly prohibits autocomplete built on its public Nominatim service and limits usage to 1 request/second.
- Do not use the public Nominatim endpoint to power postcode-to-house-number suggestions.

## Recommended next decision
Keep Photon as the no-key fallback while we test identical UK postcode/address queries against TomTom and Google Places. Compare exact house-number coverage, result relevance, latency, international coverage, quotas and expected cost. Select a primary provider only after a real test set; keep provider selection behind a common search interface so HAULWAY can switch or fall back safely.

## Validation status
- Source edits committed to the VISION18 feature branch.
- Android build and device layout verification are still pending.
- Do not treat the larger cards, side drawer or metric spacing as device-approved until the new APK is installed and checked on the Galaxy Tab A11.
