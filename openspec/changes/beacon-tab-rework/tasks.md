## 1. Settings model & persistence

- [x] 1.1 Update `VisualizerSettings` data class: remove `monoColor`, `partyOverlayText`; add `beaconHue: Float = 0f`, `beaconText: String = ""`, `beaconTextColor: BeaconTextColor = WHITE`, `beaconReactToSound: Boolean = true`
- [x] 1.2 Add `BeaconTextColor` enum (WHITE / BLACK / YELLOW / PINK / LIME) with `Color` property
- [x] 1.3 Update `SettingsStore` to read/write the new keys; remove old key reads except for migration
- [x] 1.4 Implement one-shot migration in `SettingsStore.load()`:
  - `partyTheme = MONOCHROME` → `SPECTRUM`
  - `monoColor` → extract hue → `beaconHue`
  - `partyOverlayText` → `beaconText`
  - Clear legacy keys after migration

## 2. PartyTheme cleanup

- [x] 2.1 Remove `MONOCHROME` from the `PartyTheme` enum
- [x] 2.2 Remove the `MONOCHROME` branch from `deriveColor(ctx: ThemeContext)`
- [x] 2.3 Ensure no compile references remain to `PartyTheme.MONOCHROME`

## 3. Beacon overlay

- [x] 3.1 Create `ui/BeaconOverlay.kt` — full-screen Box, BackHandler, keep-screen-on, tap-to-dismiss, "Tap to exit" hint
- [x] 3.2 Background colour: `HSV(beaconHue, 1.0, brightness)`; brightness = 1.0 when React-to-sound is OFF, audio-modulated (with quiet-state pulse) when ON
- [x] 3.3 Reuse text rendering from `PartyOverlay` (auto-fit via TextMeasurer, rotate when multi-grapheme, Bungee Shade font) with selected `beaconTextColor`
- [x] 3.4 Wire `BeaconOverlay` to `GlyphSenseService.analysisFlow` only when React-to-sound is ON

## 4. PartyOverlay cleanup

- [x] 4.1 Remove overlay-text rendering from `PartyOverlay` (text drawing, TextMeasurer logic, rotation, BungeeShade)
- [x] 4.2 ~~If text helpers are now reusable, extract to a small shared file used by `BeaconOverlay`~~ — moved them into `BeaconOverlay` directly; no second caller, no extraction warranted (prime directive)

## 5. Beacon configuration UI

- [x] 5.1 Create `ui/BeaconHuePicker.kt` — single hue slider 0..360°, 40dp preview swatch showing `HSV(hue, 1, 1)`
- [x] 5.2 Text-colour picker row — 5 36dp circular chips with a magenta dot indicator (inlined as `BeaconTextColorRow` in `MainActivity.kt`; single use site, no separate file)
- [x] 5.3 Create `BeaconTab` composable in `MainActivity.kt` — composes: find-me overlay control (reuse `EmojiOverlaySettings`, rewired to `beaconText`), hue picker, text colour row, React-to-sound switch, "Light up beacon" button
- [x] 5.4 "Light up beacon" handler: if React-to-sound is ON and service is not running, call `GlyphSenseService.start(context)`; then set `showBeacon = true` and `showParty = false`

## 6. MainActivity bottom-nav rework

- [x] 6.1 Update `NavigationBar` to four items in order Beacon (★), Play (▶), Show (✦), Glyphs (✱)
- [x] 6.2 Rename internal references from "Party" to "Show" where they refer to the tab/output label (NOT the `partyOutputEnabled` storage key — left as-is)
- [x] 6.3 Default selected tab remains Play (Tab.Play)
- [x] 6.4 Add `BeaconTab` composable to the tab content `when` block
- [x] 6.5 Add `showBeacon` state and render `BeaconOverlay` when true; render-time mutual exclusion with `partyMode` via `if/else if`

## 7. Show tab theme grouping

- [x] 7.1 Replace the flat theme list with three labelled sections: Spectrum, Mood, Pulse
- [x] 7.2 Each theme row shows theme name (bold) + one-line subtitle + selected indicator (checkmark)
- [x] 7.3 Use the subtitle wording from the design doc table
- [x] 7.4 Remove the `MonoColorPicker` from the Show tab content (file deleted)
- [x] 7.5 Remove the `EmojiOverlaySettings` from the Show tab content (it moved to Beacon)

## 8. Service-and-overlay interaction

- [x] 8.1 When the user taps Start on Play with Show enabled, any visible Beacon overlay is dismissed first (via `onLaunchParty` lambda that clears `showBeacon`)
- [x] 8.2 When "Light up beacon" is tapped, any visible Show overlay is dismissed first (via `onLaunchBeacon` lambda that clears `partyMode`)
- [x] 8.3 Dismissing Beacon does NOT stop the service — only the Play tab Start/Stop touches the service

## 9. Build & validate

- [x] 9.1 `./gradlew ktlintCheck :app:assembleDebug` succeeds
- [x] 9.2 Install on Nothing Phone (3a) — confirm: 4 tabs visible, Play selected by default — superseded by capture-lifecycle-rework (now [Beacon][Show][Lights], Beacon default; verified)
- [x] 9.3 Beacon with React-to-sound OFF — no service starts, solid bright colour, text renders
- [x] 9.4 Beacon with React-to-sound ON — service auto-starts, breathing brightness, quiet pulse during silence
- [x] 9.5 Dismiss Beacon while service is running — service keeps running (visible in notification)
- [ ] 9.6 Open Show overlay then navigate to Beacon and launch — Show dismisses cleanly, Beacon shows
- [ ] 9.7 Verify migration: install over an older build that has `partyTheme = MONOCHROME` and a custom Mono colour — on launch user lands on Spectrum, Beacon hue matches old colour's hue
- [x] 9.8 Verify the spectrum visualizer card on Play still tap-relaunches the Show overlay — superseded by capture-lifecycle-rework (card now on Lights; verified in 6.5)
- [x] 9.9 Verify Glyphs continue working under Beacon (glyphs lit on the back, Beacon on the front)

## 10. Docs

- [x] 10.1 Update README "Features" section: Beacon tab, hue picker (no saturation), text colour presets, React-to-sound, theme groupings
- [x] 10.2 Update `docs/ARCHITECTURE.md`: 4-tab layout, BeaconOverlay alongside PartyOverlay, settings migration note
