## Context

GlyphSense works on the Phone (3a) but crashes on non-Nothing devices. Settings are ephemeral. Party mode has one color scheme. These are three small, independent improvements grouped into one change.

## Goals / Non-Goals

**Goals:**
- App runs on any Android 14+ device (front-screen visualization only on non-Nothing)
- Settings persist across app restarts
- Multiple party mode color themes

**Non-Goals:**
- Supporting other Nothing Phone LED layouts (separate change)
- Complex settings UI or preferences screen
- Animated theme transitions

## Decisions

### 1. Non-Nothing device support

**Approach**: Check `Common.isXXXXX()` methods at service start. If none match, skip glyph init entirely. The audio pipeline and party mode work as normal. UI hides zone toggles and brightness slider since they only affect glyphs.

**Where**: `GlyphSenseService.startPipeline()` — conditional glyph init. `GlyphController.isNothingDevice()` — new helper. `MainActivity` — conditional UI rendering.

### 2. Settings persistence via SharedPreferences

**Approach**: Simple `SharedPreferences` read/write. No DataStore (overkill for 5 values). Load on service companion init, save on every `updateSettings()` call.

**Data**: brightness (Float), zoneCEnabled/zoneAEnabled/zoneBEnabled (Boolean), partyTheme (String enum name).

**Where**: New `SettingsStore` utility class. Called from `GlyphSenseService.updateSettings()`.

### 3. Party themes

**Approach**: Each theme is a `(AudioAnalysis, Int beatFlash) -> Color` function. Add a `PartyTheme` enum with entries. `PartyOverlay` reads the selected theme from settings and delegates color derivation.

**Themes**:
- **Spectrum** (current default): hue from dominant frequency
- **Fire**: red-orange-yellow, bass drives intensity
- **Ocean**: blue-teal-cyan, mid-freq shifts hue
- **Monochrome**: white only, amplitude drives brightness
- **Rainbow**: hue cycles over time, beat resets the cycle
- **Strobe**: pure black/white flash on beat detection only

## Risks / Trade-offs

- **SharedPreferences on main thread**: Reads are fast enough for our 5 values. Writes via `apply()` (async) avoid blocking.
- **Theme selection UX**: A dropdown/chips in the settings panel. Simple but might look cramped. Could use a bottom sheet later if needed.
