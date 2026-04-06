## 1. Non-Nothing Device Support

- [ ] 1.1 Add `isNothingDevice()` helper to GlyphController that returns true if any `Common.isXXXXX()` matches
- [ ] 1.2 Guard glyph init in GlyphSenseService.startPipeline() — skip controller.init/setFrameColors when not a Nothing device
- [ ] 1.3 Expose device type to the UI via a static flow or companion val on the service
- [ ] 1.4 Hide glyph-specific UI (brightness slider, zone toggles) on non-Nothing devices in MainActivity

## 2. Settings Persistence

- [ ] 2.1 Create SettingsStore class wrapping SharedPreferences — load() returns VisualizerSettings, save(settings) writes all fields
- [ ] 2.2 Add partyTheme field to VisualizerSettings (PartyTheme enum, default SPECTRUM)
- [ ] 2.3 Load persisted settings in GlyphSenseService companion init block (or on first access)
- [ ] 2.4 Call SettingsStore.save() inside GlyphSenseService.updateSettings() on every change

## 3. Party Themes

- [ ] 3.1 Create PartyTheme enum with entries: SPECTRUM, FIRE, OCEAN, MONOCHROME, RAINBOW, STROBE
- [ ] 3.2 Implement each theme as a color derivation function: (AudioAnalysis, beatFlash: Int) -> Color
- [ ] 3.3 Update PartyOverlay to read the selected theme from settings and delegate color calculation
- [ ] 3.4 Add theme selector (dropdown or chips) to the settings panel in MainActivity

## 4. Verification

- [ ] 4.1 Build and verify on Nothing Phone (3a) — all existing functionality preserved
- [ ] 4.2 Verify settings survive app force-kill and relaunch
