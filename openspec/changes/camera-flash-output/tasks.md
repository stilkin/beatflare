## 1. Capability detection

- [x] 1.1 Probe `CameraManager` for a flash-capable camera (`FLASH_INFO_AVAILABLE`); expose `hasFlash: Boolean` (`FlashController.available` + companion `isAvailable()`)
- [x] 1.2 Read `FLASH_INFO_STRENGTH_MAXIMUM_LEVEL`; derive render mode (smooth if `> 1`, else on/off)

## 2. FlashController

- [x] 2.1 Create `flash/FlashController.kt` with `render(analysis, intensity)` and `stop()` (always turns torch off). Note: `render` takes the analysis + user intensity so the bass/beat mapping (with its decay state) stays cohesive in the controller, rather than a bare `render(intensity: Float)`.
- [x] 2.2 Smooth mode: map intensity 0..1 → `1..maxLevel`, call `turnOnTorchWithStrengthLevel`
- [x] 2.3 On/off mode: threshold intensity with hysteresis, call `setTorchMode`
- [x] 2.4 Rate-limit to ~30 Hz and skip writes when the target state is unchanged
- [x] 2.5 Wrap torch calls in try/catch for `CameraAccessException`; back off and retry next frame, never crash

## 3. Settings

- [x] 3.1 Add `flashEnabled: Boolean` (default `false`) and `flashIntensity: Float` (default `0.8f`) to `VisualizerSettings`
- [x] 3.2 Add read/write keys to `SettingsStore`; absent keys fall back to defaults (no migration error)

## 4. Service integration

- [x] 4.1 Instantiate `FlashController` in `GlyphSenseService` when `hasFlash` (independent of `isNothingDevice`)
- [x] 4.2 On each analysis frame, when `flashEnabled`, compute intensity (`max(bass, decaying beat boost) * flashIntensity`) and call `render`. Two deliberate deviations from the original wording, per confirmed decisions: (a) **no quiet floor** — the torch rests off in silence to save battery; (b) gated on `flashEnabled` only (fires whenever the mic captures, mirroring how glyphs gate on `glyphsOutputEnabled`), not on a distinct "persistent session" check.
- [x] 4.3 Turn the torch off on flash-disable (loop `else` branch), session stop (`stopPipeline`), and `onDestroy`
- [x] 4.4 Confirmed: Flash needs no new lifecycle code — it rides the existing pipeline (which runs whenever a capture consumer holds the mic)

## 5. Lights tab UI

- [x] 5.1 Add a Flash section to the Lights tab: enable toggle + intensity slider, shown only when `hasFlash`
- [x] 5.2 On Nothing devices, place the Flash section alongside the glyph zones/brightness (after the glyph cards, before Start/Stop)
- [x] 5.3 On non-Nothing devices with a flash, the Lights tab appears: `showLights = isNothingDevice || hasFlash` (glyph cards gate on `isNothingDevice`, Flash card on `hasFlash`)

## 6. Verify & validate

- [x] 6.1 `./gradlew ktlintCheck :app:assembleDebug` passes
- [x] 6.2 On a multi-level-torch device: torch dims/brightens smoothly with the music (verified on Phone 3a)
- [x] 6.3 On a single-level-torch device: torch strobes on/off to the beat — **N/A**: no single-level-torch device in the test group; on/off path implemented but not field-tested
- [x] 6.4 Disable Flash mid-session → torch turns off; stop session → torch off; kill app → torch off (all verified on Phone 3a)
- [x] 6.5 Confirmed the app requests no `CAMERA` permission — merged debug manifest lists only RECORD_AUDIO, FOREGROUND_SERVICE(_MICROPHONE), POST_NOTIFICATIONS, the Nothing ENABLE perm, and the dynamic-receiver perm; torch is driven via `CameraManager` only
- [x] 6.6 Settings persist across restart (verified on Phone 3a)
- [x] 6.7 `openspec validate camera-flash-output` passes
