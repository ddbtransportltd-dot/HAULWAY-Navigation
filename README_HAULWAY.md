# HAULWAY Navigation — MASTER 2.0 Preview

To jest jedna skonsolidowana paczka projektu Android Studio, która łączy kod z najnowszej gałęzi Vision 15 oraz pełną specyfikację produktu z wizji 1–15.

## Start
1. Rozpakuj ZIP.
2. W Android Studio otwórz folder `HAULWAY-Navigation-main` (ten, w którym znajduje się `settings.gradle.kts`).
3. Dodaj własne klucze do lokalnego `local.properties` — nigdy nie commituj kluczy.
4. Wykonaj Gradle Sync.
5. Uruchom `Build > Make Project`, a następnie `Build APK`.

## Dokumenty master
- `HAULWAY_MASTER_PRODUCT_SPEC_PL.md` — pełna docelowa specyfikacja HAULWAY.
- `HAULWAY_MASTER_AUDIT_PL.md` — co rzeczywiście jest w kodzie, co jest częściowe, a czego brakuje.
- `HAULWAY_MASTER_ROADMAP_PL.md` — kolejność prac do wersji produkcyjnej.
- `MASTER_PROJECT_GUIDE.md` — techniczny opis projektu.
- `VISION*_CHANGELOG.md` — historia poprzednich wizji.

## Ważne
To jest **MASTER PREVIEW**, a nie certyfikowana wersja produkcyjna. Bieżący kod ma integrację routingu ORS `driving-hgv`, ale jakość ograniczeń zależy od danych i nie gwarantuje legalnego przejazdu. Live traffic feed nie jest podłączony. Offline maps nie są gotowe. Pełny Android build i testy urządzeń nie zostały potwierdzone w tym środowisku.

Wersja Android: `2.0.0-master-preview` / versionCode 6.
