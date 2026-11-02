HAULWAY STARTUP SCREEN

- Full-screen landscape/portrait artwork is loaded from drawable-nodpi/haulway_splash_background.png.
- The displayed percentage is not a timer animation. It advances only after startup tasks complete:
  25%: app settings and package metadata read successfully.
  50%: splash and HOME image resources are verified as decodable.
  75%: location permission state is checked and stored (this does not claim a GPS fix).
  100%: navigation artwork resource references are checked and the UI is ready to enter.
- If a task throws, the startup coroutine fails rather than falsely showing 100%.
- The splash then transitions to the existing HAULWAY HOME screen.
- Location permissions are declared but are not requested during startup; runtime permission UX should be handled by the navigation/GPS flow.

The progress is real for these startup checks. It does not represent downloaded map tiles or a GPS fix; those services are not yet integrated in this project stage.
