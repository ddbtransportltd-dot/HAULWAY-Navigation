# HAULWAY Vision 16 — Driving Guidance

- Changed follow-camera orientation to align with live vehicle heading; the vehicle arrow remains upright on screen.
- Reduced the default follow zoom by two levels and retained manual plus/minus zoom control.
- Added automatic zoom-in as the next manoeuvre approaches.
- Added a five-item upcoming manoeuvre list.
- Added estimated arrival clock time alongside remaining distance and ETA/duration.
- Matched the voice-guidance button shape, border and icon scale to the globe control.
- Added periodic OpenStreetMap road-name and tagged speed-limit lookup; unknown limits remain blank rather than being guessed.
- Interpolated the geographically anchored GPS marker between location fixes to reduce visible jumps.

## Verification
- GitHub Actions build and on-device navigation testing are still required.
- Speed-limit coverage depends on OpenStreetMap maxspeed tagging and mobile data availability.
- Road name and speed-limit lookups are throttled to avoid excessive Overpass requests.
