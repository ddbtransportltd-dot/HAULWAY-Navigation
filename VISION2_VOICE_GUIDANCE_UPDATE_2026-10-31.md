# HAULWAY Vision 2 — Voice Guidance Update

## Changes
- Added Android TextToSpeech voice guidance to the MAP screen.
- Uses the device's UK English speech voice (`Locale.UK`).
- Announces the active route maneuver at approximate approach bands: 800 m, 300 m, and 120 m ("now").
- Each announcement stage is spoken once per maneuver to avoid repeating on every GPS update.
- Existing speaker control enables/disables voice announcements.
- Speech engine is stopped and released when the navigation screen is disposed.
- Reset announcement state when a new route is loaded or no route maneuvers remain.

## Limitations
- Announcements use the route steps currently supplied by the existing routing provider. They are not HGV-safe directions.
- The 800 m / 300 m prompts are approximate thresholds, not guaranteed exact distances.
- Voice quality and available language depend on Android's installed Text-to-Speech engine and voice data.
- Android APK compilation was attempted with `./gradlew assembleDebug --offline`, but the wrapper tried to download Gradle 9.6.0 and failed because `services.gradle.org` was unreachable. Build is therefore unverified.
