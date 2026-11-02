# HAULWAY CARTO Layers fix

- The Layers button now cycles CARTO Dark, Voyager and Light only. OpenStreetMap remains a fallback if CARTO tiles fail.
- CARTO_API_KEY is read from root `local.properties`, Gradle property `-PCARTO_API_KEY=...`, or environment variable `CARTO_API_KEY` (in that order).
- For GitHub Actions, add a repository Actions secret named `CARTO_API_KEY`; pass it to the Gradle process as an environment variable.
- For Android Studio, add `CARTO_API_KEY=your_key` to the root `local.properties`. Do not commit that file or paste the key into source code.
- If the key was not present when the APK was built, the app displays an explicit missing-key message when Layers is pressed. Rebuild the APK after configuring the key.

This patch wires the CARTO key into BuildConfig and fixes style cycling. It does not claim to implement live traffic data; traffic overlays require a separate live data provider.
