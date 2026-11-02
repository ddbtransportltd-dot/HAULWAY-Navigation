# HAULWAY Navigation — Vision 3 Route Planner

The Route Planner screen has been rebuilt around the supplied Vision 3 reference.

- Landscape two-column composition: route list on the left, satellite map and route summary on the right.
- Route rows with numbered markers, road/leg labels, and move controls.
- Optional service/break stop row (30 min) that is a stopover annotation, not a geocoded routing waypoint.
- Add waypoint field and Fastest / Shortest / Avoid selection.
- Esri World Imagery map preview; calculated route draws a blue polyline and numbered stop markers.
- Real OSRM route calculation updates duration and distance; Start opens navigation after a route is calculated.
- Bottom navigation and the surrounding HAULWAY section header remain unchanged.

Important limitation: route calculations still use standard car routing (OSRM). They are not validated for HGV dimensions, weight, axle limits, restricted roads, or truck-specific access. The Avoid option is a UI choice only and is not guaranteed by the current routing backend.

Build status: ZIP integrity checked. Kotlin compilation could not be run because Gradle 9.6.0 is not cached and this environment cannot reach services.gradle.org.
