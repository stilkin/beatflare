## Why

The app has one input (the microphone) feeding several outputs, but it tracks that input as a single global "is the service running" flag that one tab turns on and another forgets to turn off. The Beacon "sensor stays on after dismiss" bug is the visible symptom: an inherently foreground, look-at-the-screen feature was forced through a lifecycle designed for the hands-off, screen-off Glyphs use case. We just patched Beacon with a one-off ownership flag; this change makes that a deliberate, uniform model before we add a third output (camera Flash) and inherit the same confusion.

## What Changes

- Introduce an explicit **two-class lifecycle model** for microphone capture:
  - **Persistent / hands-off outputs** (Glyphs today; Flash later) keep an explicit Start/Stop that survives screen lock and is widget-launchable. This is what the current "Play" Start/Stop already is.
  - **Ephemeral / overlay outputs** (Show, Beacon) treat the on-screen overlay as the session: launching starts capture only if nothing else needs it, and dismissing releases **only what that overlay started** — a pre-existing persistent session is left running.
- Replace the global "is running" coupling with a consumer-driven rule: **the mic runs iff at least one consumer needs it**, and each consumer releases only what it started. The Beacon ownership flag becomes the canonical implementation of this rule, applied uniformly (Show already obeys it implicitly).
- Absorb the generic "Play" tab into a renamed **Lights** tab that owns the rear outputs (Glyphs now, Flash later, device-gated) plus the persistent Start/Stop and live monitor, so controls align to lifetime instead of to a global switch. Beacon and Show remain separate job-named overlay tabs. **BREAKING (UX):** the "Play" tab is removed; nav becomes `[Beacon] [Show] [Lights]`.
- No change to audio mechanics (FFT, normalization), permissions, or the Glyph driver.

## Capabilities

### New Capabilities
- `capture-lifecycle`: Defines the microphone/service ownership model — the distinction between persistent and ephemeral capture consumers, the "mic runs iff a consumer needs it" rule, and the "release only what you started" guarantee that prevents orphaned capture.

### Modified Capabilities
- `front-screen-viz`: Overlay activation/deactivation semantics change — Show and Beacon overlays become self-contained capture sessions (dismiss releases the capture they started), and the meaning of "Start" shifts from a global run flag to the persistent output's explicit Start/Stop.

## Impact

- **Code**: `MainActivity.kt` (launch/dismiss lambdas, persistent Start/Stop placement, tab structure), `service/GlyphSenseService.kt` (start/stop entry points stay; lifecycle ownership moves to callers — no API break expected), overlay composables (`BeaconOverlay`, `PartyOverlay`) for dismiss-side release.
- **Specs**: new `capture-lifecycle`; delta on `front-screen-viz`. `audio-capture` is unaffected (the service↔capture 1:1 mapping holds; only *what triggers* start/stop changes). `widget-toggle` continues to toggle the persistent session.
- **In-flight overlap**: the `beacon-tab-rework` change (current PR, not yet archived) introduced Beacon and the temporary ownership flag; this change generalizes that flag. Sequencing/rebasing against it must be considered.
- **Out of scope**: the camera Flash output itself (separate change `camera-flash-output`), audio pipeline/FFT changes, new permissions.
