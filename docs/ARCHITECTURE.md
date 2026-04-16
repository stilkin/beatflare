# Architecture

Developer reference for BeatFlare internals.

## Building from Source

```bash
./gradlew :app:assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

Install with:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Lint

```bash
./gradlew ktlintCheck                        # Check
./gradlew ktlintFormat                       # Auto-fix
./gradlew ktlintCheck :app:assembleDebug     # Lint + build
```

## Tech Stack

| Concern | Library / Tool |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Min SDK | API 34 (Android 14+) |
| Audio | `AudioRecord` (44.1 kHz, mono, PCM 16-bit) |
| FFT | Custom Cooley-Tukey radix-2 implementation with Hann windowing |
| Glyph SDK | Nothing Glyph/Matrix SDK 2.0 (`setFrameColors(IntArray)`) |
| Architecture | Foreground service owns the pipeline; Activity observes via `StateFlow`/`SharedFlow` |

## Project Structure

```
app/src/main/java/be/pocito/glyphsense/
  audio/
    AudioCapture.kt          Mic capture via AudioRecord
    AudioAnalyzer.kt         Pipeline: buffer -> FFT -> bands -> normalize
    Fft.kt                   Cooley-Tukey radix-2 FFT
    BandSplitter.kt          FFT magnitudes -> bass/spectrum/transient bands
    BeatDetector.kt          Energy-based beat detection
    RollingPeakNormalizer.kt Adaptive normalization with noise floor
  glyph/
    GlyphController.kt      Wraps Nothing GlyphManager lifecycle
    GlyphDriver.kt          Maps AudioAnalysis -> LED values via DeviceProfile
  model/
    DeviceProfile.kt        Per-device LED zone configuration
    PartyTheme.kt           Color themes for party mode
    SettingsStore.kt        SharedPreferences persistence
    VisualizerSettings.kt   Runtime settings (brightness, zones, theme)
  service/
    GlyphSenseService.kt    Foreground service owning the pipeline
  ui/
    PartyOverlay.kt          Full-screen color visualization
  MainActivity.kt            Main UI
```

## Audio Pipeline

```
Microphone -> AudioRecord (44.1 kHz, mono, PCM 16-bit)
    -> 2048-sample buffer (~46ms latency)
    -> Hann window -> FFT (Cooley-Tukey radix-2)
    -> BandSplitter (bass / spectrum / transient bands)
    -> RollingPeakNormalizer (adaptive log-scale normalization)
    -> GlyphDriver (maps bands to LED brightness via DeviceProfile)
```

## Device Profiles

Each Nothing Phone model has a different LED count and zone layout. `DeviceProfile` defines per-device zone mappings (spectrum, bass, beat indices). The correct profile is detected at runtime via the Glyph SDK's `Common.isXXXXX()` methods.

Models with fewer LEDs (e.g. Phone 4a with 6) use all LEDs for spectrum only. Models with more LEDs (e.g. Phone 2 with 33) get dedicated bass and beat zones.

## Spike Findings

Validated on Nothing Phone (3a) hardware during initial development:

| Measurement | Result |
|---|---|
| SDK refresh rate | ~2291 fps (non-blocking IPC) |
| Brightness range | 0-4095 (12-bit) |
| LED count (Phone 3a) | 36 (C:20, A:11, B:5) |
| Audio buffer latency | ~46ms (2048 samples @ 44.1 kHz) |
