# HAULWAY — Truck Profile stage

- Replaced the static TRUCK PROFILE placeholder list with an editable vehicle profile screen.
- Fields: vehicle type, height, width, overall length, gross weight, maximum axle weight, maximum speed, and load/restriction description.
- Save validates positive numeric dimensions/weights/speed and stores values in Android SharedPreferences on the device.
- Values are restored when the profile screen is reopened or the app restarts.
- The UI explicitly warns that the saved profile is not yet consumed by the current OSRM car-routing engine. Do not use the current route output as HGV-safe guidance.

Build status: not verified. `./gradlew assembleDebug --offline` could not start because the Gradle 9.6.0 distribution is not cached and the environment cannot resolve services.gradle.org.
