# HAULWAY Vision 2 — route matching update

- Improved GPS-to-route matching by projecting each live GPS fix onto the nearest route segment instead of selecting only the nearest route vertex.
- The travelled route overlay now ends at the snapped position on the route, reducing visual jumps when route vertices are sparse.
- The vehicle accuracy ring turns amber when the GPS position is more than 90 metres from the planned route; it returns blue when back near the route.
- Existing follow mode remains active while the map is not manually dragged. Dragging pauses follow; the recenter control restores it.
- Kept the 70:30 map/panel layout, six bottom navigation tabs, current splash assets and route/map provider configuration.

## Validation

- Extracted inline map JavaScript passed `node --check`.
- Android XML resources parsed successfully.
- No duplicate drawable resource names detected.
- All `.txt` files were removed recursively from the project before packaging.
- Android Gradle build was not verified in this environment.

## HGV safety

The route provider is still standard OSRM car routing. Route matching and live GPS display do not make a route truck-legal. Do not use it for HGV guidance until a truck-routing engine and restrictions are integrated.
