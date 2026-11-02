# HAULWAY splash startup polish

- Replaced the transparent Android 12+ splash glyph with the real HAULWAY launcher artwork so the mandatory system splash is branded instead of appearing as an empty black screen.
- Kept the full-screen cinematic splash as the first Compose screen.
- Startup progress now advances after actual work: settings/profile read, full image decoding, WebView renderer warm-up, and permission checks.
- WebView warm-up is bounded by a 7-second timeout and is best-effort; a failure does not block app entry.
- No artificial long delay was added. On fast devices the splash can still be brief because the work genuinely finishes quickly.

Android 12+ still controls the brief system splash and only permits an icon-style graphic there; the full artwork appears immediately after Activity content is drawn.
