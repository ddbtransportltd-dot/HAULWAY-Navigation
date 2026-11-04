# HAULWAY Vision 7 — TomTom Traffic setup

Vision 7 now contains a TomTom Orbis Traffic API v2 incident client.

## Add the API key safely

1. Create or open a TomTom developer project and enable access to the **Traffic API** product.
2. Copy the project's API key.
3. In GitHub, open **Settings → Secrets and variables → Actions → New repository secret**.
4. Set the secret name to `TOMTOM_TRAFFIC_API_KEY` and paste the key as its value.
5. Run the **HAULWAY Android Build** workflow again.

For a local Android Studio build, add this line to the untracked `local.properties` file in the project root:

```properties
TOMTOM_TRAFFIC_API_KEY=your_key_here
```

Do not commit `local.properties`, paste the key into Kotlin source, or place the key in a public URL.

## What the integration currently does

- Requests current incidents from TomTom Orbis Traffic API v2.
- Uses the current device GPS area for **NEAR ME** and **MAP** scopes.
- Uses the active route geometry to calculate the search bounding box for **ROUTE** scope.
- Displays provider-returned incident type, severity, description, affected road and reported delay.
- Filters the incident list using the corresponding enabled Vision 7 layers.
- Shows request errors and the last successful refresh time; it does not fabricate sample incidents.

## Current boundaries

- Live incident cards are implemented; incident geometry is parsed but is not yet drawn on the map.
- The existing CARTO basemap and shared map engine are intentionally unchanged.
- TomTom incidents do not replace authoritative HGV routing restrictions. Height, weight, width, length, axle and vehicle restrictions still need a dedicated authoritative data source and validation.
- Availability, coverage, quotas and commercial terms depend on the TomTom account and selected plan.
