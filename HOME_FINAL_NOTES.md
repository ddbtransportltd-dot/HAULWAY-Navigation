HAULWAY Navigation — HOME finalisation notes

Implemented in this handoff:
- HOME retains the photographic truck background, 3x3 feature grid and six-item bottom navigation.
- Header layout is layered across the full available width: HAULWAY logo left, live device clock centered on the full display, live device indicators right.
- Clock is read from the Android device time and refreshed every second.
- Status values are read from Android services: active network transport, location services enabled, Bluetooth adapter state, battery level and charging state. They refresh every second.
- Tile styling, spacing and typography remain close to the supplied Vision 1 reference while preserving the current 16:10 tablet layout.

Important limitations:
- The reference is a raster mock-up, so exact pixel-for-pixel reproduction is not possible across different screen sizes/densities. Layout is responsive.
- This project is a UI/home-screen handoff, not a road-tested truck navigation product. Map/routing and truck restriction data must be implemented and validated separately before use for real driving decisions.
- Build was not verified in this environment because the Gradle 9.6.0 distribution is not cached and network download is unavailable. Build with Android Studio before installing.
