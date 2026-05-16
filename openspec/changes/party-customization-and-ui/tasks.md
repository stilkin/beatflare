## 1. App icon (vector)

- [ ] 1.1 Create `drawable/ic_launcher_foreground.xml` — three vertical rounded bars (60%/100%/75% heights) with magenta→orange linear gradient fills
- [ ] 1.2 Create `drawable/ic_launcher_background.xml` — solid dark gray (or pure black) background
- [ ] 1.3 Update `mipmap-anydpi-v26/ic_launcher.xml` + `ic_launcher_round.xml` to reference the new vector layers
- [ ] 1.4 Delete raster `mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher*.webp` files
- [ ] 1.5 Visual check on device: launcher icon renders cleanly at all sizes, including the round/squircle masks

## 2. Notification icon

- [ ] 2.1 Create `drawable/ic_notification.xml` — monochrome white silhouette of the three bars, transparent background, follows Material notification icon guidelines
- [ ] 2.2 Update `GlyphSenseService.buildNotification` to use `R.drawable.ic_notification` for `setSmallIcon`
- [ ] 2.3 Verify on device: notification shows a recognizable icon, not a blob

## 3. Mono custom color

- [ ] 3.1 Add `monoColor: Int` field to `VisualizerSettings` (default `0xFFFFFFFF`)
- [ ] 3.2 Persist/restore `monoColor` via `SettingsStore`
- [ ] 3.3 Refactor `PartyTheme.deriveColor` signature to take a single `ThemeContext` data class (analysis, beatFlash, nowMs, settings)
- [ ] 3.4 Update `PartyOverlay` to construct and pass `ThemeContext`
- [ ] 3.5 `MONOCHROME.deriveColor` reads `ctx.settings.monoColor`, modulates its lightness with bass + quiet pulse (consistent with audio-feedback change)
- [ ] 3.6 Add a 3-slider color picker (hue / saturation / value) component, visible in Party tab only when Mono is selected

## 4. Party overlay character

- [ ] 4.1 Add `partyOverlayText: String` field to `VisualizerSettings` (default `""`)
- [ ] 4.2 Persist/restore `partyOverlayText` via `SettingsStore`
- [ ] 4.3 In `PartyOverlay`, render a centered `Text` composable when `partyOverlayText` is non-empty, sized to 60% of the shorter screen dimension
- [ ] 4.4 Add soft drop shadow for legibility against bright color washes
- [ ] 4.5 Add emoji preset row (8 presets) + single-line text field in Party tab settings
- [ ] 4.6 Tapping a preset writes to settings; typing in the field overwrites with input

## 5. Tabbed main UI

- [ ] 5.1 Refactor `MainActivity` content into `Scaffold` with `NavigationBar` at the bottom
- [ ] 5.2 Extract `PlayTab` composable: header, visualizer card, permissions, start/stop button, debug section
- [ ] 5.3 Extract `PartyTab` composable: theme selector, conditional mono picker, emoji overlay settings, Launch Party Mode button
- [ ] 5.4 Extract `GlyphsTab` composable: brightness slider, zone toggles
- [ ] 5.5 Use `rememberSaveable` for selected tab
- [ ] 5.6 Hide Glyphs tab on non-Nothing devices (bottom nav shows 2 items instead of 3)
- [ ] 5.7 Default landing tab on app launch: Play

## 6. Verification

- [ ] 6.1 Build: `./gradlew ktlintCheck :app:assembleDebug`
- [ ] 6.2 Install on Nothing Phone (3a): icon recognizable on home screen, notification icon visible in status bar
- [ ] 6.3 Tab navigation works, state survives rotation
- [ ] 6.4 Mono color picker: pick a color, see Mono theme use it in party mode, restart app, color persists
- [ ] 6.5 Emoji overlay: set "❤️", launch party mode, see heart centered + legible against all themes
- [ ] 6.6 Glyphs tab present on Phone 3a, absent on non-Nothing test device (or emulator)
- [ ] 6.7 ktlint passes
- [ ] 6.8 Bump `versionCode` for Play Store re-upload
