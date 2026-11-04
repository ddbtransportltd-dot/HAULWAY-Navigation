# HAULWAY VISION17 — Navigation layout and road alerts

Version: 2.1.7  
Version code: 21  
Branch: vision-17-navigation-layout-alerts

## Changes
- Moved the current road label into a compact bubble below the centred GPS marker; background alpha is approximately 65%.
- Reworked the right navigation rail: five upcoming manoeuvres use compact 40dp cards.
- Truck Parking and Services are now two compact side-by-side action tiles.
- Kept the distance-left and ETA/arrival panel below the manoeuvres and quick actions.
- Removed the on-map globe toggle. The map starts in globe projection and automatically transitions to the normal road map when the first valid GPS fix arrives.
- Kept the voice button in the top-right corner without a globe button beside it.
- Added nearby mapped road-alert chips for traffic lights, STOP/Give Way signs, mapped speed cameras, roadworks and traffic calming, refreshed every 45 seconds.
- Live vehicle speed continues to come from GPS; mapped speed limits remain blank when no reliable maxspeed tag is found.

## Data and safety notes
- Road alerts are nearby OpenStreetMap features within roughly 650 metres of the GPS fix. They are not guaranteed to lie on the calculated route, and missing OSM tagging means an alert may not appear.
- Speed limits and alerts are map-data dependent, not a substitute for roadside signs or the driver's judgement.
- No speed limit or warning is invented when source data is absent.

## Validation
- GitHub Actions build and on-device testing are still required.
- Verify the GPS bubble placement, globe-to-GPS transition, right-rail fit, volume button and alert coverage on the Samsung Galaxy Tab A11.
