## Why

The home-screen widget toggles the persistent visualizer service, but that session only produces visible output on Nothing phones (glyphs) or non-Nothing phones with Flash explicitly enabled. On a default non-Nothing phone, tapping the widget starts the mic + foreground notification with **no visible result** — a confusing, battery-wasting dead end. Rather than patch that device-split behaviour, repurpose the widget for the **Beacon "find me"** use case, which is universal across every phone, instant, and genuinely widget-shaped (the OS flashlight tile, but personalised).

## What Changes

- **BREAKING (widget behaviour):** The widget no longer toggles the persistent capture service. Tapping it now **launches the app straight into the Beacon overlay**, using the user's last-used / currently persisted Beacon settings (hue, text, text colour, react-to-sound).
- The widget becomes a **stateless launcher**: it no longer reflects a running/stopped state, because dismissal happens on the screen (back / tap), not via the widget.
- The widget works **identically on every phone** — Beacon is a front-screen overlay, so there's no Nothing/non-Nothing split and no microphone-permission crash risk for the static beacon.
- The persistent visualizer Start/Stop is unaffected — it still lives on the **Lights tab** button and the foreground-service notification.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `widget-toggle`: The widget's single tap changes from "start/stop the visualizer foreground service" to "launch the Beacon overlay with the current settings." The running-state reflection requirements are removed (the widget is now stateless).

## Impact

- **Code:**
  - `widget/GlyphSenseWidget.kt` — replace the `acquire/release(PERSISTENT)` toggle with a `PendingIntent` that launches `MainActivity` carrying a "launch beacon" extra; drop the running/stopped visual state and the `STATE_CHANGED` refresh.
  - `MainActivity.kt` — read the launch-beacon intent extra in `onCreate` and `onNewIntent` (with `setIntent`), and drive the existing `onLaunchBeacon` / `showBeacon` path.
  - `GlyphSenseService.kt` — `notifyStateChanged` calls into the widget become unnecessary for the widget's sake (the widget no longer renders state); the service's own state handling is otherwise unchanged.
  - Widget layout/label text (`res/layout/widget_layout.xml`, drawables) — update copy to read as a Beacon launcher; the running/stopped backgrounds are no longer toggled.
- **No new permissions, settings, or persistence** — `BeaconOverlay` already reads `VisualizerSettings`, so "last-used settings" is automatic.
- **Out of scope:** Show/Flash/glyph behaviour; the separate "non-Nothing inert by default" Flash-default question; any change to the Lights-tab Start/Stop or the notification.
