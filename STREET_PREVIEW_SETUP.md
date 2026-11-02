# HAULWAY 360 Street Preview setup

Add credentials locally to the project root `local.properties` (never commit this file):

```properties
GOOGLE_MAPS_API_KEY=your_google_maps_javascript_api_key
MAPILLARY_ACCESS_TOKEN=your_mapillary_access_token
```

`GOOGLE_MAPS_API_KEY` is the canonical key name used by the embedded WebView for **Maps JavaScript API + Street View Panorama**. For compatibility, Gradle also accepts the older `GOOGLE_STREETVIEW_API_KEY` name, but new installations should use `GOOGLE_MAPS_API_KEY`.

In Google Cloud enable **Maps JavaScript API** and billing. The interactive `StreetViewPanorama` is part of the Maps JavaScript API; Street View Static API is not required for the interactive viewer. Ensure the key's API restrictions permit Maps JavaScript API and its application restrictions are compatible with WebView usage. Do not put the key in source code or commit `local.properties`.

Mapillary uses its Graph API access token to find nearby images and MapillaryJS to display a selected image. KartaView is attempted as a no-key fallback; its response schema and coverage may change, and it may provide a single image rather than a 360 panorama.

The screen accepts an address/postcode and resolves it with Android Geocoder, or accepts coordinates directly. AUTO tries Google Street View first, then Mapillary, then KartaView. Coverage varies by location.
