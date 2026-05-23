## Why

The app's whole pitch — "hold your phone face-down and watch the back light up with the music" — only works on Nothing phones today, because it depends on the glyph LEDs. Almost every Android phone has a rear camera flash (torch). Driving that torch from the audio pipeline delivers the same core experience to every phone, with **no new permission** (the torch APIs don't require `CAMERA`). It's effectively a one-LED glyph, and it slots straight into the persistent-output model from `capture-lifecycle-rework`.

## What Changes

- Add a new rear output, **Flash**: a `FlashController` that modulates the camera torch from the audio analysis (bass level / beat), parallel to the existing `GlyphController`.
- Available **wherever the device has a flash unit** — additive on Nothing phones (glyphs + flash), and on non-Nothing phones it becomes the persistent output that makes the **Lights** tab appear.
- Use `turnOnTorchWithStrengthLevel()` for **smooth brightness** on hardware that reports more than one strength level; fall back to threshold **on/off** where only a single level is supported. Detect capability via `CameraManager` characteristics.
- Add **Flash enable + intensity** settings, persisted, surfaced as a section on the Lights tab.
- **Rate-limit** torch updates (~20–30 Hz cap) and keep average output modest to limit thermal/battery impact.
- Integrate as a **persistent capture consumer** per `capture-lifecycle-rework` — no new lifecycle code; Flash renders while the persistent session runs, gated by its enable toggle.
- No `CAMERA` permission added.

## Capabilities

### New Capabilities
- `flash-visualizer`: The camera-flash output — capability detection, audio-reactive torch brightness with smooth/on-off rendering modes, intensity control, and thermal/rate safeguards.

### Modified Capabilities
- `settings-persistence`: Persisted settings gain Flash enable + intensity (added as a new requirement, not a change to existing behavior).

## Impact

- **Depends on** `capture-lifecycle-rework`: relies on the Lights tab and the persistent-consumer model. This change should land after it.
- **Code**: new `FlashController` (and a small capability probe) in a `flash/` package alongside `glyph/`; `GlyphSenseService` gains a flash output branch that consumes the same analysis stream; `VisualizerSettings` + `SettingsStore` gain `flashEnabled` / `flashIntensity`; Lights-tab UI gains a flash section.
- **Hardware/SDK**: `CameraManager.setTorchMode` / `turnOnTorchWithStrengthLevel` (API 33+; guaranteed by minSdk 34), `CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL` for capability + smooth-vs-onoff decision. No new permission.
- **Out of scope**: flash patterns/sequences, multi-camera flashes, a steady-torch "beacon" variant (possible follow-on), and any change to the audio pipeline.
