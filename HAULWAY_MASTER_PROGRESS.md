
## Iteration 2 — My Places foundation
- Replaced the generic MY PLACES placeholder with a functional local saved-place screen.
- Users can add a name + address/postcode, see saved entries, remove entries, and send a selected address to Route Planner.
- Saved places persist in Android SharedPreferences across app restarts.
- Route Planner now initializes its destination field from the selected saved place.
- Remaining work: edit place, resolve and persist coordinates, Home/Work favourites, import/export, Room migration and sync.

## Build verification
- `bash gradlew --offline assembleDebug` attempted.
- Build could not start because Gradle 9.6.0 is not cached and network DNS is unavailable (`UnknownHostException: services.gradle.org`).
- This is an environment/download limitation, not a confirmed Kotlin compile result. A real Gradle sync/build is still required in Android Studio.

## Iteration 3 — Route History foundation
- Calculated route plans are now persisted locally (up to 50 records).
- Added a History screen showing origin, destination, route summary and explicit PLANNED status.
- Users can delete saved route-plan entries.
- Corrected the route-planner warning: the current provider uses HGV routing mode, but map-data/provider restrictions are not guaranteed complete. The UI now tells drivers to obey actual road signs rather than incorrectly claiming this is standard car routing.
- Remaining: completion detection, actual driven distance/time, date display, replay/edit route and export.

## Iteration 4 — Nearby Services & POI / Truck Parking
- Replaced the static Services & POI and Truck Parking placeholder lists with a live nearby search screen backed by OpenStreetMap Overpass data.
- Added categories: truck parking, fuel, truck wash/car wash, food, repairs, weighbridges and rest areas.
- Searches use the device's last known current location, a 12 km radius, loading/error/empty states and nearest-first sorting.
- Added a directions hand-off for each result using Android's navigation intent; HAULWAY does not claim this is its own in-app guided route yet.
- Added OpenStreetMap attribution and a warning that mapped results may be incomplete.
- Truck Parking opens with the parking category selected; Services & POI opens with Fuel selected.
- Limitations: results depend on OSM tagging/coverage; the search is nearby-only (not yet along the active route), and Overpass availability/rate limits can affect response. No live availability, truck-space count, opening status or verified HGV suitability is inferred.

## Build verification for 2.0.3
- ZIP/source checks are performed at packaging time.
- Full Android Gradle build remains unverified in this environment because the required Gradle distribution is not cached and network access to services.gradle.org is unavailable.


## 2.0.4 — Vision 16 POI along route
- Added Near Me / Along Route search scopes to Services & POI and Truck Parking.
- Along Route queries OSM around up to six sampled points on the active route, de-duplicates OSM elements and sorts by distance to the nearest sample.
- Added `VISION_16_POI_ALONG_ROUTE.md` with limitations and data-quality notes.
- Bumped Android versionCode to 8 and versionName to 2.0.4-master-preview.
- ZIP integrity can be checked separately; Android compilation remains unverified without Gradle distribution access.


## 2.0.5 — Vision 17 map startup / globe recovery
- Fixed a severe tile-rendering regression in the globe-to-location animation: the previous animation changed raster tile zoom on every animation frame, causing repeated render cancellation and fractional tile zoom URLs.
- Map tile zoom now stays at an integer and is rendered once at the target detail level; the globe artwork performs the visual zoom independently.
- Startup map begins at a low integer world zoom while the globe is visible; on first location fix, the detailed map is rendered underneath and the globe fades only after a location tile loads successfully.
- This restores the intended globe intro and avoids repeatedly restarting tile downloads during the animation.
- Bumped Android versionCode to 9 and versionName to 2.0.5-map-startup-fix.
- JavaScript syntax check passed with Node; Android Gradle compilation/device performance remain unverified in this environment.


## 2.0.6 — Smooth map camera and GPS marker sync
- Added continuous CSS camera scaling for zoom gestures; only requests a new integer tile zoom after the transition, avoiding fractional tile URLs and repeated requests.
- Keeps the previous tile layer visible until the replacement viewport has loaded, then fades the camera scale back to native tile scale.
- Recalculates the GPS marker screen position from its geographic fix during manual panning instead of translating the marker DOM with the tile layer; GPS follow remains disabled after manual map interaction.
- JavaScript syntax validated with Node. Android/Gradle build and on-device smoothness still require verification.

## Master 2.0.9 — Smooth pan and persistent tile cache
- Preserve loaded tiles from the current zoom/style when the user pans; reposition cached tiles instead of discarding them immediately.
- Keep a bounded 96-tile LRU cache to reuse nearby map imagery and avoid repeated network requests.
- Keep old map imagery visible while missing neighbouring tiles load.
- Fix GPS/overlay double translation during manual pan: map imagery translates, overlays are recalculated from geographic coordinates.
- Manual map interaction disables auto-follow until the explicit Follow control is used.
- Build version: 2.0.9 (versionCode 13).
- JavaScript syntax checked; Android build and on-device performance remain unverified.

## Master 2.1.0 — Zoom tile handover and partial-load protection
- During zoom, keep the previous complete map layer visible while target zoom tiles download.
- Hide target zoom tiles until the complete visible viewport has loaded; prevents isolated tile squares replacing the map.
- Remove fallback zoom tiles only after full target viewport coverage succeeds.
- Keep a neutral map background behind raster tiles.
- Lengthen zoom transition to 720ms with eased motion.
- Build version: 2.1.0 (versionCode 14).
- JavaScript syntax checked; Android build and real-device zoom behavior remain unverified.
