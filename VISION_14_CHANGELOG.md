# HAULWAY VISION 14 — Map Rendering & Voice Guidance

## Included in this source package

### Navigation header
- Replaced the redundant `HGV NAVIGATION` label with the existing live device status header.
- Header now shows Wi-Fi/mobile network, location, Bluetooth and battery status alongside the HAULWAY brand and clock.

### Voice guidance
- Speech is now deduplicated by the physical manoeuvre identity (coordinates + instruction + road name), rather than the transient manoeuvre list index.
- The speech de-duplication key is no longer cleared whenever route geometry refreshes. This prevents the same prompt being replayed after GPS updates or a same-destination reroute.
- Voice uses one concise advance cue per manoeuvre, with no stacked 300 m / NOW prompts.
- Instructions are normalized to short actions such as “turn left”, “turn right”, “continue straight” and “make a U-turn”.
- TTS speech rate is set slightly slower for clearer in-cab delivery.
- `QUEUE_FLUSH` remains in use so stale queued instructions cannot play over a newer instruction.

### Map rendering
- Tile images overlap by one CSS pixel to hide raster tile hairline seams.
- Map and scene backgrounds now match the light basemap rather than exposing the dark WebView background in uncovered pixels.
- 3D transform is less aggressive and centred, reducing the large blank/black areas caused by perspective projection.
- Added a compact live street-name bubble beneath the vehicle marker. It uses throttled Photon reverse geocoding (at most one lookup per 15 seconds, and only after meaningful movement).

## Version
- Android `versionCode`: 5
- Android `versionName`: `1.4.0-vision14`

## Validation notes
- Source-level changes and archive structure were checked.
- A full Android Gradle build still needs to be run in Android Studio or GitHub Actions with the project's Android SDK and configured API secrets.
- Reverse-geocoded street labels require an internet connection and may be unavailable where the geocoder has no road result.


## Korekta interfejsu – pojedynczy pasek statusu
- Usunięto powielone ikony sieci/Wi-Fi/baterii z górnej belki panelu manewrów.
- Wskaźniki urządzenia pozostają wyłącznie w nagłówku w prawym górnym obszarze aplikacji.


## Korekta dymka ulicy
- Dymek pod pojazdem ma tło o kryciu 45%, wyraźniejszy tekst i nieco większą szerokość.
- Dymek pokazuje wyłącznie pole `street` z geokodowania zwrotnego; kod pocztowy, miejscowość ani ogólna nazwa obiektu nie zastępują nazwy ulicy.


## Szybszy start mapy i znaków (Vision 14.1)
- Mapa może natychmiast ustawić się na ostatniej znanej pozycji z ostatnich 2 minut, a następnie skorygować ją świeżym GPS.
- Lokalizacja sieciowa i GPS są uruchamiane równolegle, zamiast czekać wyłącznie na GPS.
- Zapytanie OSM o znaki drogowe i POI ma mniejszy obszar oraz krótszy limit czasu, aby szybciej zwracać pierwsze wyniki.
- Uwaga: jeśli urządzenie nie ma zapisanej ostatniej pozycji lub nie ma dostępu do sieci, nie da się zagwarantować natychmiastowego ustalenia pozycji/znaków.


## Płynne śledzenie pojazdu (Vision 14.2)
- Zastąpiono restartowaną animację 900 ms ciągłym wygładzaniem pozycji GPS.
- Nowe odczyty GPS aktualizują cel animacji bez anulowania aktualnej klatki, dzięki czemu marker i przesuw mapy nie zatrzymują się i nie przeskakują co sekundę.
- Kierunek jazdy jest również wygładzany po najkrótszej drodze kątowej.
