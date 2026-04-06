## 1. Non-Nothing Device Support

- [x] 1.1 Add `isNothingDevice()` helper to GlyphController that returns true if any `Common.isXXXXX()` matches
- [x] 1.2 Guard glyph init in GlyphSenseService.startPipeline() — skip controller.init/setFrameColors when not a Nothing device
- [x] 1.3 Expose device type to the UI via a static flow or companion val on the service
- [x] 1.4 Hide glyph-specific UI (brightness slider, zone toggles) on non-Nothing devices in MainActivity

## 2. Settings Persistence

- [x] 2.1 Create SettingsStore class wrapping SharedPreferences — load() returns VisualizerSettings, save(settings) writes all fields
- [x] 2.2 Add partyTheme field to VisualizerSettings (PartyTheme enum, default SPECTRUM)
- [x] 2.3 Load persisted settings in GlyphSenseService companion init block (or on first access)
- [x] 2.4 Call SettingsStore.save() inside GlyphSenseService.updateSettings() on every change

## 3. Party Themes

- [x] 3.1 Create PartyTheme enum with entries: SPECTRUM, FIRE, OCEAN, MONOCHROME, RAINBOW, STROBE
- [x] 3.2 Implement each theme as a color derivation function: (AudioAnalysis, beatFlash: Int) -> Color
- [x] 3.3 Update PartyOverlay to read the selected theme from settings and delegate color calculation
- [x] 3.4 Add theme selector (dropdown or chips) to the settings panel in MainActivity

## 4. Verification

- [x] 4.1 Build and verify on Nothing Phone (3a) — all existing functionality preserved
- [x] 4.2 Verify settings survive app force-kill and relaunch
