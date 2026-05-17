## 1. Settings model & persistence

- [ ] 1.1 Update `VisualizerSettings` data class: remove `monoColor`, `partyOverlayText`; add `beaconHue: Float = 0f`, `beaconText: String = ""`, `beaconTextColor: BeaconTextColor = WHITE`, `beaconReactToSound: Boolean = true`
- [ ] 1.2 Add `BeaconTextColor` enum (WHITE / BLACK / YELLOW / PINK / LIME) with `Color` property
- [ ] 1.3 Update `SettingsStore` to read/write the new keys; remove old key reads except for migration
- [ ] 1.4 Implement one-shot migration in `SettingsStore.load()`:
  - `partyTheme = MONOCHROME` → `SPECTRUM`
  - `monoColor` → extract hue → `beaconHue`
  - `partyOverlayText` → `beaconText`
  - Clear legacy keys after migration

## 2. PartyTheme cleanup

- [ ] 2.1 Remove `MONOCHROME` from the `PartyTheme` enum
- [ ] 2.2 Remove the `MONOCHROME` branch from `deriveColor(ctx: ThemeContext)`
- [ ] 2.3 Ensure no compile references remain to `PartyTheme.MONOCHROME`

## 3. Beacon overlay

- [ ] 3.1 Create `ui/BeaconOverlay.kt` — full-screen Box, BackHandler, keep-screen-on, tap-to-dismiss, "Tap to exit" hint
- [ ] 3.2 Background colour: `HSV(beaconHue, 1.0, brightness)`; brightness = 1.0 when React-to-sound is OFF, audio-modulated (with quiet-state pulse) when ON
- [ ] 3.3 Reuse text rendering from `PartyOverlay` (auto-fit via TextMeasurer, rotate when multi-grapheme, Bungee Shade font) with selected `beaconTextColor`
- [ ] 3.4 Wire `BeaconOverlay` to `GlyphSenseService.analysisFlow` only when React-to-sound is ON

## 4. PartyOverlay cleanup

- [ ] 4.1 Remove overlay-text rendering from `PartyOverlay` (text drawing, TextMeasurer logic, rotation, BungeeShade)
- [ ] 4.2 If text helpers are now reusable, extract to a small shared file used by `BeaconOverlay`

## 5. Beacon configuration UI

- [ ] 5.1 Create `ui/BeaconHuePicker.kt` (or refactor `MonoColorPicker.kt` in place) — single hue slider 0..360°, 40dp preview swatch showing `HSV(hue, 1, 1)`
- [ ] 5.2 Create `ui/TextColorPickerRow.kt` — 5 circular chips (32dp) for the `BeaconTextColor` enum with selection indicator
- [ ] 5.3 Create `BeaconTab` composable in `MainActivity.kt` — composes: find-me overlay control (reuse `EmojiOverlaySettings`, rewired to `beaconText`), hue picker, text colour row, React-to-sound switch, "Light up beacon" button
- [ ] 5.4 "Light up beacon" handler: if React-to-sound is ON and service is not running, call `GlyphSenseService.start(context)`; then set `showBeacon = true` and `showParty = false`

## 6. MainActivity bottom-nav rework

- [ ] 6.1 Update `NavigationBar` to four items in order Beacon, Play, Show, Glyphs (unicode glyph icons: ★ ▶ ✦ ✱ or similar — pick a glyph for Beacon)
- [ ] 6.2 Rename internal references from "Party" to "Show" where they refer to the tab/output label (NOT the `partyOutputEnabled` storage key — leave that)
- [ ] 6.3 Default selected tab remains Play (index 1)
- [ ] 6.4 Add `BeaconTab` composable to the tab content `when` block
- [ ] 6.5 Add `showBeacon` state and render `BeaconOverlay` when true; ensure render-time mutual exclusion with `showParty`

## 7. Show tab theme grouping

- [ ] 7.1 Replace the flat theme list with three labelled sections: Spectrum, Mood, Pulse
- [ ] 7.2 Each theme row shows theme name (bold) + one-line subtitle + selected indicator (checkmark or radio)
- [ ] 7.3 Use the subtitle wording from the design doc table (refine if natural during coding)
- [ ] 7.4 Remove the `MonoColorPicker` from the Show tab content
- [ ] 7.5 Remove the `EmojiOverlaySettings` from the Show tab content (it moved to Beacon)

## 8. Service-and-overlay interaction

- [ ] 8.1 Ensure that when the user taps Start on Play with Show enabled, any visible Beacon overlay is dismissed first
- [ ] 8.2 Ensure that when "Light up beacon" is tapped, any visible Show overlay is dismissed first
- [ ] 8.3 Confirm dismissing Beacon does NOT stop the service (test by starting from Play, opening Beacon, dismissing)

## 9. Build & validate

- [ ] 9.1 `./gradlew ktlintCheck :app:assembleDebug` succeeds
- [ ] 9.2 Install on Nothing Phone (3a) — confirm: 4 tabs visible, Play selected by default
- [ ] 9.3 Beacon with React-to-sound OFF — no service starts, solid bright colour, text renders
- [ ] 9.4 Beacon with React-to-sound ON — service auto-starts, breathing brightness, quiet pulse during silence
- [ ] 9.5 Dismiss Beacon while service is running — service keeps running (visible in notification)
- [ ] 9.6 Open Show overlay then navigate to Beacon and launch — Show dismisses cleanly, Beacon shows
- [ ] 9.7 Verify migration: install over an older build that has `partyTheme = MONOCHROME` and a custom Mono colour — on launch user lands on Spectrum, Beacon hue matches old colour's hue
- [ ] 9.8 Verify the spectrum visualizer card on Play still tap-relaunches the Show overlay
- [ ] 9.9 Verify Glyphs continue working under Beacon (glyphs lit on the back, Beacon on the front)

## 10. Docs

- [ ] 10.1 Update README "Features" section: Beacon tab, hue picker (no saturation), text colour presets, React-to-sound, theme groupings
- [ ] 10.2 Update `docs/ARCHITECTURE.md`: 4-tab layout, BeaconOverlay alongside PartyOverlay, settings migration note
