# HAULWAY MASTER — PLAN DOPROWADZENIA DO PRODUKTU 1:1

Kolejność jest celowa: najpierw bezpieczeństwo i stabilność nawigacji, potem dane pomocnicze i funkcje kierowcy.

## P0 — Stabilna baza jazdy
- Naprawić i przetestować zoom +/−, gesty, pan, FOLLOW i powrót do FOLLOW.
- Przejście globus → ostatnia pozycja → świeży GPS bez blokowania mapy.
- Zastąpić rasterowy renderer wektorowym, zachowując layout i styl HAULWAY.
- Płynny marker i przesuw mapy bez szarpnięć; adaptacyjny interwał GPS.
- Poprawić nazwy ulic: ostre etykiety wektorowe; dymek pokazuje tylko nazwę ulicy.
- Stabilny panel manewrów i głos: jeden krótki prompt na manewr.
- Testy regresji na realnym tablecie.

## P1 — Prawdziwe HGV routing i znaki
- Audyt requestu ORS: wysokość, szerokość, długość, masa, nacisk osi, typ pojazdu, ADR, przyczepa, ograniczenia.
- Weryfikacja wartości domyślnych dla wszystkich pięciu typów i pól niestandardowych.
- Dodać jawny status „HGV route calculated / restrictions data incomplete”.
- Znaki dopasowywać do konkretnych way IDs oraz właściwej jezdni/kierunku; nie używać samej odległości od polilinii.
- Pobierać dane dla korytarza całej aktywnej trasy, nie tylko aktualnego viewportu.
- Aktualizować znaki przy reroutingu; cache z TTL; testy rond, węzłów, dróg równoległych.
- Jeśli OSM nie ma pewnego powiązania znaku z drogą, pominąć znak zamiast zgadywać.

## P2 — Parking i usługi
- Prawdziwy search provider i filtry HGV.
- Karty szczegółów, dystans, godziny, udogodnienia, telefon/strona jeśli dostępne.
- „Near me / Along route / At destination”.
- Nawiguj do wybranego POI; zapisuj ulubione.
- Nie udawać dostępności wolnych miejsc, jeśli feed nie istnieje.

## P3 — Traffic i pogoda
- Wybrać legalny provider live traffic, incidents, roadworks i closures dla rynków docelowych.
- Wspólny model zdarzenia: typ, lokalizacja/odcinek, kierunek, timestamp, źródło, ważność.
- Filtry z Vision 7/10 mają sterować rzeczywistymi markerami.
- Pogoda wzdłuż trasy z providerem i timestampem.
- Jeśli brak feedu, czytelny status „unavailable”, a nie puste przełączniki udające aktywne dane.

## P4 — Kierowca i dane lokalne
- [x] My Places: podstawowe dodawanie i usuwanie nazwanych adresów, zapis lokalny i przekazanie do Route Planner.
- [ ] My Places: edycja, geokodowanie i współrzędne, import/eksport.
- [x] Historia obliczonych planów tras (lokalnie, usuwanie wpisów).
- [ ] Rejestr ukończonych tras i statystyki rzeczywistej jazdy.
- Parking/fuel/services history.
- Pre-trip checklist, zgłaszanie usterek, przypomnienia serwisowe — jako rozszerzenie, jeśli potwierdzi się zakres.
- Room DB, migracje, kopia zapasowa i eksport.

## P5 — Street imagery / offline / global
- Street View: przejście z wpisywania koordynatów do podglądu wybranego adresu i przystanku.
- Pokazywać rodzaj obrazu i datę; nie obiecywać panoramy, jeśli to zwykłe zdjęcie.
- Offline map packages z legalnym providerem, pobieraniem, aktualizacją i kontrolą pamięci.
- Offline routing dopiero po integracji offline road graph.
- Global provider strategy: adresy, map tiles, HGV restrictions, traffic, imagery i języki.
- Backend proxy dla kluczy, quota, cache i telemetrii z ochroną prywatności.

## P6 — Release
- Automatyczny CI build i testy.
- Testy jednostkowe: profile, route geometry, break duration, maneuver de-dup, map matching.
- Testy UI: wszystkie ekrany i stany błędów.
- Testy terenowe na różnych tabletach/telefonach i w różnych krajach.
- Licencje/atrybucje, polityka prywatności, bezpieczeństwo API.
- Dopiero potem oznaczyć wersję jako produkcyjną.

## Następny etap po 2.0.3
1. Dodać wyszukiwanie usług w korytarzu aktywnej trasy, a nie tylko wokół GPS.
2. Rozdzielić zwykłe parkingi od parkingów potwierdzonych jako HGV; nie zgadywać dostępności miejsc.
3. Przekazywać wybrany POI bezpośrednio do wewnętrznego planera HAULWAY z zachowaniem punktów trasy.
4. Dodać cache i drugi endpoint Overpass, limity zapytań oraz czytelne stany offline.
5. Dodać filtrowanie po cechach: wysokość wjazdu, prysznice, toalety, paliwo, AdBlue, warsztat — wyłącznie gdy dane źródłowe istnieją.


### Vision 16 — POI wzdłuż trasy (2.0.4)
- Dodano zakres wyszukiwania „Near Me” oraz „Along Route”.
- Wzdłuż trasy wykonywane jest wyszukiwanie w promieniu 2,5 km wokół maksymalnie sześciu punktów próbkowania geometrii trasy.
- Wyniki są deduplikowane po ID elementu OSM. To przybliżony korytarz, nie pełne ciągłe pokrycie trasy.
