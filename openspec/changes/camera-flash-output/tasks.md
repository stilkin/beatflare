## 1. Capability detection

- [ ] 1.1 Probe `CameraManager` for a flash-capable camera (`FLASH_INFO_AVAILABLE`); expose `hasFlash: Boolean`
- [ ] 1.2 Read `FLASH_INFO_STRENGTH_MAXIMUM_LEVEL`; derive render mode (smooth if `> 1`, else on/off)

## 2. FlashController

- [ ] 2.1 Create `flash/FlashController.kt` with `render(intensity: Float)` and `stop()` (always turns torch off)
- [ ] 2.2 Smooth mode: map intensity 0..1 → `1..maxLevel`, call `turnOnTorchWithStrengthLevel`
- [ ] 2.3 On/off mode: threshold intensity with hysteresis, call `setTorchMode`
- [ ] 2.4 Rate-limit to ~20–30 Hz and skip writes when the target state is unchanged
- [ ] 2.5 Wrap torch calls in try/catch for `CameraAccessException`; back off and retry next frame, never crash

## 3. Settings

- [ ] 3.1 Add `flashEnabled: Boolean` and `flashIntensity: Float` to `VisualizerSettings` with defaults
- [ ] 3.2 Add read/write keys to `SettingsStore`; absent keys fall back to defaults (no migration error)

## 4. Service integration

- [ ] 4.1 Instantiate `FlashController` in `GlyphSenseService` when `hasFlash`
- [ ] 4.2 On each analysis frame, when the persistent session runs and `flashEnabled`, compute intensity (bass + beat boost + quiet floor, scaled by `flashIntensity`) and call `render`
- [ ] 4.3 Turn the torch off on flash-disable, session stop, and `onDestroy`
- [ ] 4.4 Confirm Flash needs no new lifecycle code — it renders under the existing persistent consumer (depends on `capture-lifecycle-rework`)

## 5. Lights tab UI

- [ ] 5.1 Add a Flash section to the Lights tab: enable toggle + intensity slider, shown only when `hasFlash`
- [ ] 5.2 On Nothing devices, place the Flash section alongside the glyph zones/brightness
- [ ] 5.3 On non-Nothing devices with a flash, ensure the Lights tab appears (Flash is the rear output that surfaces it)

## 6. Verify & validate

- [ ] 6.1 `./gradlew ktlintCheck :app:assembleDebug` passes
- [ ] 6.2 On a multi-level-torch device: torch dims/brightens smoothly with the music
- [ ] 6.3 On a single-level-torch device: torch strobes on/off to the beat
- [ ] 6.4 Disable Flash mid-session → torch turns off; stop session → torch off; kill app → torch off
- [ ] 6.5 Confirm the app requests no `CAMERA` permission (inspect manifest + runtime)
- [ ] 6.6 Settings persist across restart
- [ ] 6.7 `openspec validate camera-flash-output` passes
