## Context

The app shipped with a single scrollable `Column` and a raster launcher icon. Real-world feedback after Play Store submission asked for: a proper notification icon, a vector-clean app icon, more party-mode customization (custom mono color, emoji "find me" overlay), and tabs to reduce scroll on return visits.

## Goals / Non-Goals

**Goals:**
- Notification icon recognizable, vector, follows Android masking conventions.
- Launcher icon as adaptive vector — no raster mipmaps.
- Mono theme supports any user-chosen color.
- Party overlay supports an optional centered character (emoji or text) for "find me at the festival".
- Tabbed main UI: Play / Party / Glyphs.

**Non-Goals:**
- Theme refactor into `(colorSource × motion)` composition — explicitly deferred from the prior proposal.
- Per-theme settings beyond Mono — Fire/Ocean/Sweep/etc. stay parameter-free.
- Multi-character or animated overlay text — single character/emoji only.
- Light theme / dynamic color theming — still dark only.

## Decisions

### 1. App icon (flat vector redesign)

The current JPG icon (3D-rendered metal/gradient bars) cannot be reproduced as Android vector — vectors don't do photorealistic shading. Redo as a flat vector design that *evokes* the original:

- **Foreground**: three vertical rounded bars at heights 60%/100%/75% (matching the original's silhouette), filled with linear gradients in the existing brand colors (magenta `#E91E8C` → orange `#F4811E`).
- **Background**: solid dark gray (`#2C2C2C`) approximating the brushed-metal background, or pure black for max OLED contrast.
- Adaptive icon: separate foreground + background drawable layers, per Android 8+ convention. Both as VectorDrawable XML in `drawable/`.
- Remove all `mipmap-*dpi/ic_launcher*.webp` raster variants. Keep only `mipmap-anydpi-v26/ic_launcher.xml` referring to the vector layers.

### 2. Notification icon

Android masks notification small icons to a single color (since API 21). Use a monochrome vector — same three-bar silhouette as the launcher but **white-only, transparent background**. Tinted by Android automatically. Path: `drawable/ic_notification.xml`.

`GlyphSenseService.buildNotification` switches from `R.mipmap.ic_launcher` to `R.drawable.ic_notification`.

### 3. Mono theme color

Add `monoColor: Int` (ARGB) to `VisualizerSettings`, default white (`0xFFFFFFFF`). `PartyTheme.MONOCHROME` reads it from the settings passed through to `deriveColor`.

This requires extending the `deriveColor` signature *again* — it already takes `analysis`, `beatFlash`, and (after the audio-feedback change) `nowMs`. To avoid a four-parameter signature, group into a `ThemeContext` data class:

```kotlin
data class ThemeContext(
    val analysis: AudioAnalysis,
    val beatFlash: Int,
    val nowMs: Long,
    val settings: VisualizerSettings,
)
```

`deriveColor(ctx: ThemeContext): Color`. Other themes ignore `settings` for now; Mono reads `ctx.settings.monoColor`. This is a defensible API shape because it leaves room for future per-theme settings without further signature churn.

**Color picker UI**: Use a simple HSL hue slider + saturation/value sliders, NOT a full hue/saturation/value 2D wheel. Compose Material 3 has no built-in picker; rolling our own as three sliders keeps it ~50 LOC and avoids adding a dependency.

### 4. Emoji/letter overlay

Add `partyOverlayText: String` to `VisualizerSettings`, default empty (no overlay). Rendered in `PartyOverlay` as a centered `Text` composable when non-empty.

**Sizing**: `fontSize = (min(screenWidth, screenHeight) * 0.6f).sp` computed from `LocalConfiguration`. 60% of the shorter dimension comfortably fits most single emoji/letters and leaves margin.

**Legibility**: Add a soft drop shadow (radius ~24dp, alpha 0.4) so the character reads against bright color washes. Color: `Color.White.copy(alpha = 0.95f)` — works on most theme outputs since color themes generally avoid pure white.

**Input UI** (in Party tab): A row of 8 emoji presets (e.g., ❤️ 🦄 ⭐ 🔥 🌊 🎵 ✨ 💜) + a `TextField` with `singleLine = true`. Tapping a preset writes it to the field (and to settings). Typing in the field overwrites with whatever's typed. No length cap — Android handles single emoji as 1+ codepoints; a `maxLength = 8` chars guard is enough.

The text field's placeholder/hint shows one of the presets so users understand "emoji goes here." `KeyboardType.Text` — Android does not have an emoji-only keyboard mode that's universally supported, so accept that users may need to tap an emoji preset or switch their keyboard.

### 5. Tabs (bottom navigation)

```
┌──────────────────────────────────┐
│  [logo + status dot, small]      │
│                                  │
│  ┌────────────────────────────┐  │
│  │  Tab content                │  │
│  │                             │  │
│  └────────────────────────────┘  │
│                                  │
├──────────────────────────────────┤
│  ▶ Play   🎉 Party   💡 Glyphs   │
└──────────────────────────────────┘
```

State: `var selectedTab by rememberSaveable { mutableStateOf(Tab.Play) }`. `rememberSaveable` so the selected tab survives config changes (rotation).

Each tab is its own `@Composable`:
- `PlayTab(modifier, isRunning, ...)`: header (logo + status dot), `VisualizerCard`, permission buttons (if needed), `GradientButton` (start/stop). Debug section also lives here.
- `PartyTab(modifier, settings, onSettingsChange, isRunning, onPartyMode)`: theme selector, conditional mono color picker (only when `MONOCHROME` selected), emoji overlay settings, Launch Party Mode button.
- `GlyphsTab(modifier, settings, onSettingsChange)`: brightness slider, zone toggles. Hidden on non-Nothing devices.

Non-Nothing devices: only 2 tabs (Play + Party). Bottom nav adapts based on `GlyphSenseService.isNothingDevice`.

**Why bottom nav, not top tabs**: thumb-reachable on a phone; standard Material 3 pattern; no surprise for users.

### 6. Settings migration

`VisualizerSettings` gains `monoColor: Int` and `partyOverlayText: String`. `SettingsStore` reads with defaults so existing users upgrade cleanly (missing keys → defaults). No version bump or migration needed since the store uses key-based SharedPreferences-style loading.

## Risks / Trade-offs

- **Vector icon won't look identical** to the original 3D-rendered version. Accepted by user; the new design will be cleaner and more on-brand for Android.
- **Three-slider color picker** is less elegant than a 2D wheel but trades visual polish for simplicity and no new dependency. Acceptable for a feature used ~once per install.
- **Tab restructure changes muscle memory** for existing users — but app has only just shipped, user base is small, and the new pattern is more discoverable for new users.
- **Emoji rendering depends on system font** — older Android versions render newer emoji as boxes. Min SDK is 34 (Android 14+), so this risk is low.
- **`ThemeContext` data class** is a minor allocation per frame (~22 Hz). Negligible. Acceptable.
