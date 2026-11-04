# HAULWAY — MASTER PRODUCT SPECIFICATION
## Jeden produkt, wszystkie wizje (1–15), kierunek docelowy

**Nazwa produktu:** HAULWAY Navigation
**Android application ID:** `com.czerman.haulway_navigation`
**Wersja źródłowa:** `2.0.0-master-preview`
**Stan dokumentu:** konsolidacja wymagań i audyt źródła z 3 listopada 2026 r.

---

## 1. Cel produktu

HAULWAY ma być profesjonalną, globalną nawigacją dla kierowców ciężarówek, autobusów i pojazdów dostawczych. Nie ma być jedynie mapą z niebieską linią. Ma łączyć:

- nawigację HGV z profilem rzeczywistego pojazdu;
- czytelny ekran jazdy typu TomTom/Garmin;
- planowanie tras wieloprzystankowych i przerw;
- ostrzeganie o znakach i ograniczeniach na dokładnie tej drodze, którą prowadzi aktywna trasa;
- parkingi ciężarowe i usługi przy trasie;
- informacje drogowe i pogodowe;
- podgląd ulic / zdjęcia 360°;
- profile pojazdów, ulubione miejsca, historię i ustawienia;
- głosowe instrukcje bez powtórzeń i nakładania komunikatów.

Priorytetem jest bezpieczeństwo, czytelność w kabinie i stabilność na tabletach o ograniczonych zasobach. Interfejs nie może zasłaniać mapy ani zmuszać kierowcy do obsługi wielu drobnych elementów podczas jazdy.

## 2. Zasady produktu, których nie wolno naruszyć

1. Nazwa w interfejsie to zawsze **HAULWAY**. Nie przywracać starej nazwy „HGV Navigation”.
2. Zachować ciemny, przyjazny interfejs z niebieskimi akcentami, bielą, szarością, pomarańczowym i czerwonym dla ostrzeżeń.
3. Ekran nawigacji: mapa po lewej / główna część ekranu, panel manewrów i informacji po prawej; na tablecie docelowo około 65–70% mapy i 30–35% panelu.
4. W prawym górnym rogu tylko jeden pasek statusu urządzenia. Nie dublować ikon w panelu manewrów.
5. Jedna aktywna instrukcja manewru ma być wyraźna; kolejne kroki w kolejności, bez powielania trip summary.
6. Mapa ma obsługiwać ręczne przesuwanie i zoom. FOLLOW włącza się ponownie dopiero po poleceniu kierowcy, nie odbiera sterowania podczas gestu.
7. Tryb 3D ma śledzić pojazd i nie może obracać mapy w nieoczekiwany sposób. Kompas/kierunek pojazdu nie może powodować restartu ekranu.
8. Dymek pod markerem pokazuje wyłącznie nazwę ulicy. Nigdy nie zastępować jej kodem pocztowym ani nazwą miasta. Docelowe tło dymka: około 45% krycia.
9. Znaki drogowe na mapie nawigacyjnej pokazują się wyłącznie na drodze/jezdni przypisanej do aktywnej trasy. Samo „blisko linii” nie wystarcza.
10. Brak danych oznacza „brak danych”, a nie wartość wymyśloną. Nie generować fikcyjnej prędkości, limitu, korka, dostępności parkingu ani ETA.
11. Wszystkie funkcje HGV muszą uwzględniać aktywny profil pojazdu. Wynik publicznego routingu nigdy nie jest gwarancją legalnego przejazdu.
12. Żadnych sekretów API w repozytorium. Klucze lokalnie lub po stronie kontrolowanego backendu.

## 3. Wizje 1–15 — skonsolidowany zakres

### Vision 1 — HOME / Driver Centre
- Ekran startowy HAULWAY i marka.
- Kafle do nawigacji, planowania trasy, profilu pojazdu, parkingów, usług/POI, ruchu, zapisanych miejsc, historii i ustawień.
- Szybkie wejście do mapy.
- Splash screen z brandingiem HAULWAY.

