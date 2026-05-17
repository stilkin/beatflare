## Why

Four pieces of UX polish that emerged from real-world testing:

1. **Notification icon is just a white blob.** The launcher mipmap is being used as the notification small icon — Android masks it to a single-color silhouette, so the result is unrecognizable. Needs a dedicated monochrome vector.
2. **App icon needs to be reproducible as vector.** Current launcher icon is a soft-shaded JPG which can't be expressed as an Android vector drawable. A flat-design redo lets the icon ship as a true adaptive vector across all densities and theming.
3. **Customization is limited.** Mono theme is locked to white. Party mode is just a color wash — nothing identifying. Two requested features address this:
   - Custom color picker for the Mono theme.
   - Optional emoji/letter overlay on the party screen ("look for the heart 💜 — that's me") for the "find each other at a festival" use case.
4. **Main screen is one long scroll.** Configure-once, launch-often usage pattern suggests tabs. Bottom navigation: **Play** (big start/stop, status, visualizer card), **Party** (party mode settings + launch), **Glyphs** (brightness, zone toggles — Nothing devices only).

## What Changes

### Branding
- New flat-vector app icon (adaptive icon: foreground vector layer + colored background).
- New monochrome notification small icon (white silhouette on transparent, vector).
- Re-derive accent palette from the new flat icon if hues drift; otherwise keep existing magenta/orange.

### Party customization
- Mono theme gains a user-configurable color (color picker in settings, persisted).
- Party overlay gains an optional centered character (emoji or letter), persisted. Default empty (no overlay). Sized to ~60% of the shorter screen dimension. Soft outline/shadow for legibility over bright color washes.
- Settings UI for the character: a row of 6–8 emoji presets + a single-line text field. Text field defaults to one of the presets as placeholder.

### Main UI
- Replace single scrollable column with a `Scaffold` + bottom `NavigationBar` (3 tabs).
- **Play tab**: status dot, visualizer card, start/stop button, permissions prompts.
- **Party tab**: theme selector, mono color picker (when Mono selected), emoji overlay settings, Launch Party Mode button.
- **Glyphs tab**: brightness slider, zone toggles. Hidden entirely on non-Nothing devices (degrades to two tabs).

## Capabilities

### Modified Capabilities
- `app-branding`: New adaptive vector launcher icon + dedicated notification icon; layout no longer described as a single scrollable column.
- `front-screen-viz`: Party overlay supports an optional centered character.
- `party-themes`: Mono theme accepts a user-configured color.
- `settings-persistence`: Persists mono color and overlay character.

## Impact

- **Files affected**:
  - Resources: `mipmap-anydpi-v26/` (adaptive XML), `drawable/ic_notification.xml`, `drawable/ic_launcher_*.xml`, `values/colors.xml`, raster mipmaps removed.
  - Code: `MainActivity.kt` (tabbed layout), `PartyOverlay.kt` (character overlay), `PartyTheme.kt` (Mono accepts color param), `VisualizerSettings.kt` (two new fields), `SettingsStore.kt` (persist new fields), `service/GlyphSenseService.kt` (notification builder uses new icon).
- **Risk**: Medium. Tabs touch the highest-traffic file (`MainActivity.kt`) — small mistakes are very visible. Icon change is irreversible-feeling but safe (no API/signing impact since AppID is unchanged).
- **Backwards compatibility**: `VisualizerSettings` gains two fields; persisted preferences need a default for missing keys. Existing users land on the Play tab on first launch after upgrade.
- **Verification**: Manual on device — icon on home screen, notification appearance, tab navigation, mono color picker, emoji overlay legibility against bright backgrounds.
