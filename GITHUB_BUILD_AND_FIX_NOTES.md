# HAULWAY — GitHub build package

This package is based on the latest available project archive, `HAULWAY_DOWNLOAD.zip` (2 November 2026).

## Changes in this update

- Route calculation now opens the MAP / driving-navigation screen automatically after a successful calculation. No second tap on `Start` is required.
- The embedded map now sizes itself to the WebView bounds instead of `100vw` / `100vh`, reducing viewport sizing gaps.
- The 3D map plane has a slightly larger centred scale and a deeper tile overscan buffer to prevent exposed dark/black strips at the top and edges when tilted.
- A GitHub Actions workflow builds the debug APK on pushes to `main` / `master` and on manual dispatch.

## Upload to GitHub from a tablet

1. Extract this ZIP.
2. Open the `HAULWAYNavigation` folder.
3. Upload the **contents of that folder** to the root of `ddbtransportltd-dot/HAULWAY-Navigation` (including the hidden `.github` folder). Do not upload the outer `HAULWAYNavigation` folder as a nested directory.
4. In GitHub, open **Actions** and select **HAULWAY Android Build**. It also starts automatically after a push to `main` or `master`.
5. When the run finishes, open its run page and download the `HAULWAY-APK-debug` artifact.

## GitHub Actions secrets

Add these under Settings → Secrets and variables → Actions if the corresponding services are used:

- `ORS_API_KEY`
- `CARTO_API_KEY`
- `GOOGLE_STREETVIEW_API_KEY` (or `GOOGLE_MAPS_API_KEY`)
- `MAPILLARY_ACCESS_TOKEN`

The source code does not contain local API keys. Missing optional secrets are passed as empty values; some providers/features will then be unavailable.

## Important validation note

The source and ZIP structure have been checked, but this environment does not have the Android SDK installed, so an Android APK compile and on-device test have **not** been claimed. GitHub Actions is configured to perform the actual Android build.