### Vision 2 — MAP / Driving Navigation
- Mapa na żywo, pozycja GPS, dokładność, prędkość z pomiaru urządzenia.
- Marker pojazdu z kierunkiem; interpolacja pomiędzy odczytami GPS.
- FOLLOW / ponowne centrowanie.
- Zoom +/−, wyszukiwanie adresu, wybór warstwy, 2D/3D.
- Linia trasy, przebyta część trasy i pozostała część.
- ETA i dystans aktualizowane względem postępu.
- Panel manewrów po prawej, jeden aktywny manewr i kolejne kroki.
- Czytelny limit prędkości, jeśli dostępny z wiarygodnych danych; osobno prędkość GPS.
- Globus jako ekran startowy, a następnie płynne przybliżenie do pierwszej wiarygodnej lokalizacji.
- Szybkie pokazanie ostatniej znanej pozycji, później korekta GPS/siecią.
- Rasterowe kafelki nie są docelowym rozwiązaniem ostrości etykiet: docelowo mapa wektorowa z ostrymi nazwami ulic i właściwą kamerą 3D.

### Vision 3 — ROUTE PLANNER
- Start, cel, dowolna liczba przystanków pośrednich.
- Dodawanie, usuwanie i zmiana kolejności przystanków.
- Trasy Fastest / Shortest / Avoid tylko wtedy, gdy silnik rzeczywiście egzekwuje daną opcję.
- Podgląd mapy, ponumerowane punkty, dystans i czas.
- Start nawigacji przekazuje dokładnie obliczoną geometrię i instrukcje.
- Przerwy jako osobne wiersze (np. 15 i 30 minut mogą istnieć jednocześnie).
- Przerwy zwiększają czas podróży dokładnie raz, ale nie są geokodowane i nie zwiększają dystansu drogowego.
- Obsługa „Current location” jako startu.

### Vision 4 — TRUCK PROFILE
- Pięć typów: ARTIC, RIGID, VAN, WAG & DRAG, BUS.
- Wiele nazwanych profili, dodawanie, edycja, usuwanie i wybór aktywnego.
- Wymiary: wysokość, szerokość, długość; masa całkowita i nacisk osi.
- Typ przyczepy, ładunek, ADR/hazmat, ograniczenia pojazdu i prędkość maksymalna.
- Ilustracja 3D pojazdu i aktualne wymiary.
- Walidacja danych i trwałe zapisanie.
- Aktywny profil przekazywany do HGV routing request.

### Vision 5 — TRUCK PARKING
- Szukanie blisko mnie, wzdłuż trasy i przy celu.
- Filtry: HGV access/designation, WC, prysznic, jedzenie, paliwo, ochrona, płatność, dostępność.
- Szczegóły, dystans, nawiguj, zapisz.
- Dostępność miejsc tylko z aktualnego, wiarygodnego źródła; inaczej status „nieznany”.

### Vision 6 — SERVICES & POI
- Paliwo, ładowanie (jeśli dotyczy), truck wash, warsztaty, opony, weighbridge, restauracje, sklepy, toalety, odpoczynek i parking.
- Kategorie, filtry, lista + mapa, szczegóły i nawiguj.
- Wyszukiwanie w pobliżu, wzdłuż aktywnej trasy i przy celu.
- Oznaczenia usług nie mogą mieszać się ze znakami drogowymi.

### Vision 7 — TRAFFIC
- Warstwy: korki, wypadki, awarie, roboty, zamknięcia pasów/dróg, tymczasowa sygnalizacja, ograniczenia, niskie mosty, wysokość/szerokość/masa, LEZ, opłaty, kamery, pogoda, parking.
- Filtry zapisują się lokalnie.
- Każde zdarzenie ma źródło, czas aktualizacji, kategorię, kierunek/odcinek i poziom pewności.
- Bez podłączonego feedu warstwa musi jasno informować, że dane live są niedostępne; nie wolno udawać live traffic.

