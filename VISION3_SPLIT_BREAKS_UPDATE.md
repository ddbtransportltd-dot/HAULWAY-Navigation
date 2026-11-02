# Vision 3 – Separate split breaks

- Every break is stored as its own `BREAK|minutes` stop in the route list.
- Add Break inserts a new break before the destination; it never replaces or accumulates another break.
- Tapping an existing Break row opens the duration selector and edits only that break.
- 15-minute and 30-minute breaks can coexist, e.g. Manchester → Break 15 min → Break 30 min → London.
- Total break time is calculated as the sum of the individual break stops and is added once to the driving duration.
- Break rows are excluded from geocoding and OSRM routing; only real address stops are sent to the road router.
- Route distance remains road distance and is not increased by breaks.
