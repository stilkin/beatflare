## Why

Tester feedback surfaced two related UI problems:

1. **The "find friends" use case is hidden** inside the Party tab as overlay text on top of audio-reactive themes. Users didn't realize they could use the phone as a visual beacon at a concert.
2. **The Party tab is overloaded** — eight themes plus a colour picker plus an overlay-text section make it cluttered, and the Mono theme is really a "solid colour" setting in disguise (it just happens to modulate brightness with audio).

We are extracting the beacon/find-friends use case into its own first-class tab so users can find it. We are also slimming the Party tab to a curated, grouped list of audio-reactive themes, renaming it to **Show**, and dropping Mono (its role is fully replaced by Beacon).

A separate, smaller change (`quiet-noise-gate`) handles the jitter-in-near-silence complaint.

## What Changes

- **Add a new Beacon tab**, positioned leftmost: `Beacon | Play | Show | Glyphs`. The Beacon tab contains:
  - Find-me overlay (emoji presets + 1–4 graphemes, same as today's `EmojiOverlaySettings`)
  - Background hue picker (hue only, saturation locked to 1.0, brightness from "React to sound" or full)
  - Text colour picker (5-chip preset row: White / Black / Yellow / Pink / Lime; default White)
  - "React to sound" toggle (default ON — preserves current Mono-like brightness behaviour)
  - "Light up beacon" launch button
- **New full-screen Beacon overlay** launched by the Beacon tab's button. When "React to sound" is OFF, no audio service is required; when ON, the launch button auto-starts the audio pipeline if not already running.
- **BREAKING** Rename the "Party" tab to **Show** and remove the Monochrome theme. Migration: `MONO → SPECTRUM`. The previously-saved `monoColor` is preserved and reused as `beaconHue` (hue extracted, saturation discarded).
- **Remove the overlay-text feature from the Show overlay.** Overlay text now lives only on the Beacon overlay. Saved `partyOverlayText` is preserved as `beaconText`.
- **Group themes on the Show tab** into three sections, with a one-line subtitle under each *theme*:
  - Spectrum (Spectrum, Rainbow)
  - Mood (Fire, Ocean)
  - Pulse (Breathe, Sweep, Strobe)
- **Show and Beacon overlays are mutually exclusive at the screen layer.** Glyph output runs independently regardless — you can have Glyphs on the back AND Beacon on the front simultaneously.
- **Service lifecycle stays anchored on the Play tab.** Beacon may auto-start the service when needed but never auto-stops it; only the Play tab Start/Stop controls the service.

## Capabilities

### New Capabilities

- `beacon`: Full-screen "find me" beacon overlay with hue-only background, optional text, configurable text colour, and an audio-reactivity toggle. Launches from its own tab.

### Modified Capabilities

- `front-screen-viz`: Rename "party mode" terminology to "Show". Remove the overlay-text requirement (text moves to `beacon`). Add the requirement that Show and Beacon overlays are mutually exclusive.
- `party-themes`: Remove the Monochrome theme and its colour-picker requirement (replaced by Beacon). Update the built-in themes list and add a requirement for theme groupings on the Show tab.
- `settings-persistence`: Rename `monoColor → beaconHue` and `partyOverlayText → beaconText`. Add `beaconReactToSound` and `beaconTextColor`. Provide migration from old keys.

## Impact

- Code:
  - New: `ui/BeaconOverlay.kt`, `ui/BeaconTab.kt`, `ui/TextColorPickerRow.kt` (or inlined).
  - Modified: `MainActivity.kt` (4-tab bottom nav, new Beacon tab content, theme grouping on Show tab, remove overlay-text card from Show tab, remove MonoColorPicker usage).
  - Modified: `ui/PartyOverlay.kt` — drop overlay-text rendering.
  - Modified: `model/VisualizerSettings.kt` and `model/SettingsStore.kt` — rename fields, add new fields, migration logic.
  - Modified: `model/PartyTheme.kt` — remove `MONOCHROME` enum entry, remove its branch from `deriveColor`.
  - Modified: `service/GlyphSenseService.kt` — expose an auto-start path used by the Beacon button when "React to sound" is on.
- No new dependencies.
- Settings migration is one-shot at next launch; default behaviour for users with `partyTheme = MONO` is to land on `SPECTRUM` with their old colour preserved on the Beacon tab.
