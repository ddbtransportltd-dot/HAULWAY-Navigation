# Vision 17 — Map startup and globe recovery

## Problem
The globe-to-location animation previously changed the map tile zoom continuously on every animation frame. The raster renderer consequently cancelled/restarted requests and could generate invalid fractional zoom tile URLs. This made the map appear to load very slowly and could make the globe intro seem to disappear or fail to transition.

## Change
- Keep tile zoom at an integer.
- Render the detailed location view once after the first location fix.
- Animate only the globe artwork using CSS transform.
- Keep the globe visible until at least one tile at the detailed location view has loaded successfully, then fade it away.
- Start the map at a low integer world zoom while the globe is visible.

## Remaining limits
- The globe is a stylized SVG illustration, not a live satellite globe.
- Tile loading still depends on internet connection, provider availability, API configuration and device performance.
- Full Gradle build and physical-device testing are still required.
