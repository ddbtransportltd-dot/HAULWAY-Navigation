# HAULWAY Navigation — Master Project Guide

## Identity
- Product: HAULWAY Navigation
- Android application ID / namespace: `com.czerman.haulway_navigation`
- Kotlin + Jetpack Compose
- Current consolidated source: Vision 15 Exact Road Signs
- Master package: `2.0.0-master-preview`, versionCode 6

## Source of truth
Read these in order:
1. `HAULWAY_MASTER_PRODUCT_SPEC_PL.md`
2. `HAULWAY_MASTER_AUDIT_PL.md`
3. `HAULWAY_MASTER_ROADMAP_PL.md`
4. Vision change logs for historical detail.

## Existing implementation
- Branded HOME/splash and navigation sections.
- Driving screen with embedded raster-tile WebView map, GPS marker, current GPS speed, follow, zoom, 2D/3D, route display, street-name bubble and voice guidance.
- Route planner with geocoding, multi-stop routing, break rows and route preview.
- ORS HeiGIT `driving-hgv` request that reads the selected profile's dimensions, gross weight and axle load, plus hazardous-load flag.
- Five vehicle profile types and local profile storage.
- Traffic filter UI (live incident feed is not connected).
- 360 imagery screen/provider setup (requires configured provider credentials and device testing).
- Vision 15 globe intro and road-sign overlay restricted to active route and matched OSM road geometry.

## Important engineering limitations
- HGV routing is integrated, but OpenStreetMap restriction coverage and routing provider data are not complete enough to guarantee legal access or clearance.
- Current map renderer is raster tiles in a WebView; 3D is a perspective effect, not a full vector camera. Vector labels are the target architecture.
- Traffic screen stores filters but no live incident provider is connected.
- Parking/services, My Places, History and several settings remain partial or placeholder sections; see the master audit.
- Offline maps and offline routing are not implemented.
- Street imagery provider coverage, credentials, billing and terms must be checked.
- Never commit `local.properties` or API secrets.

## Build and device verification
The archive has been checked for ZIP integrity. A full Gradle build, APK installation and on-device tests have not been verified in this preparation environment. Run Gradle Sync and `assembleDebug` in Android Studio, then test on Lenovo Tab M8 TB300FU and Samsung Galaxy Tab A11 SM-X135F.
