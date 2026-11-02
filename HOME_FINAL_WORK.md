# HAULWAY HOME — final implementation pass

This pass updates the actual HOME screen in `MainActivity.kt`:
- Replaces the hard-coded `10:24` and decorative status glyphs with a centered live device clock.
- Reads active Wi-Fi/mobile transport, Android location-service state, Bluetooth adapter state (with Android 12+ runtime permission request), and battery percentage/charging state.
- Keeps the existing HOME background, 3x3 tile layout, tile actions and bottom navigation.
- Adds `ACCESS_NETWORK_STATE` and `BLUETOOTH_CONNECT` permissions.

## Build status
A local Gradle build was attempted with `./gradlew assembleDebug --offline --no-daemon`, but Gradle Wrapper tried to fetch Gradle 9.6.0 and the environment could not resolve `services.gradle.org` (`UnknownHostException`). Therefore compilation is **not verified** here. Build in Android Studio before installing.

## Known scope boundary
This package finalizes the HOME header/device-status implementation pass; it does not turn the existing placeholder map into a certified truck-routing engine. Do not rely on the current app for truck-safe route guidance until routing, restrictions and on-road behaviour have been separately implemented and tested.
