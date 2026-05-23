## 1. Consumer model in the service

- [x] 1.1 Add a `Consumer` enum (`PERSISTENT`, `SHOW`, `BEACON`) to `GlyphSenseService`
- [x] 1.2 Add a guarded `consumers: MutableSet<Consumer>` and derive `isRunning` from `consumers.isNotEmpty()` (keep the existing `isRunning` StateFlow contract for UI/widget)
- [x] 1.3 Implement `acquire(context, Consumer)`: add to set; if capture was stopped, start the foreground service + capture (idempotent — second acquire while running is a no-op)
- [x] 1.4 Implement `release(context, Consumer)`: remove from set; if the set is now empty, stop capture (`stopSelf`) and release the mic
- [x] 1.5 Notify the widget on every transition (reuse existing `notifyStateChanged`)

## 2. Wire the persistent consumer

- [x] 2.1 Map the existing Start/Stop action to `acquire(PERSISTENT)` / `release(PERSISTENT)`
- [x] 2.2 Map the home-screen widget toggle to `acquire/release(PERSISTENT)`
- [x] 2.3 Verify the persistent session keeps capturing with the screen off and is unaffected by overlays

## 3. Wire the overlay consumers

- [x] 3.1 Show launch → `acquire(SHOW)`; Show dismiss (tap / back) → `release(SHOW)`
- [x] 3.2 Beacon launch → `acquire(BEACON)` only when React-to-sound is on; Beacon dismiss → `release(BEACON)`
- [x] 3.3 Beacon with React-to-sound off → acquire nothing, render from settings only (no service, no notification)
- [x] 3.4 Remove the temporary `beaconStartedService` UI-layer ownership flag introduced by the Beacon dismiss patch — its behavior is now covered by the consumer set

## 4. Robustness against activity recreation

- [x] 4.1 Hoist the overlay shown-flags to `rememberSaveable` so a config change does not orphan an overlay consumer
- [x] 4.2 On `Activity.onDestroy` when not `isChangingConfigurations`, release any overlay consumers the activity owns (safety net against orphaned capture)
- [x] 4.3 Confirm rapid launch/dismiss taps cannot leave capture running with an empty consumer set (or stopped with a non-empty set)

## 5. UI alignment — Lights tab

- [x] 5.1 Rename the Glyphs tab to "Lights" and absorb the Play tab's persistent Start/Stop + live spectrum monitor into it
- [x] 5.2 Device-gate Lights content: glyph zones/brightness on Nothing phones (the flash section ships with `camera-flash-output`)
- [x] 5.3 Show the Lights tab only when the device has at least one rear output; bottom-nav order becomes `[Beacon] [Show] [Lights]`
- [x] 5.4 Keep Beacon and Show as separate job-named tabs, each with its own launch action; preserve relaunch-from-running-session for Show
- [x] 5.5 Update tab headers/labels accordingly (e.g. "Glyph Settings" → "Lights")

## 6. Verify & validate

- [x] 6.1 `./gradlew ktlintCheck :app:assembleDebug` passes
- [x] 6.2 On device — Beacon (react on) alone: light up → notification appears, mic active; dismiss → notification gone, mic released
- [x] 6.3 On device — Beacon over a running persistent session: dismiss leaves the session running
- [x] 6.4 On device — static Beacon (react off): no notification, no mic, dismiss is a no-op
- [x] 6.5 On device — Show launch/dismiss acquires/releases capture as expected
- [x] 6.6 On device — persistent session survives screen off; Stop / widget ends it
- [x] 6.7 `openspec validate capture-lifecycle-rework` passes
