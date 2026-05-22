## Context

Microphone capture lives in `GlyphSenseService`, a foreground service. Today its lifetime is a single boolean: `isRunning`. The Play tab's Start/Stop flips it, the widget flips it, and overlays (Show, Beacon) were bolted on with ad-hoc start logic — Beacon even auto-starts the service but, until the recent patch, never stopped it. That patch added a UI-layer "did Beacon start the service?" flag. This change replaces the global boolean and the scattered ownership flags with one explicit model, before a third output (camera Flash) inherits the same mess.

The key realization: the app is **one input (mic) → many outputs**, and the outputs have two different natural lifetimes. Glyphs (and later Flash) are *hands-off* — phone face-down, screen possibly off, optionally widget-launched. Show and Beacon are *look-at-the-screen* overlays — the session is exactly as long as the overlay is visible.

## Goals / Non-Goals

**Goals:**
- A single, explicit rule for when the mic runs: **it runs iff at least one consumer needs it**, and each consumer releases only what it started.
- Make the recently-patched Beacon ownership behavior fall out of that rule rather than being a special case.
- Align UI controls to lifetime: one persistent Start/Stop bound to the hands-off output; overlays are self-contained.
- Leave room for Flash to slot in as a second persistent consumer with zero new lifecycle code.

**Non-Goals:**
- The Flash feature itself (separate change `camera-flash-output`).
- Audio pipeline, FFT, normalization, or Glyph driver changes.
- New permissions or settings-schema changes.
- A full ViewModel/architecture migration — keep the existing service + Compose-observes-StateFlow shape.

## Decisions

### Decision 1: Model capture as a consumer set in the service, not a global boolean

The service owns `consumers: Set<Consumer>` where `Consumer ∈ { PERSISTENT, SHOW, BEACON }`. Capture (mic + pipeline) runs iff the set is non-empty. `isRunning` becomes a derived value (`consumers.isNotEmpty()`) so existing observers and the widget keep working unchanged.

- `acquire(consumer)`: add to set; if capture was not running, start it (foreground service).
- `release(consumer)`: remove from set; if the set is now empty, stop capture (`stopSelf`).

**Why over alternatives:**
- *Global boolean (today)*: the source of the bug — no notion of who needs the mic, so the last toucher wins.
- *UI-layer ownership flags (the recent patch)*: works for one overlay but doesn't compose, scatters lifecycle across composables, and is fragile across activity recreation. The consumer set centralizes the rule in one testable place.

The "release only what you started" guarantee is automatic: a consumer can only `release` its own token, and a token still held by someone else keeps the mic alive.

### Decision 2: Two consumer classes with different acquire/release triggers

- **PERSISTENT** — acquired by the explicit Start action (and the widget), released by Stop. Survives screen lock. This *is* today's Start/Stop, renamed in intent. On Nothing devices it serves Glyphs; on other devices (later) it serves Flash.
- **SHOW / BEACON** — acquired when the overlay is launched, released when it is dismissed (tap or system back). These are transient and bound to overlay visibility, never to screen-off operation.

### Decision 3: Reactive-only overlays acquire; a static Beacon touches nothing

Show is always audio-reactive, so launching it always `acquire(SHOW)`. Beacon acquires `BEACON` **only when React-to-sound is on**. A Beacon with React-to-sound off renders a static hue from pure Compose state: no mic, no service, no notification. This preserves the "zero-permission findable beacon on any phone" property and means dismissing it has nothing to release.

### Decision 4: A single "Lights" tab owns the persistent session

The generic "Play" tab is absorbed into a renamed **Lights** tab that owns both rear outputs (Glyphs and, later, Flash) plus the persistent Start/Stop and the live spectrum monitor. Content is device-gated: glyph zones/brightness on Nothing phones, flash enable/intensity wherever a torch exists. The Lights tab appears whenever the device has at least one rear output. This is the home of the persistent consumer (Decision 2). Resulting nav: `[Beacon] [Show] [Lights]` on Nothing, and the same (Lights → Flash) on other phones once Flash ships.

Beacon and Show stay as separate, job-named tabs, each with its own launch action — they remain the ephemeral overlay consumers.

**Rejected: a render-layer navigation (foreground / background / lights).** It is domain-elegant — the front overlay genuinely is a background layer (solid hue or audio theme) plus an optional foreground text layer — but as *navigation* it splits the "find me" job across two tabs and uses renderer jargon instead of user intent. The layering is instead adopted only as an *internal* render model (Beacon = solid background + text; Show = theme background + no text, two entry points into one renderer). Promoting it to a unified "Screen" tab is deferred unless testers ask.

## Risks / Trade-offs

- **Orphaned overlay consumer across activity recreation** (rotation / process death): if the overlay's shown-state is transient Compose state, a recreate could drop the overlay while `BEACON`/`SHOW` stays in the set → mic stuck on. → Mitigation: hoist the overlay shown-flags to `rememberSaveable` at the activity level, make `acquire` idempotent (it's a Set), and release on confirmed dismiss. The app runs portrait-locked, so rotation is largely moot today; process death takes the whole single-process service with it. A cheap safety net: on `Activity.onDestroy` when not `isChangingConfigurations`, release the overlay consumers it owns.
- **Foreground-service notification when only an overlay is the consumer**: a reactive overlay running alone shows the service notification. → Accepted: the mic *is* active, and Android requires the foreground notification for it. It also gives the user an honest "audio is being captured" signal.
- **Double-acquire / start races** from rapid taps or recomposition. → Mitigation: Set semantics make acquire idempotent; guard capture start so a second `acquire` while starting is a no-op.
- **UI churn vs. the in-flight `beacon-tab-rework` PR**: that change just reshaped these tabs. → Mitigation: land this on top of it (rebase), and keep Decision 4's tab consolidation minimal — the lifecycle model is the substance; tab layout can follow.

## Migration Plan

Behavior-only change; no persisted settings schema change, so no data migration. Roll out by replacing the `isRunning` writes with `acquire`/`release` and deriving `isRunning` from the consumer set. Rollback = revert the commit; defaults remain sensible. Sequence after `beacon-tab-rework` is merged so the Beacon ownership flag is superseded rather than conflicting.

## Open Questions

- **Resolved:** the **Play** tab is absorbed into a **Lights** tab that owns the persistent session (Decision 4).
- **Resolved:** **Show** keeps its own launch action on the Show tab and remains relaunchable from a running session; Beacon and Show stay as distinct job-named tabs.
- **Resolved:** non-Nothing devices have **no persistent session until Flash ships** (overlays only); once `camera-flash-output` lands, Flash becomes their persistent output and the Lights tab appears.
- **Remaining (deferred):** whether the internal background+foreground render model is later surfaced as a unified "Screen" tab — tester-driven, out of scope here.
