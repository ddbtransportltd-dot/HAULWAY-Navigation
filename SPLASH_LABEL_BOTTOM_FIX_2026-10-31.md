# HAULWAY Splash Label Bottom Fix — 2026-10-31

- Removed the baked-in `LOADING MAPS AND ROUTES...` lettering from `haulway_splash_background.jpg` using image inpainting.
- Recreated the label as a Compose `Text` overlay anchored to the bottom edge, independently of the artwork.
- Kept the progress bar and live percentage as separate Compose elements.
- Kept the Vision 2 map layout, route overlays, GPS marker, map providers and navigation panels unchanged.
- No `.txt` files are present in the project tree or delivery archive.
- Android build was attempted with `./gradlew assembleDebug --offline` but could not start because Gradle 9.6.0 is not cached and the environment cannot reach services.gradle.org.

## Map continuation

- Maneuver distance now follows the route geometry (nearest route vertices and cumulative segment distance) instead of using only straight-line distance when route geometry is available.
- Straight-line distance is retained only as a fallback if route geometry cannot be parsed.
- Maneuver advancement uses the route-aware distance threshold.
