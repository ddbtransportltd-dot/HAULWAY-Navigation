# Vision 2 – WebView map refresh and route overlay fix

- Fixed `RealNavigationMap` so Compose recompositions no longer call `WebView.reload()`.
- The previous update branch had two identical `pageReady` conditions; the second branch (zoom and route synchronization) was unreachable.
- Map zoom is now sent to the live Leaflet instance without restarting the page.
- Newly calculated route geometry is sent once when it changes, allowing the route polyline and bounds to appear on the existing map.
- Tile loading indicator now waits for at least three successful tile loads before hiding; repeated tile errors trigger provider fallback sooner.

Validation: extracted the inline map JavaScript and passed `node --check`. Android Gradle build was not run because this environment has no configured Android SDK and no cached Gradle 9.6.0 distribution.

Important: routing still uses public OSRM car routing and is not HGV-safe. Do not use it for real truck navigation until a truck-aware routing engine and restrictions data are integrated and validated.
