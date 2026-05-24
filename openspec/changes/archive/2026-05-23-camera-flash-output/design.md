## Context

Audio capture and analysis already run in `GlyphSenseService`, producing a per-frame `AudioAnalysis` (bass level, spectrum, beat). `GlyphController` consumes that to drive the 36 glyph LEDs. The camera torch is a single, coarse output — at best one LED with variable strength — so a flash output is a much simpler consumer of the same stream: map a 0..1 intensity to torch state each frame.

The nuance is entirely in the hardware API and its variability across devices, plus thermal/battery care. This change rides on `capture-lifecycle-rework`: Flash is a rear output rendered while the persistent session runs, so it needs **no new lifecycle code** — only a controller and a settings/UI surface on the Lights tab.

## Goals / Non-Goals

**Goals:**
- Bring the face-down "back lights up to the music" experience to any phone with a torch, with no new permission.
- Smooth brightness modulation where the hardware supports it, graceful on/off fallback where it doesn't.
- Bounded thermal/battery cost via a frame-rate cap and modest average output.
- Reuse the existing analysis stream and the persistent-consumer model unchanged.

**Non-Goals:**
- Flash patterns/sequences, color (torches are fixed white), or multiple flash units.
- A steady-torch "findable beacon" variant (possible later follow-on).
- Any audio-pipeline or analysis change.

## Decisions

### Decision 1: Drive the torch via `CameraManager`, not the Camera2 capture pipeline

Use `CameraManager.setTorchMode(id, on)` and, where supported, `turnOnTorchWithStrengthLevel(id, level)`. These control the torch **without opening a camera session and without the `CAMERA` permission** — critical for a clean Play Store listing and for not fighting other camera users. Find the flash-capable camera via `CameraCharacteristics.FLASH_INFO_AVAILABLE`.

*Alternative rejected:* a full Camera2 session to control flash — heavier, needs `CAMERA`, conflicts with other apps, no upside for torch-only use.

### Decision 2: Two render modes chosen by hardware capability

Read `FLASH_INFO_STRENGTH_MAXIMUM_LEVEL` (API 33+, guaranteed by minSdk 34):
- **`maxLevel > 1` → smooth mode:** map the frame intensity (0..1) to `1..maxLevel` and call `turnOnTorchWithStrengthLevel`. Quantize/threshold tiny values so we don't spam near-identical levels.
- **`maxLevel == 1` (or default level only) → on/off mode:** threshold the intensity (with a small hysteresis band) and call `setTorchMode`. This reads as a beat/bass strobe rather than a dimmer.

The controller exposes one `render(intensity: Float)` API; the mode is an internal detail decided once at init.

### Decision 3: Intensity mapping reuses the glyph mapping shape

Frame intensity = a bass-weighted level with a beat boost and a quiet-state floor, mirroring the glyph brightness curve so Flash "feels" like the glyphs. A user **Flash intensity** setting scales the ceiling. Keep the mapping in the controller, fed by the same `AudioAnalysis`.

### Decision 4: Rate-limit and fail safe

- Cap torch updates to ~20–30 Hz regardless of analysis rate; skip a write if the target state is unchanged (especially important for on/off mode and torch hardware wear).
- On any `CameraAccessException` (torch in use, transient error), log and back off rather than crash; retry on the next frame.
- Always turn the torch **off** when the flash output stops, the session stops, or the service is destroyed — a stuck-on torch is the worst failure mode.

### Decision 5: Flash is a rear output under the persistent session

Flash renders only while the persistent session is running (per `capture-lifecycle-rework`), gated by `flashEnabled`. It does not introduce its own capture lifecycle. On Nothing phones it coexists with glyphs; on other phones it is the sole persistent output and is what makes the Lights tab appear.

## Risks / Trade-offs

- **Device variability in strength support** → mode is chosen from `FLASH_INFO_STRENGTH_MAXIMUM_LEVEL`; on/off fallback guarantees *something* works everywhere with a torch. Don't promise smooth dimming in marketing copy.
- **Thermal / battery during long sets** → frame-rate cap + modest average + bass-driven (mostly-low) output; a stuck-on torch is prevented by the always-off-on-stop rule.
- **Torch contention with other apps / the user's own camera** → catch `CameraAccessException` and back off; Flash is best-effort, never fatal.
- **Hardware wear from rapid on/off toggling** in on/off mode → hysteresis + skip-if-unchanged + the rate cap keep toggle frequency bounded.
- **Perceived value of a single coarse LED** vs. 36 glyphs → accepted; it's the "minimal" rear output, and for findability/ambiance a pulsing rear torch is genuinely effective.

## Migration Plan

Additive feature; new persisted settings default to a sensible state (`flashEnabled = false` on Nothing where glyphs already exist; consider `true` on non-Nothing where it's the only rear output — decide at implementation). No data migration. Rollback = revert; absent settings fall back to defaults. Land after `capture-lifecycle-rework`.

## Open Questions

- Default `flashEnabled`: off everywhere, or on by default on non-Nothing devices (where it's the only rear output)?
- Should `flashIntensity` be a single ceiling, or also gate how much beat-boost contributes?
- Where exactly the flash section sits within the Lights tab relative to the glyph zones (Nothing phones show both).
