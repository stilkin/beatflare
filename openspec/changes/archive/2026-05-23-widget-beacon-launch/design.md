## Context

The widget (`widget/GlyphSenseWidget.kt`) is an `AppWidgetProvider`. Today its tap broadcasts `ACTION_TOGGLE` to itself, which calls `GlyphSenseService.acquire/release(PERSISTENT)`, and the service broadcasts `ACTION_STATE_CHANGED` back so the widget can redraw a running/stopped background. That toggle is meaningful only where a persistent rear output exists (Nothing glyphs, or Flash-enabled non-Nothing) — elsewhere it silently starts a no-output session.

Repurposing the widget to launch the Beacon turns it into a plain **activity launcher**. The Beacon is a front-screen overlay already wired in `MainActivity` via the `onLaunchBeacon` lambda + `showBeacon` (`rememberSaveable`) state, and `BeaconOverlay` reads its appearance straight from the persisted `VisualizerSettings`. So "launch the Beacon with last-used settings" reduces to: get `MainActivity` to the foreground in its Beacon state.

## Goals / Non-Goals

**Goals:**
- One widget tap → app opens directly into the Beacon overlay, using current persisted Beacon settings.
- Identical behaviour on every device; no microphone-permission crash risk for the static beacon.
- Stateless widget — no running/stopped rendering, no service round-trip.
- Reuse the existing `onLaunchBeacon` path; no new persistence.

**Non-Goals:**
- Changing the persistent visualizer Start/Stop (stays on the Lights tab + notification).
- Any Show / Flash / glyph behaviour change.
- The "non-Nothing inert by default" Flash-default question.

## Decisions

### Decision 1: Launch the activity via a `PendingIntent.getActivity`, not a service start
The widget's click `PendingIntent` targets `MainActivity` directly with an extra `EXTRA_LAUNCH_BEACON = true`, using `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_SINGLE_TOP`. No more self-broadcast `ACTION_TOGGLE` round-trip.

*Alternative rejected:* keep broadcasting to the provider and have it `startActivity` — pointless indirection now that there's no service state to manage.

### Decision 2: `singleTop` + `onNewIntent` so a tap never spawns a duplicate activity
Add `android:launchMode="singleTop"` to `MainActivity`. Handle the extra in **both** `onCreate` (cold start / fresh task) and `onNewIntent` (app already foreground/background) — calling `setIntent(intent)` in `onNewIntent` so the launched state is read consistently.

### Decision 3: Consume the flag once, drive the existing `showBeacon` path
On reading `EXTRA_LAUNCH_BEACON`, route through the same `onLaunchBeacon` logic the in-app Beacon tab button uses (which already gates mic acquisition on `beaconReactToSound && micGranted`, with a static fallback). The flag is **consumed** after handling (remove the extra / one-shot guard) so a later rotation or return-from-background does not re-trigger the Beacon. `showBeacon` being `rememberSaveable` covers config changes.

### Decision 4: Strip the widget's state machinery
Remove `ACTION_TOGGLE`/`ACTION_STATE_CHANGED` handling, the running/stopped background swap, and the `isRunning`-based status text. The widget renders one static look ("Beacon — tap to show"). `GlyphSenseService.notifyStateChanged` no longer needs to reach the widget (the service may keep calling it harmlessly, or the call sites can be dropped if they exist solely for the widget — verify before removing).

## Risks / Trade-offs

- **Cold start from a dead process** → the extra must be honoured on first composition. Mitigation: read `intent` in `onCreate` and seed the initial `showBeacon` state before/at `setContent`.
- **Stale intent re-firing the Beacon** (e.g., user dismisses Beacon, rotates, or returns from background and the old intent is replayed) → Mitigation: consume the flag after first handling (Decision 3) and rely on `rememberSaveable` for `showBeacon`.
- **Reactive Beacon needs the mic** → identical to the in-app launch; the existing permission check renders a static beacon when mic isn't granted. No new risk; static beacon needs no permission.
- **Existing placed widgets after app update** → the system calls `onUpdate` on upgrade, re-binding the new launcher `PendingIntent`; no manual migration needed.

## Migration Plan

Behaviour-only change; no data migration. On update, existing widget instances re-bind to the Beacon-launcher intent on the next `onUpdate`. Rollback = revert the change; the widget returns to the service toggle. The persistent visualizer remains fully controllable from the Lights tab and the notification throughout.
