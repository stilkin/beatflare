## Context

Six themes exist today: `SPECTRUM`, `FIRE`, `OCEAN`, `MONOCHROME`, `RAINBOW`, `STROBE`. Each is an enum entry with a `deriveColor(analysis, beatFlash)` method that maps audio state to a Color. The pipeline fires ~22 Hz (one FFT per 2048-sample buffer at 44.1 kHz).

Three real-world problems surfaced:

1. **Strobe** returns `Color.Black` unless `beatFlash > 0`. Weak audio → no beats → black screen.
2. **Spectrum** picks `analysis.spectrum.indices.maxByOrNull { ... }` and maps that index to a hue. The dominant bin jumps erratically frame-to-frame, so the screen flickers between unrelated colors. **Rainbow** uses a smooth time-cycle, so the two themes overlap perceptually (both sweep through hues) but Spectrum reads as the messier version of Rainbow.
3. All themes have `lightness = base + bassLevel * something`. When `bassLevel ≈ 0` (quiet passage), `lightness ≈ base` (very dark) — screen looks frozen.

## Goals / Non-Goals

**Goals:**
- Strobe always shows *something* visible (transients flash, otherwise gentle bass pulse).
- Spectrum and Rainbow are perceptually distinct.
- All themes have a minimum visible motion floor — no "frozen black" state.
- Two additional motion themes (Breathe, Sweep) for variety.

**Non-Goals:**
- Refactoring `PartyTheme` into a `(colorSource × motion)` composition. Tempting but premature — adds abstraction before we know which axes users actually want to mix. Revisit if customization demand grows beyond a handful of themes.
- Touching the audio pipeline. Beat detector, normalizer, FFT, band splitter are all out of scope.
- Per-theme settings. Themes stay parameter-free for now; the next proposal handles custom mono color and emoji overlay.

## Decisions

### 1. Quiet-state fallback (the cross-cutting fix)

Add a baseline pulse that runs in every theme: a slow sine wave (period ~2 s) with amplitude scaled by `(1 - bassLevel)` so it fades out when real audio takes over, and fades in when audio goes quiet. The pulse drives a low-level brightness floor (~5–10%) so the screen always has gentle motion.

This is implemented as a helper inside `PartyTheme` (the enum gets a private `quietPulse(time)` method or similar), used by each theme to ensure its `lightness` calculation has a floor. Implementation detail: themes need access to a monotonic time source; pass `nowMs: Long` as a third parameter to `deriveColor`.

### 2. Strobe rework

```
if transient detected (= beat OR bass-peak-above-floor):
  flash white (high contrast)
else:
  pulse a dim white (5-15%) driven by bass + quiet baseline
```

"Transient detected" reuses `analysis.beat` plus a synthetic transient check based on bass level vs. its floor — if `bassLevel > 0.6f` since last frame, treat it as a strobe trigger even if the beat detector didn't fire. This is *not* a new beat detection algorithm; it's a "is something loud right now" check that complements the beat detector.

### 3. Spectrum rework

Replace "dominant bin → hue" with: compute a weighted-average frequency centroid across the 20 bands and map *that* to hue. Low frequencies = warm hues (red/orange), high = cool (blue/violet). This gives a smooth color that "tracks the music" instead of jumping around. Use bass for brightness as before, plus the quiet-state baseline.

### 4. Rainbow rework

Keep the time-driven hue cycle (currently `hueOffset += 2f per frame`). Change brightness driver from `0.10f + bassLevel * 0.50f` to `0.25f + bassLevel * 0.65f` so audio modulation is more visible. Remove the `hueOffset = 0f on beat` reset — it's a jarring visual snap that doesn't add value.

### 5. Two new themes

- **Breathe**: fixed hue (default 280° = purple), brightness = `0.15 + 0.35 * sin(time) + 0.40 * bassLevel`. Calm, meditative — for ambient music or for "I'm carrying my phone in my pocket and want a gentle indicator."
- **Sweep**: hue rotates linearly with time, but unlike Rainbow it's a slow rotation through a *narrow* hue range (e.g., 200–320° = blue → magenta). Bass modulates saturation. Reads more like a "color washing back and forth" than Rainbow's full-spectrum cycle.

### 6. Stateful themes

`Rainbow` already holds a `hueOffset` field. `Breathe`, `Sweep`, and the quiet-state pulse also need time. Rather than each theme owning its own clock state, pass `nowMs` from the caller (`PartyOverlay`) into `deriveColor`. `Rainbow`'s `hueOffset` can be computed from `nowMs` directly, eliminating the field.

This is a small API change to the enum's abstract method:

```kotlin
abstract fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color
```

`PartyOverlay` already runs a `LaunchedEffect`; it grabs `System.currentTimeMillis()` per frame and passes it in.

## Risks / Trade-offs

- **Quiet-state pulse may be distracting** in noisy environments where audio is fine. Mitigated by the `(1 - bassLevel)` envelope — at festivals where bass is high, the pulse stays near zero.
- **`nowMs` parameter is a breaking API change** to `deriveColor`. Internal-only API, no callers outside this module — safe to change.
- **No unit tests for "looks alive"**. Acceptable; visual verification on device is the test.
