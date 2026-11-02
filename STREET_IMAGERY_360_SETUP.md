# HAULWAY 360° Street Imagery – integration notes

## What is included

- New `360° Street View` tile on the HAULWAY home screen.
- Compose screen with coordinate input and provider selection: AUTO / GOOGLE / MAPILLARY / KARTAVIEW.
- AUTO provider order: Google Street View → MapillaryJS → KartaView.
- Google Street View uses the interactive Maps JavaScript API panorama viewer.
- Mapillary uses Graph API image discovery and MapillaryJS for interactive street imagery.
- KartaView uses its public nearby-photo endpoint as a last-resort still-image fallback. A KartaView result is not assumed to be a full 360° panorama.
- API credentials are read from root `local.properties` and are not committed.

## Configure credentials

Open the `HAULWAYNavigation` project root and create or edit `local.properties` (this file is git-ignored). Keep existing ORS/CARTO entries and add:

```properties
GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_MAPS_PLATFORM_KEY
# Alternatively: GOOGLE_STREETVIEW_API_KEY=YOUR_GOOGLE_MAPS_PLATFORM_KEY
MAPILLARY_ACCESS_TOKEN=YOUR_MAPILLARY_CLIENT_TOKEN
```

`local.properties.example` shows the expected names. Do not paste real keys into source files or commit them to GitHub.

### Google

1. Create/select a project in Google Cloud Console.
2. Enable billing and the **Maps JavaScript API**.
3. Create an API key and restrict it to the APIs required by the app.
4. Set appropriate quotas and budget alerts before testing.
5. Add the key to `GOOGLE_STREETVIEW_API_KEY`.

Google dynamic panorama loads are billable independently from ordinary map loads. The current integration checks for a nearby outdoor panorama before creating the viewer. The Gradle build accepts either GOOGLE_MAPS_API_KEY or GOOGLE_STREETVIEW_API_KEY, so an existing Google Maps Platform key can be reused without renaming it. Google key restrictions and Google Maps Platform terms must be configured for the final Android distribution environment.

### Mapillary

1. Sign in at Mapillary and register an application to obtain a client access token.
2. Add it as `MAPILLARY_ACCESS_TOKEN`.
3. Ensure the token/app has access to the Graph API.

MapillaryJS is loaded from its published CDN build. The integration searches a small bounding box around the selected coordinate, prefers `is_pano` imagery when present, and then opens the selected image in MapillaryJS. Coverage and image type vary by location.

### KartaView

The integration currently calls the public nearby-photo endpoint documented by KartaView. Public photo/sequence queries generally do not require a token. The viewer displays a returned image when the endpoint supplies a usable image URL. It is a fallback, not a promise of interactive 360° navigation.

## Current limitations / production hardening

- The screen currently accepts latitude/longitude. The next integration step is to pass coordinates from HAULWAY address autocomplete and selected route stops.
- Provider coverage is not uniform. A location can have no imagery in all three sources.
- Mapillary and KartaView imagery may be ordinary forward-facing photos rather than 360° panoramas.
- Provider network/API behaviour must be tested on a real Android device and across representative countries.
- For a public commercial release, move provider credentials and request orchestration behind a controlled backend where provider terms permit; do not assume a mobile BuildConfig value is secret. Apply attribution, caching, retention, privacy and usage limits per provider terms.
- This integration is an imagery preview, not a truck-routing or road-clearance validation feature.

## Build

Open the `HAULWAYNavigation` folder in Android Studio, allow Gradle Sync, then run `Build > Make Project`. A build could not be verified in the preparation environment because Gradle distribution download was blocked by unavailable network access (`UnknownHostException: services.gradle.org`).
