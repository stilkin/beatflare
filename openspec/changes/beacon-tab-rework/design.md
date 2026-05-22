## Context

The current 3-tab UI (Play / Party / Glyphs) bundles three different concerns onto the Party tab: choosing one of eight themes, configuring a custom Mono color, and configuring an overlay text/emoji for the find-friends use case. Testers reported the find-friends feature is buried, and that the Mono theme + overlay-text combo (the actual beacon configuration) isn't recognisable as such.

This change reorganises around *what the user wants to do*:
- **Play**: control the visualizer (start/stop, output toggles)
- **Show**: pick a music-reactive theme to display
- **Beacon**: be findable in a crowd
- **Glyphs**: configure glyph LED output

The Mono theme exists only because there was no other place to put a "solid color, audio-reactive brightness" mode. Beacon now owns that idea — and extends it with a clearer toggle for "audio reactive vs static".

## Goals / Non-Goals

**Goals:**
- Make the find-friends use case discoverable: it has its own tab and its own launch button.
- Slim the Show tab to focus on theme selection.
- Allow Beacon to be used without granting microphone permission (when "React to sound" is off).
- Preserve user investment in their saved settings (Mono color, overlay text) by migrating, not deleting.
- Keep glyph output independent of which front-screen mode is active.

**Non-Goals:**
- No new themes, no theme-engine changes (theme rendering code is untouched aside from removing the MONOCHROME branch).
- No mic-permission-removal heroics — when React-to-sound is on, the existing service path is reused as-is.
- No new background/foreground service. Beacon-with-React-to-sound-off renders purely from Compose state.
- No animated transitions between overlays.
- No multi-line text overlays. Overlay stays single line.

## Decisions

### Beacon overlay is a separate Composable, not a flag on PartyOverlay

`PartyOverlay` and `BeaconOverlay` share boilerplate (full-screen Box, BackHandler, keep-screen-on, tap-to-dismiss, "Tap to exit" hint, frame-millis ticker for animation). The bodies diverge enough that branching inside a single composable would obscure intent:
- `PartyOverlay` reads `settings.partyTheme` and calls `deriveColor(ThemeContext)`; renders no text.
- `BeaconOverlay` reads `settings.beaconHue`, `settings.beaconReactToSound`, `settings.beaconText`, `settings.beaconTextColor`; renders solid colour (or audio-modulated colour) plus optional centred text.

Shared boilerplate can move into a small `FullscreenOverlay` helper if it gets duplicated; not required up-front.

Alternative considered: a single `FrontOverlay(mode: FrontOverlayMode)` with `enum FrontOverlayMode { SHOW, BEACON }`. Rejected — branching on mode for nearly every field is more cognitive load than two ~80-line composables.

### Launching the Beacon overlay

State is held in `MainActivity`: `var showBeacon by remember { mutableStateOf(false) }`. The "Light up beacon" button does:

1. If `settings.beaconReactToSound` is true AND the service isn't running, call `GlyphSenseService.start(context)` (existing entry point).
2. Set `showBeacon = true`.

When `showBeacon` is true, render `BeaconOverlay` over the main UI (same pattern as the existing party overlay). Tap or back dismisses it (sets `showBeacon = false`). The service is **not** stopped on dismiss — that's Play tab's job.

Mutual exclusion with Show overlay: only one `var showOverlay` state would be needed if both were the same. Cleanest: separate states `showParty` and `showBeacon`, and in the launchers, set the *other* to false. The render-side guard `if (showBeacon) BeaconOverlay(...) else if (showParty) PartyOverlay(...)` is also safe and prevents accidental stacking.

### Background color: hue only, saturation locked

The picker exposes only a single hue slider (0..360°), preview swatch, and the React-to-sound toggle. Saturation = 1.0 is hardcoded. Brightness:
- React-to-sound OFF → brightness = 1.0 (fully bright; this is the "find me" use case)
- React-to-sound ON → brightness = audio-driven via the same pulse formula as the old Mono theme, with the same quiet-state baseline pulse so it never goes pitch-black

This means `Color.hsv(beaconHue, 1.0f, brightness)` for the final colour. No alpha.

### Text colour: enum, not free picker

