# HAULWAY Vision 15 – Globe Start & Zoom Repair

- Naprawiono bazową wartość zoomu w Compose: startowy poziom UI odpowiada zoomowi ulicznemu 16, a przyciski + / - zmieniają go stopniowo.
- Żądany poziom przybliżenia jest zapamiętywany do czasu uzyskania pierwszej pozycji GPS, więc Compose nie nadpisuje wewnętrznego poziomu mapy.
- Dodano startowy ekran globusa w stylu nawigacji satelitarnej (wektorowa kula Ziemi, kontynenty i atmosfera).
- Po otrzymaniu pierwszej pozycji GPS mapa przechodzi animacją z widoku świata do lokalizacji użytkownika w ciągu ok. 4,8 s, a globus płynnie zanika.
- Po zakończeniu przejścia działa dotychczasowa mapa kafelkowa, GPS, znaki i warstwy. Globus jest ekranem startowym, nie zamiennikiem danych mapowych.

Build APK nie został wykonany w tym środowisku; należy sprawdzić kompilację w Android Studio.


## Route-only road signs
- Znaki STOP, ustąp pierwszeństwa, sygnalizacja, fotoradary i ograniczenia prędkości są pobierane/wyświetlane wyłącznie, gdy istnieje geometria aktywnej trasy.
- Usunięto z warstwy znaków parkingi, stacje paliw, sklepy i pozostałe POI.
- Każdy marker jest filtrowany względem przebiegu trasy; wyświetlane są tylko obiekty w korytarzu do 28 m od linii trasy, aby ograniczyć znaki z dróg pobocznych.
- Po wyczyszczeniu trasy wszystkie znaki znikają natychmiast; po wyznaczeniu nowej trasy dane są pobierane ponownie.


## Exact road-ID sign matching
- Dopasowanie geometrii aktywnej trasy do konkretnych dróg OSM (9 m, minimum 2 próbki i 35% zgodności).
- Znaki węzłowe są wyświetlane tylko wtedy, gdy ich identyfikator OSM należy do dopasowanej drogi.
- Ograniczenia prędkości z tagów dróg pochodzą wyłącznie z dopasowanych identyfikatorów dróg.
- Znaki z dróg bocznych, parkingi, paliwo i POI nie są dodawane do warstwy.


## Vision 15.1 / Master 2.0.7 — My Route map startup
- My Route's separate WebView now explicitly starts the globe intro; it no longer depends on a GPS first-fix callback that this screen does not receive.
- The globe remains over the map until a usable detail tile loads, using the existing reveal completion path.
- Empty My Route state starts at regional zoom 5 instead of zoom 12 to avoid an unnecessarily heavy first tile burst.
- CARTO raster requests use standard 1x PNG tiles instead of 2x images to reduce transfer and decode cost.
- Build version: 2.0.7 (versionCode 11).
- Android build and on-device behavior still require verification in Android Studio / tablet.
