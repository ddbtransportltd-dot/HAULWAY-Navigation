# HAULWAY Navigation — Master project

Android Studio project for HAULWAY Navigation, implemented with Kotlin and Jetpack Compose.

## Included screens

- **Vision 1 — HOME:** branded HAULWAY driver home screen and launcher/startup artwork.
- **Vision 2 — MAP:** driving-navigation layout with map panel, manoeuvre/information rail, navigation controls and bottom navigation.
- **Vision 3 — ROUTE:** route planner with address stops and individually represented break stops. Break durations are summed once and break rows are excluded from address routing.
- **Vision 4 — TRUCK PROFILE:** editable profiles for ARTIC, RIGID, VAN, WAG & DRAG and BUS, with 3D vehicle artwork and editable dimensions/weights. The profile list now scrolls independently and includes per-profile deletion while retaining the ability to add profiles.

## Open in Android Studio

1. Extract the ZIP.
2. Open the extracted project root (the folder containing `settings.gradle.kts`).
3. Allow Gradle sync to finish.
4. Connect an Android device with USB debugging enabled and press Run.

## Important current limitations

- The current route request in `MainActivity.kt` uses the public OSRM **car** profile. It is not truck-aware and must not be treated as HGV-safe navigation.
- The researched next routing step is an `openrouteservice` `driving-hgv` adapter, with vehicle dimensions/weights mapped to its HGV options. This adapter is not yet integrated; it will require an API key and must respect the provider's public quota/terms.
- OSM restriction data can be incomplete. Even after HGV routing is integrated, route output must not be represented as a guarantee of legal access or clearance.
- Build status for this delivery is **not verified**. In the current build environment, Gradle 9.6.0 could not be downloaded because `services.gradle.org` could not be resolved. Please run `Build > Make Project` in Android Studio on your PC and share the first error if one appears.

See `MASTER_PROJECT_GUIDE.md` for the current scope and QA notes.
