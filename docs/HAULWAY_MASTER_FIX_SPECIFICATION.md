# HAULWAY — MASTER MAP & NAVIGATION FIX SPECIFICATION

**Project:** HAULWAY Navigation  
**Package:** `com.czerman.haulway_navigation`  
**Priority:** Critical  
**Status:** Requirements recorded; implementation and device verification pending.

## 0. Purpose and delivery rule

This is the single source of truth for the map and navigation defects reported by the product owner. It is not complete when code merely compiles or an APK installs. Every requirement below must be implemented in the actual HAULWAY project and pass its acceptance tests.

Preserve the existing HAULWAY interface, screens, colours, map/information panel proportions, menus, bottom navigation and working features. Do not replace the app with a demo map or silently redesign existing screens. Inspect the exact current repository/branch before editing; do not patch an assumed or obsolete ZIP.

Do not mark anything DONE without test evidence. Do not claim live traffic, offline maps or guaranteed truck-legal routing unless actually implemented and verified.

## 1. Reported defects

1. Night map is too dark; street names and road detail are unreadable.
2. Day map is too pale/low contrast; roads and labels are hard to see.
3. Current street-name bubble/label does not reliably appear.
4. Map interaction is not fluid: pinch zoom and pan should be smooth.
5. User wants continuous zoom-out to a globe showing continents, countries and labels, then smooth zoom back to street level.
6. Map sometimes disappears completely while navigation is active and the vehicle is moving.
7. GPS position marker/icon sometimes disappears.
8. Initial navigation zoom is too close; user must press minus twice to get a useful view.
9. Manual zoom controls do not work reliably while driving, especially zooming out on motorways.
10. Automatic zoom-out on long straight roads/motorways does not work.
11. Automatic zoom-in near roundabouts, bends and junctions does not work.
12. Manoeuvre guidance is vague or insufficiently specific.
13. Roundabout instructions must state the correct exit number and change at the correct time.
14. Map/camera must remain stable during GPS updates, tile/style loading, rerouting, zooming and Day/Night changes.

## 2. Map rendering, visibility and styles

- Use a proper vector map renderer supporting continuous camera movement and globe projection. Assess migration from the current custom raster renderer to MapLibre GL JS in the existing WebView if compatible and reliable.
- Do not create Night mode by globally inverting a light map with CSS filters.
- Day style: clear road hierarchy, readable street labels, visible route and junction details.
- Night style: purpose-designed dark style, readable labels with suitable contrast/halo, clear roads and route.
- Keep labels readable at normal navigation zoom.
- Switching Day/Night must not recreate the whole map or lose GPS, route, camera or active navigation.
- Preserve required map attribution/provider compliance.

**Acceptance:** At the same location/zoom, both styles show readable road names, roads and route. Repeated style switching does not blank the map, reset route or remove GPS marker.

## 3. Smooth map, zoom, pan and globe

- Support smooth pinch-to-zoom and drag/pan without WebView reload or navigation restart.
- Zoom continuously from street level to a globe with continents, country borders/names and labels; return smoothly to regional/city/street views.
- Use actual map camera/projection features, not CSS transforms pretending to be 3D/globe.
- Map fills the entire allocated map viewport in 2D and 3D: no black bands, clipped area or empty strips.
- Camera animations are interruptible by user gestures and must not fight manual input.
- Keep route and GPS overlays through zoom transitions.

**Acceptance:** Repeated pinch/drag produces no flicker, blank map, app restart or lost overlays. Globe and labels appear at small scales; local labels appear at street scale. Follow resumes smoothly after panning.

## 4. Map stability — critical

- Never clear or hide the map during valid GPS updates, tile loading, route calculation, rerouting, camera motion or temporary geocoding failure.
- Keep one persistent map instance; update data sources/layers rather than destroying/recreating the map for every position or route update.
- Keep the last valid rendered map visible while new tiles/style data load.
- Handle tile/network/style errors without clearing the last usable view.
- Prevent stale asynchronous requests from overwriting newer map content.
- Log map lifecycle, style-load, tile-load and rendering errors.

**Acceptance:** Simulated movement with frequent GPS updates and camera changes never blanks the map. Rerouting does not blank it. Network loss retains last available content where cache allows and reports missing new data honestly. Repeated start/stop and Day/Night changes do not lose the map.

## 5. GPS marker and current street

- GPS marker is a persistent overlay above map tiles and route layers.
- Tile refresh, style switch, zoom, pan, rerouting and geocoding failures must not remove it.
- Update only from valid location fixes; temporary poor/missing fixes must not null the last valid position.
- Marker remains visible at all navigation zoom levels and in both styles.
- Follow keeps vehicle marker in a stable lower-centre position within the existing layout.
- Show current street name in a visible label/bubble near the marker when reliable data is available.
- Retain last known street name through temporary reverse-geocoding failure; do not flash/hide it on every failed request.
- Update when the vehicle enters a different street. Do not show only a postcode if street name is available.
- Reverse geocoding is throttled and never blocks rendering or GPS marker updates.

**Acceptance:** Simulated movement updates marker. Tile reload/style change/reroute/camera animation leave it visible. Geocoder timeout retains marker and last street. Street boundary crossing updates label. Verify both themes.

## 6. Automatic camera zoom by road and manoeuvre

Automatic zoom must work without pressing +/−. Base it on distance to next manoeuvre, manoeuvre type, road class and speed where available. Use hysteresis to prevent oscillation and smooth animation to prevent abrupt jumps.

