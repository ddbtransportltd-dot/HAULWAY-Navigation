# HAULWAY — Route Planner stage

Implemented in this stage:
- Route Planner now has editable start and destination fields.
- Address lookup uses OpenStreetMap Nominatim (sequential requests with a delay).
- Road route calculation uses the public OSRM demo routing service.
- Returned route geometry is drawn on the Leaflet map.
- Returned route distance, estimated duration and a short turn-by-turn step preview are shown.
- The map no longer draws the previous hard-coded Leeds–Hull sample route when no route has been calculated.
- The navigation side rail and route overview use the calculated route data when available.

Important limitations:
- OSRM's public demo profile is standard car routing, not HGV routing. It does not enforce truck height, width, length, weight, axle limits, hazardous cargo, bridge restrictions, or HGV prohibitions. Do not use these routes as safe/legal truck guidance.
- Public Nominatim/OSRM endpoints are community services with usage limits and no availability guarantee. Production use needs a compliant hosted provider or self-hosted routing/geocoding stack.
- ETA is the routing engine's estimate and does not include live traffic, mandatory breaks, tachograph rules, or HGV-specific speeds.
- Voice guidance and live manoeuvre progression are not implemented in this stage.

Build status: archive/source checks only. A full Android Gradle build was not run in this environment.
