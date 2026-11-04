# HAULWAY MASTER — AUDYT STANU KODU
Stan sprawdzenia: 3 listopada 2026 r. Źródło: projekt Vision 15 Exact Road Signs.

Statusy:
- **W KODZIE** — istnieje implementacja w obecnym projekcie.
- **CZĘŚCIOWO** — część logiki istnieje, ale brakuje źródła danych, pełnej integracji, UX lub testów.
- **BRAK / DO ZROBIENIA** — jest pomysł, opis lub placeholder, ale nie pełna funkcja produkcyjna.

| Obszar | Stan źródła | Brak / ryzyko |
|---|---|---|
| HOME / branding / splash | W KODZIE | Testy responsywności i finalna kontrola grafiki |
| Driving map / raster tiles | W KODZIE | Docelowy renderer wektorowy, ostre etykiety, kontrola licencji i skali |
| GPS marker / speed / accuracy | W KODZIE | Testy terenowe, filtr jakości i oszczędzanie baterii |
| FOLLOW / pan / zoom / 2D-3D | W KODZIE | Regresja zoomu z Vision 14.2/15 wymaga testów na urządzeniu |
| Globus i animacja startowa | NAPRAWA 2.0.5 | Animacja grafiki niezależna od kafelków; szczegółowa mapa odsłania się po załadowaniu kafelka; wymaga testu na urządzeniach |
| HGV routing ORS | W KODZIE | Wymaga klucza ORS; niepełne OSM restrictions nie gwarantują legalnego przejazdu; backend produkcyjny i audyt parametrów |
| Profile ARTIC/RIGID/VAN/WAG & DRAG/BUS | W KODZIE | Dodatkowe pola: trailer, osie, ADR i limity muszą być w pełni spójne z parametrami routingu |
| Route planner multi-stop | W KODZIE | Testy skrajnych przypadków, alternatyw i błędów geokodowania |
| Break stops 15/30 min | W KODZIE | Testy sumowania i reordering |
| Autocomplete adresów | W KODZIE | Publiczny provider i limity; globalna jakość/adresy |
| Street bubble | W KODZIE | Photon/Internet; brak nazwy ulicy oznacza pusty dymek; dopasowanie map-matching do jezdni |
| Voice guidance | W KODZIE | Testy deduplikacji, progu, języka, TTS i reroutingu w kabinie |
| Znaki na drodze aktywnej trasy | CZĘŚCIOWO | OSM mapowanie może być niepełne; dokładne dopasowanie way/node i geometrii wymaga testów skrzyżowań/jezdni równoległych; obecny pobór ma obszar widoku, nie całą trasę |
| Parking HGV | CZĘŚCIOWO | Ekran/kategorie istnieją, ale potrzebne prawdziwe wyszukiwanie, filtry, szczegóły i wiarygodne dane |
| Services & POI | CZĘŚCIOWO | Menu i kategorie są, lecz pełny katalog/lista/mapa/nawiguj wymaga integracji |
| Live Traffic | BRAK | Panel filtrów istnieje, ale sam komunikat informuje, że feed live nie jest podłączony |
| My Places | CZĘŚCIOWO → CRUD podstawowy w tej iteracji | Dodano lokalne dodawanie/usuwanie nazwanych miejsc i przekazanie adresu do Route Planner; nadal brak edycji, geokodowania przy zapisie, współrzędnych, importu/eksportu i synchronizacji |
| History & Statistics | CZĘŚCIOWO → historia planów w tej iteracji | Zapisuje obliczone plany tras lokalnie i pozwala je usuwać; to nie jest jeszcze rejestr ukończonych podróży, brak daty w UI, statystyk rzeczywistej jazdy i eksportu |
| Settings | CZĘŚCIOWO | Część preferencji jest zapisywana; trzeba podłączyć wszystkie przełączniki do zachowania mapy/nawigacji |
| 360° Street imagery | CZĘŚCIOWO | Integracja Google/Mapillary/KartaView opisana i ekran istnieje; wymaga kluczy, testów pokrycia, billing/attribution i przejścia z wyszukiwania współrzędnych na adres/stop |
| Offline maps | BRAK | Nie ma pełnego pobierania, zarządzania pakietami, routingu offline ani aktualizacji offline |
| Vector map labels / true 3D camera | BRAK | Obecna mapa to rasterowy renderer WebView z perspektywą 3D |
| Global commercial data/backend | BRAK | Potrzebny backend, licencje, SLA, cache, quota i provider strategy |
| Build APK / device QA | NIEPOTWIERDZONE | W tej sesji nie wykonano pełnego Android Gradle build ani testu na Lenovo/Samsung |

## Ważne korekty względem starych notatek

- Starszy `MASTER_PROJECT_GUIDE.md` i `README_HAULWAY.md` mówiły, że projekt używa OSRM car routing i nie ma ORS HGV. To jest nieaktualne dla bieżącego źródła: obecny `calculateRoadRoute()` wysyła `driving-hgv` do HeiGIT ORS i przekazuje część wymiarów/mas pojazdu.
- Nie oznacza to gwarancji HGV-safe. Kompletność ograniczeń zależy od danych mapowych i dostawcy.
- `TrafficVisionPanel` ma przełączniki i zapis ustawień, ale kod jawnie pokazuje `LIVE INCIDENT FEED NOT CONNECTED`.
- Rasterowa mapa z kafelków nie daje jakości etykiet i kamery porównywalnej z nowoczesnym rendererem wektorowym.
- Dokumenty wizji opisują część funkcji, które w obecnym kodzie są nadal tylko sekcjami lub placeholderami.

## Kryteria „gotowe do publicznego wydania”

Nie oznaczać jako release-ready przed:
1. zielonym `assembleDebug` i `assembleRelease`;
2. testami na Lenovo Tab M8 i Samsung Galaxy Tab A11;
3. testami routingu dla każdego typu pojazdu i ograniczeń;
4. testami znaków na drogach równoległych, skrzyżowaniach, rondach i jezdniach rozdzielonych;
5. działającym źródłem live traffic albo usunięciem obietnic live traffic z UI;
6. prawdziwym CRUD My Places / History / Services / Parking;
7. testami offline i jasnym oznaczeniem, które funkcje wymagają sieci;
8. kontrolą licencji, atrybucji, prywatności i API keys.

## Iteration 4 update — Services & POI
- Implemented nearby OpenStreetMap/Overpass search for parking, fuel, wash, food, repairs, weighbridges and rest areas.
- Results are sorted by straight-line distance from the last known device location and can be handed to an external navigation app.
- Still missing: along-route corridor search, verified truck-only parking classification, capacity/availability, opening hours validation, in-app HAULWAY route handoff, provider fallback/caching and offline POI data.
