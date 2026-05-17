## 1. App icon (vector)

- [x] 1.1 Create `drawable/ic_launcher_foreground.xml` — three vertical rounded bars (60%/100%/75% heights) with magenta→orange linear gradient fills
- [x] 1.2 Create `drawable/ic_launcher_background.xml` — solid dark gray (or pure black) background
- [x] 1.3 Update `mipmap-anydpi-v26/ic_launcher.xml` + `ic_launcher_round.xml` to reference the new vector layers
- [x] 1.4 Delete raster `mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher*.webp` files
- [x] 1.5 Visual check on device: launcher icon renders cleanly at all sizes, including the round/squircle masks

## 2. Notification icon

- [x] 2.1 Create `drawable/ic_notification.xml` — monochrome white silhouette of the three bars, transparent background, follows Material notification icon guidelines
- [x] 2.2 Update `GlyphSenseService.buildNotification` to use `R.drawable.ic_notification` for `setSmallIcon`
- [x] 2.3 Verify on device: notification shows a recognizable icon, not a blob

## 3. Mono custom color

- [x] 3.1 Add `monoColor: Int` field to `VisualizerSettings` (default `0xFFFFFFFF`)
- [x] 3.2 Persist/restore `monoColor` via `SettingsStore`
- [x] 3.3 Refactor `PartyTheme.deriveColor` signature to take a single `ThemeContext` data class (analysis, beatFlash, nowMs, settings)
- [x] 3.4 Update `PartyOverlay` to construct and pass `ThemeContext`
- [x] 3.5 `MONOCHROME.deriveColor` reads `ctx.settings.monoColor`, modulates its lightness with bass + quiet pulse (consistent with audio-feedback change)
- [x] 3.6 Add a 3-slider color picker (hue / saturation / value) component, visible in Party tab only when Mono is selected

## 4. Party overlay character

- [x] 4.1 Add `partyOverlayText: String` field to `VisualizerSettings` (default `""`)
- [x] 4.2 Persist/restore `partyOverlayText` via `SettingsStore`
- [x] 4.3 In `PartyOverlay`, render a centered `Text` composable when `partyOverlayText` is non-empty, sized to 60% of the shorter screen dimension
- [x] 4.4 Add soft drop shadow for legibility against bright color washes
- [x] 4.5 Add emoji preset row (8 presets) + single-line text field in Party tab settings
- [x] 4.6 Tapping a preset writes to settings; typing in the field overwrites with input

## 5. Tabbed main UI

- [x] 5.1 Refactor `MainActivity` content into `Scaffold` with `NavigationBar` at the bottom
- [x] 5.2 Extract `PlayTab` composable: header, visualizer card, permissions, start/stop button, debug section
- [x] 5.3 Extract `PartyTab` composable: theme selector, conditional mono picker, emoji overlay settings, Launch Party Mode button
- [x] 5.4 Extract `GlyphsTab` composable: brightness slider, zone toggles
- [x] 5.5 Use `rememberSaveable` for selected tab
- [x] 5.6 Hide Glyphs tab on non-Nothing devices (bottom nav shows 2 items instead of 3)
- [x] 5.7 Default landing tab on app launch: Play

## 6. Verification

- [x] 6.1 Build: `./gradlew ktlintCheck :app:assembleDebug`
- [x] 6.2 Install on Nothing Phone (3a): icon recognizable on home screen, notification icon visible in status bar
- [x] 6.3 Tab navigation works, state survives rotation
- [x] 6.4 Mono color picker: pick a color, see Mono theme use it in party mode, restart app, color persists
- [x] 6.5 Emoji overlay: set "❤️", launch party mode, see heart centered + legible against all themes
- [x] 6.6 Glyphs tab present on Phone 3a, absent on non-Nothing test device (or emulator)
- [x] 6.7 ktlint passes
- [x] 6.8 Bump `versionCode` for Play Store re-upload

## Divergences from spec

Documented during implementation; specs/* updated to match shipped behaviour.

- **3.6 — color picker is hue+saturation, not HSV.** Mono brightness is
  audio-driven, so a user-controlled value slider would be redundant.
  Picker shows a live preview swatch instead.
- **4.3 — overlay sized to 55% of shorter screen dim**, not 60%. Cosmetic
  tuning after on-device testing; left this number free to evolve.
- **4.4 — shadow reduced to a soft 6dp offset / 12 blur** because the
  bundled **Bungee Shade** font carries its own 3D depth. The shadow is
  now just for contrast on pale backgrounds.
- **5.3 — Launch Party Mode button removed from Party tab.** Replaced
  with **output toggles on Play tab** (Glyphs / Party mode, Nothing
  devices only). Tapping Start activates whichever outputs are enabled;
  tapping the visualiser card while running re-launches the overlay.
  This is a UX win over the original button — see the new
  `front-screen-viz` requirements for the canonical behaviour.

## Bonus work (not in original tasks)

- Bundled **Bungee Shade** font for the overlay (~300 KB in `res/font/`).
- Multi-symbol overlays **rotate −90° (CCW)** along the screen's long
  axis so each glyph stays large.
- Character cap reduced from 8 to **4** in the input field; keyboard
  opens in **CAPS** mode.
- **BackHandler** dismisses the overlay back to the tabbed view instead
  of finishing the activity.
- Service skips the Glyph SDK session entirely when the Glyphs output
  toggle is off.