```kotlin
enum class BeaconTextColor(val color: Color) {
    WHITE(Color.White),
    BLACK(Color.Black),
    YELLOW(Color(0xFFFFEB3B)),
    PINK(Color(0xFFFF4081)),
    LIME(Color(0xFFCDDC39)),
}
```

Rendered as a 5-chip horizontal row of 32dp circular swatches with a selected outline. Default `WHITE`. Storage: ordinal `Int` in SharedPreferences.

Alternative considered: same hue picker for text as for background. Rejected — guarantees nothing about contrast and adds complexity. A curated palette is foolproof for the beacon use case.

### Show overlay loses overlay text

`partyOverlayText` rendering is removed from `PartyOverlay`. The setting key in SharedPrefs is *read* once at migration (see below) and not written there afterward.

### Theme groupings on the Show tab

Render each group as a labelled `Text` heading followed by a vertical list of selectable theme rows. Each theme row shows: theme name, one-line subtitle, and a selected indicator. We are NOT building horizontal chip rows — vertical rows handle subtitle text naturally and match the existing settings-row style.

Initial subtitles (per theme, may be refined during apply):

| Group | Theme | Subtitle |
|---|---|---|
| Spectrum | Spectrum | Color follows the music's frequency |
| Spectrum | Rainbow | Hue cycles continuously, brightness from bass |
| Mood | Fire | Warm reds and oranges, intensity tracks bass |
| Mood | Ocean | Cool blues and teals, hue shifts with mids |
| Pulse | Breathe | Slow sine pulse on a fixed hue |
| Pulse | Sweep | Slow hue rotation across cool tones |
| Pulse | Strobe | Bright white flash on every beat |

### Settings migration

`SettingsStore.load()` performs a one-shot migration when reading prefs:

1. If `partyTheme == MONO` → write `partyTheme = SPECTRUM` and continue.
2. If `monoColor` is set but `beaconHue` is not → extract HSV from `monoColor` and store the hue as `beaconHue`. Discard saturation (forced to 1.0 anyway).
3. If `partyOverlayText` is set but `beaconText` is not → copy the value to `beaconText`.
4. Old keys can be left in place (cheap) or cleared (cleaner). We'll **clear** them after the first successful migration to avoid stale state confusing later code reads.

Migration runs on the synchronous load path in `SettingsStore`, before any reactive flow emits.

### Play tab unchanged in concept

The Play tab still owns the Start/Stop button, the output toggles (Glyphs, Show), and the spectrum visualizer card with tap-to-relaunch-Show behaviour. The label of the second output toggle changes from "Party" to "Show". The toggle's persisted key (`partyOutputEnabled`) stays the same — internal naming churn isn't worth a migration.

## Risks / Trade-offs

- [Risk: Two front-screen overlays both visible due to a state-management bug] → Mitigation: render-side `if/else if` between them; only one branch can be taken at a time. Unit-testable.
- [Risk: User taps "Light up beacon" expecting it to start glyphs too] → Mitigation: Beacon button copy explicitly references the beacon. Glyph control remains on Play tab; not changed by Beacon launch.
- [Risk: Saved Mono colour with low saturation becomes a wildly different hue at saturation=1.0] → Mitigation: this is acceptable for migration. The user explicitly stated migration doesn't need to "make sense" for testers, only "not crash". We extract hue and ignore saturation; a previously beige Mono will become a vibrant orange. User can re-pick.
- [Risk: Bottom nav with 4 tabs feels cramped on small phones] → Mitigation: Material 3 NavigationBar handles 3–5 destinations natively. Labels are short ("Beacon"/"Play"/"Show"/"Glyphs").
- [Risk: Auto-starting the service from Beacon surprises users on a non-Nothing device that never expected mic capture] → Mitigation: "React to sound" toggle defaults ON to preserve current behaviour, but users who don't want it can switch it off and Beacon works permission-free. The toggle label is clear about what it enables.

## Migration Plan

1. Ship the change. On first launch with the new build:
   - SettingsStore migrates old keys.
   - Users on `MONO` theme land on `SPECTRUM` with a notification-style toast? No — silently. Testers don't need ceremony.
2. Rollback = revert to the previous build; old SharedPrefs keys remain (we didn't delete data, only mapped it). If we did clear old keys on migration, rollback users will see defaults, not their old Mono colour. Acceptable for the tester group.

## Open Questions

None major. Final theme subtitle wording will be finalised during apply — the table above is the working set.
