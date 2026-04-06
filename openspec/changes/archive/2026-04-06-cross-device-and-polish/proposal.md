## Why

GlyphSense currently only works on the Nothing Phone (3a) and crashes on non-Nothing devices during glyph init. The party mode front-screen visualization has no dependency on glyph hardware, so it should work on any Android 14+ phone. Additionally, settings (brightness, zone toggles) reset on app kill, and the party mode only has one color scheme. These three improvements — non-Nothing support, settings persistence, and party themes — are small, independent changes that together make the app significantly more polished and broadly usable.

## What Changes

- Guard glyph SDK init so the app runs gracefully on non-Nothing phones (front-screen visualization only)
- Persist `VisualizerSettings` to `SharedPreferences` so brightness, zone toggles, and theme survive app restarts
- Add selectable color themes for party mode (Spectrum, Fire, Ocean, Monochrome, Rainbow, Strobe)
- Hide glyph-specific UI (zone toggles, brightness slider) on non-Nothing devices

## Capabilities

### New Capabilities
- `party-themes`: Multiple selectable color mapping strategies for the front-screen party mode visualization
- `settings-persistence`: Persist user settings (brightness, zone toggles, selected theme) across app restarts using SharedPreferences

### Modified Capabilities
- `glyph-visualizer`: Guard initialization so non-Nothing devices skip glyph setup without crashing
- `front-screen-viz`: Support multiple color themes driven by the selected party theme setting

## Impact

- **Broader device support**: App becomes installable and useful on any Android 14+ phone (party mode only for non-Nothing devices)
- **Dependencies**: `SharedPreferences` (Android built-in, no new deps)
- **No breaking changes**: Existing behavior on Nothing Phone (3a) is preserved
