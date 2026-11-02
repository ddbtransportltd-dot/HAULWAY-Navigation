# HAULWAY Navigation – delivery notes (31 Oct 2026)

This source project carries forward the existing HOME, the branded launcher icon, the full-screen HAULWAY startup artwork, and the 70:30 driving-navigation layout with the six bottom tabs.

## Startup screen
- Full-screen artwork: `app/src/main/res/drawable-nodpi/haulway_splash_background.png`.
- The Compose startup screen overlays its own progress bar and status text; the bar is not a timed animation.
- Progress advances after app settings/package access, image-resource decoding, location-permission checks, and navigation-resource checks complete.
- A location permission check is not a GPS fix. The startup sequence does not claim that a satellite fix or route calculation has completed.

## Driving navigation screen
- Map area and right-side rail use a 70:30 width split.
- Six bottom navigation tabs remain visible.
- The zoom-out control uses a minus icon.
- The map canvas is still a stylised placeholder. Live map tiles, live GPS position, truck-aware route calculation, traffic data, and spoken turn guidance are NOT yet integrated.
- Example route values (M62, Leeds, Hull, distances, speed and ETA) are demonstration content, not live navigation data.

## Build status
The archive contains the Android Studio source project and Gradle wrapper. It has not been compiled in this environment because an Android SDK installation is not available here. Please run `Build > Make Project` in Android Studio and share the first Kotlin error line if any appears.