### Vision 8 — MY PLACES
- Home, Work i własne ulubione.
- Dodaj, edytuj, usuń, ustaw ikonę i nazwę.
- Podgląd mapy, „Navigate”, import/eksport.
- Zapisane miejsca przechowują współrzędne i czytelną nazwę.

### Vision 9 — HISTORY & STATISTICS
- Ostatnie trasy z datą, startem, celem, dystansem i czasem.
- Statystyki dystansu, czasu jazdy, przerw i zużycia paliwa tylko wtedy, gdy dane są wprowadzone/zmierzone.
- Otwórz ponownie trasę, usuń wpis, eksportuj podsumowanie.
- Prywatność i lokalne przechowywanie danych.

### Vision 10 — SETTINGS
- Map Appearance: Day / Night / Auto, styl mapy, etykiety, POI, ruch, budynki 3D.
- Navigation: FOLLOW, 2D/3D, auto-zoom, przeliczanie trasy.
- Warnings: znaki, prędkość, ograniczenia HGV, alerty.
- Display: jasność, układ, wielkość tekstu.
- Voice Guidance: głośność, język, szybkość, typ komunikatów, wyciszenie.
- Units & Regional: mph/km/h, mile/km, język i region.
- Offline Maps: pobieranie obszaru, aktualizacja, rozmiar i usuwanie — dopiero po integracji prawdziwego offline map engine.
- About HAULWAY, licencje, źródła danych, wersja.

### Vision 11 — stabilność i czytelność jazdy
- Widoczny marker, płynne GPS, aktualizowane ETA/dystans.
- FOLLOW i 3D.
- Limit prędkości czytelny i odróżniony od prędkości pojazdu.
- Usunięcie zduplikowanego panelu podróży.
- Mapa mieści się w panelu manewrów.
- Logo i zegar w nagłówku.
- Ikony odpowiadają faktycznym manewrom.
- Jedna krótka zapowiedź manewru w ustalonym wyprzedzeniu.

### Vision 12 — nawigacja w ruchu
- Interpolacja położenia i kierunku.
- Ręczne przesunięcie mapy wyłącza FOLLOW; przycisk centrowania przywraca.
- Dopasowanie GPS do geometrii trasy i oddzielenie części przebytej od pozostałej.
- Jedna aktywna karta manewru.
- Jedna krótka instrukcja głosowa na manewr; bez kaskady 800/500/300/NOW.
- ETA i dystans z postępu trasy, z oznaczeniem, jeśli nie są live-traffic aware.

### Vision 13 — znaki i POI
- Rozpoznawalne symbole STOP, GIVE WAY, światła, ograniczenia, parking, lay-by, kamera i POI.
- Dane OSM/Overpass z informacją o ograniczonej kompletności.
- POI = ALL w ekranie usług, ale nie w warstwie znaków nawigacyjnych.
- Adaptacyjny zoom.
- Dymek ulicy pod pojazdem.
- Znaki nie mogą zalewać ekranu.

### Vision 14 — rendering i głos
- Jedna belka statusu urządzenia.
- Deduplikacja głosu na podstawie tożsamości manewru, a nie indeksu listy.
- TTS bez nakładania i powtarzania komunikatów.
- Kafelki bez szczelin, stabilny render, ograniczone puste pola w 3D.
- Nazwa ulicy z reverse geocoding, tylko gdy jest znana; dymek około 45% krycia.

### Vision 15 — globus, szybki start i dokładne znaki
- Globus startowy i animowane przybliżenie do lokalizacji GPS.
- Szybszy start na ostatniej znanej pozycji, później poprawka świeżym GPS.
- Znaki tylko na drodze przypisanej do aktywnej trasy — nie na sąsiednich ulicach.
- Zoom musi działać niezależnie od animacji startowej.

## 4. Docelowy przepływ nawigacji

