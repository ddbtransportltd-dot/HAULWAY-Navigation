# HAULWAY Navigation — Vision 3 Route Planner

This project contains the consolidated Vision 3 route-planning screen.

## Route planner
- Two-column tablet layout: itinerary on the left and satellite map on the right.
- Multi-stop route with start, intermediate waypoints, service stop and destination.
- Add and remove intermediate stops; reorder them with the up/down controls.
- Service stop is a real geocoded waypoint (default example: Doncaster Services, UK), with a 30-minute break label. It can be removed or added again.
- Fastest and Shortest select from routes returned by OSRM when alternatives are available.
- Route geometry is drawn on the map and markers use OSRM's snapped waypoint coordinates rather than evenly spaced points along the route.
- Displays calculated journey duration and distance; Start passes the calculated route to the MAP screen.
- Existing HAULWAY bottom navigation and other screens are retained.

## Important limitations
- Routing is supplied by the public OSRM car-routing demo server. It is not HGV-aware and must not be used as a truck-legal route.
- The Avoid option is shown for design continuity but is not enforced by the current routing backend.
- The service-stop default is an editable example location, not a recommendation or guaranteed rest facility. Confirm the chosen stop independently.
- Public Nominatim/OSRM services have usage limits and require an internet connection.

## Build status
The source was updated and the ZIP archive was validated. A Gradle build could not be completed in this environment because the Gradle 9.6.0 distribution host was unreachable.