| Situation | Expected behaviour |
|---|---|
| Motorway/long straight with no nearby manoeuvre | Gradually zoom out to show a longer section ahead |
| Approaching exit/junction | Gradually zoom in |
| About 500 m from roundabout | Show roundabout and correct exit clearly |
| Immediately before/on roundabout | Closer view of roundabout geometry and exit |
| Sharp bend/complex junction | Zoom in enough to understand road shape |
| After manoeuvre | Smoothly return to wider view for next section |

Technical requirements:
- Document zoom bands/thresholds and hysteresis.
- Use distance-to-next-manoeuvre, type, road class and speed where available.
- Do not zoom in due to noisy GPS speed or zoom out while a manoeuvre is imminent.
- Manual zoom/pan temporarily takes priority; define predictable resume-Follow behaviour.
- Start navigation at a useful scale; no two minus presses should be necessary.
- Camera updates must not reload map.

**Acceptance:** Simulate motorway with several kilometres to next turn (zoom out), approach exit/roundabout/bend/junction (zoom in), complete manoeuvre (zoom out smoothly). GPS noise must not cause jitter. Manual controls and Follow resume must work.

## 7. Manoeuvre guidance and roundabout exit number

- Route steps must carry structured manoeuvre data, not only vague text.
- Use routing step data where available: manoeuvre type, distance, road name, bearing/modifier, roundabout exit number and step geometry/location.
- Roundabout exit number must come from reliable route-step data or validated calculation from route geometry and roundabout topology. Never guess.
- Provide clear Polish voice and on-screen guidance with distance and action.
- Announce in stages and prevent excessive duplicate announcements.

Required roundabout example:
1. Advance announcement: **„Za 500 metrów na rondzie trzeci zjazd.”**
2. Immediate instruction: **„Na rondzie trzeci zjazd.”**
3. After the exit: announce the next route instruction.

The 500 m example must be supported; thresholds may adapt to speed and context and must be documented.

Also support specific guidance for left/right/slight turns, keep-left/right and lane guidance where data supports it, motorway entry/exit, forks/slip roads, U-turns, bends/complex junctions and destination arrival. Avoid generic “continue” when more precise data exists.

**Acceptance:** A test route with exit 3 announces the two required staged phrases and then the next instruction. Test exits 1–4. If exit number is missing/ambiguous, do not invent it; use safe generic wording and log missing data. Distance countdown, voice and screen remain synchronized; completed steps are not repeated continuously.

## 8. North-up, Follow and 3D

- North-up means north remains at the top; do not rotate the whole map to vehicle heading.
- Vehicle arrow may rotate to travel direction when heading is reliable.
- Follow keeps vehicle at the intended stable position and recentres smoothly.
- 3D uses actual map camera pitch, not CSS perspective that clips the map.
- 2D/3D changes preserve GPS, route, zoom intent and visibility.
- 3D fills the allocated map panel without black bands or missing tiles.

## 9. Preserve existing HAULWAY interface and features

- Keep HAULWAY branding, current screen structure, map/information panel proportions, bottom navigation and existing controls unless change is necessary to fix a defect.
- Do not replace app with generic sample screen.
- Do not remove Route Planner, Vehicle Profile, POI/Services, Traffic, saved places/routes or other existing features as a shortcut.
- Keep controls usable on target Android tablets in landscape.
- Avoid unnecessary UI changes that force the user to relearn the layout.

## 10. Engineering and diagnostics

- Inspect exact current branch/source before editing.
- Identify current renderer, route-step model, GPS update path, camera state and voice guidance pipeline.
- Separate map rendering, GPS state, route geometry, camera policy and voice instruction state so one failure cannot erase another.
- Handle lifecycle, WebView reload, network errors and stale asynchronous responses.
- Add diagnostic logs for GPS fixes, camera mode/zoom, next manoeuvre/distance, instruction stage, route-step data and map/style/tile errors.
- Never commit API keys or expose them in logs/screenshots; use secrets/configuration correctly.

## 11. Required test matrix

| Test | Expected result |
|---|---|
| Day street map | Roads and names clearly readable |
| Night street map | Dark style, readable labels and route |
| Pinch zoom/pan | Smooth; no restart/flicker/blank map |
| Globe zoom-out/return | Globe, countries and labels; smooth return |
| GPS movement | Marker remains visible and follows valid fixes |
| Reverse-geocode failure | Marker remains; last street retained |
| Active route/tile updates | Map never disappears |
| Motorway, distant manoeuvre | Automatic zoom-out |
| Roundabout approach | Automatic zoom-in |
| Roundabout exit 3 | Correct staged third-exit instructions |
| Bend/junction approach | Useful zoom-in and specific instruction |
| Manual zoom / resume Follow | Predictable user control and Follow |
| Day/Night switch in navigation | No map, route or GPS loss |
| Rerouting | Map stays visible; new route appears |
| App background/foreground | Map/navigation state recovers |

## 12. Definition of done

A requirement is DONE only when:
1. Implementation exists in the actual HAULWAY project.
2. Project builds successfully.
3. Relevant test is executed on a target device or documented simulator.
4. Evidence is recorded (test result, log, screenshot or video).
5. No previously working HAULWAY feature is broken.

If a test fails, status remains NOT DONE. Do not deliver another “finished” ZIP/APK with known map disappearance, missing GPS marker, broken zoom, unreadable labels or incorrect manoeuvre guidance.

## 13. Current status

This document records requirements and acceptance criteria. It does not claim defects have already been fixed. Implementation, build verification and device testing must be tracked separately.
