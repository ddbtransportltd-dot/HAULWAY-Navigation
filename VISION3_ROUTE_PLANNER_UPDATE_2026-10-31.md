# HAULWAY Navigation — Vision 3 Route Planner

Updated the ROUTE PLANNER screen to follow the supplied Vision 3 reference.

## Included
- Multi-stop route list with start, via stops and destination.
- Add and remove via stops.
- Reorder stops using the up/down controls.
- Fastest and shortest route selection from OSRM alternatives when alternatives are returned.
- Avoid option presented with an explicit limitation notice; the public preview service cannot guarantee all avoidance preferences or HGV restrictions.
- OpenStreetMap/Leaflet route preview with numbered stop markers after calculation.
- Calculated journey time and distance, plus Start action to open the existing navigation map.
- Existing bottom navigation retained.

## Important limitation
The route engine remains standard car routing through public Nominatim/OSRM services. It is not HGV-legal routing and must not be used to guide an HGV until truck dimensions, weight, axle limits and road restrictions are supported by a suitable routing engine.

## Build verification
ZIP integrity was checked. A local Gradle build could not be completed because the environment could not resolve `services.gradle.org` to download the Gradle distribution. Build status therefore remains unverified until Android Studio builds it locally.