1. Kierowca wybiera aktywny profil pojazdu.
2. Wprowadza adres/postać kodu pocztowego lub wybiera zapisane miejsce.
3. Wybiera przystanki i przerwy.
4. HAULWAY geokoduje adresy, oblicza trasę HGV, pokazuje ograniczenia i ostrzeżenia oraz informuje o niepewności danych.
5. Kierowca sprawdza trasę i potwierdza rozpoczęcie.
6. Mapa przechodzi do FOLLOW, wyświetla wyłącznie znaki powiązane z drogą na trasie, a panel pokazuje następny manewr.
7. Głos odtwarza pojedynczy, krótki komunikat dla każdego manewru.
8. Po zboczeniu z trasy aplikacja wykrywa odchylenie, proponuje przeliczenie i po zaakceptowaniu aktualizuje drogę oraz znaki.
9. Po zakończeniu zapisuje podsumowanie podróży.

## 5. Docelowa architektura techniczna

- UI: Kotlin + Jetpack Compose, komponenty ekranów rozdzielone na moduły.
- Map engine: docelowo renderer wektorowy (np. MapLibre Native/SDK z odpowiednią licencją i stylem), a nie ręcznie składany raster WebView jako architektura końcowa.
- Location: jeden natywny źródłowy manager lokalizacji, fused provider jeśli zostanie dodany; ograniczanie częstotliwości i filtrowanie jittera.
- Routing: adapter `TruckRoutingProvider`; ORS `driving-hgv` jako obecny provider integracyjny; możliwość późniejszej zmiany na własny backend/Valhalla.
- Geocoding: provider adapter dla autocomplete i reverse geocoding; limity publicznych API respektowane.
- Road signs: OSM road geometry + relation/node/way association; dopasowanie do identyfikatora drogi; cache i aktualizacja odcinkami trasy.
- Traffic: osobny dostawca danych live, niezależny od dostawcy kafelków mapy.
- Voice: kolejka komunikatów z identyfikatorem manewru, priorytetem, progiem dystansu i cooldownem.
- Storage: Room dla profili, miejsc, historii, pobranych obszarów; migracje wersji bazy.
- Secrets: klucze testowe lokalnie; produkcyjny proxy/backend dla kluczy i quota.
- Observability: logi diagnostyczne bez prywatnych danych, raport błędów i pomiar czasu startu mapy.

## 6. Wymagania jakościowe i testy

- Tablet 8–11 cali, orientacja landscape, minimum 3 GB RAM.
- Mapa nie może blokować zoomu/gestów podczas GPS follow.
- Brak skoków markera przy odczytach GPS.
- Po pierwszej znanej pozycji marker i mapa muszą pojawić się bez zbędnego oczekiwania na pełny zestaw kafelków.
- Brak podwójnych ikon statusowych i podwójnych promptów.
- Dla testów znaków przygotować trasę z drogą równoległą/skrzyżowaniem: znak na trasie ma się pojawić, znak na drodze bocznej nie.
- Testy profili: wszystkie 5 typów, wartości niestandardowe, kasowanie i ponowne dodawanie.
- Testy przerw: 15 + 30 min, edycja jednej nie zmienia drugiej, suma tylko raz.
- Testy API: brak klucza, 401/403, 429, timeout, brak internetu, błędna geometria.
- Testy 2D/3D, ręczny pan, zoom +/−, FOLLOW, powrót do FOLLOW.
- Testy głosu: jedna zapowiedź na manewr mimo odświeżenia geometrii i reroutingu.
- Testy build: Gradle sync, `assembleDebug`, instalacja na Lenovo TB300FU i Samsung Tab A11.

## 7. Zasady publikacji i bezpieczeństwa

HAULWAY nie może być przedstawiany jako certyfikowany lub gwarantujący legalny przejazd system HGV. OSM i publiczne routing API mogą mieć niepełne ograniczenia. Kierowca musi przestrzegać znaków drogowych, ograniczeń pojazdu i instrukcji operatora. Przed wydaniem komercyjnym wymagane są: audyt routingu, warunków dostawców, ochrony kluczy, prywatności lokalizacji, cache/offline, testy urządzeń i testy terenowe.
