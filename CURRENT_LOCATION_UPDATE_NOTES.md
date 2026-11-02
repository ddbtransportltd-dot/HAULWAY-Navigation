# HAULWAY V3 CARTO — Current Location update

Changes in `app/src/main/java/com/czerman/haulway_navigation/MainActivity.kt`:
- The first route stop can now be set to `CURRENT_LOCATION` from the start-location edit dialog using **USE CURRENT LOCATION**.
- Uses Android fine/coarse location permission and the freshest available last-known provider fix.
- Route geocoding recognizes the internal `CURRENT_LOCATION|latitude|longitude` token and sends the coordinates directly to the HGV routing service instead of attempting address geocoding.
- The start-stop row displays **Current Location** and indicates that selecting it again refreshes the GPS position.
- If no location fix is available, the UI explains that the user should wait for GPS and retry.

Build verification: not completed in this environment because Gradle 9.6.0 could not be downloaded (network DNS unavailable). Build on the user's Android Studio / GitHub Actions environment before installing.
