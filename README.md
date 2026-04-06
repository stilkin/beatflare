# BeatFlare

A live music visualizer for the **Nothing Phone (3a)** that drives the glyph LEDs from microphone audio. Designed for concerts and festivals -- hold your phone face-down and watch the back light up in sync with the music.

## What it does

BeatFlare captures audio from the microphone, runs real-time FFT analysis, and maps the frequency spectrum onto the 36 glyph LEDs:

| Zone | LEDs | Visualization |
|------|------|---------------|
| **C** (20 LEDs) | Long diagonal strip | Frequency spectrum analyzer |
| **A** (11 LEDs) | Vertical strip | Bass VU meter (fills bottom-up) |
| **B** (5 LEDs) | Small cluster | Beat detection flash |

The visualizer runs as a foreground service, so it keeps working when the screen is off -- ideal for saving battery at a festival while your phone's back pulses to the music.

### Features

- **Microphone-based**: Captures live ambient audio, not just on-device playback
- **Adaptive normalization**: Automatically adjusts to any volume level (quiet room to festival stage) using log-scale rolling peak normalization with noise floor tracking
- **Per-LED brightness control**: 12-bit (0-4095) brightness per LED via the Glyph SDK
- **Foreground service**: Survives screen lock and app backgrounding
- **Brightness slider**: Adjust overall LED intensity
- **Zone toggles**: Enable/disable spectrum, bass, and beat zones independently
- **Party mode**: Full-screen front-of-phone color visualization synced to audio (hue shifts with frequency, brightness pulses with bass, flashes on beats)
- **Notification control**: Start/stop from the persistent notification

## Requirements

- Nothing Phone (3a) or (3a) Pro
- Android 14+ (API 34+)
- Glyph debug mode enabled:
  ```bash
  adb shell settings put global nt_glyph_interface_debug_enable 1
  ```
  (expires after 48 hours, re-run as needed)

## Building

```bash
./gradlew :app:assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

Install with:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Tech stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Audio**: `AudioRecord` (44.1 kHz, mono, PCM 16-bit)
- **FFT**: Custom Cooley-Tukey radix-2 implementation with Hann windowing
- **Glyph SDK**: Nothing Glyph/Matrix SDK 2.0 (`setFrameColors(IntArray)`)
- **Architecture**: Foreground service owns the pipeline; Activity observes via `StateFlow`/`SharedFlow`

## Project structure

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
    GlyphDriver.kt          Maps AudioAnalysis -> IntArray(36) LED values
  model/
    VisualizerSettings.kt   Runtime settings (brightness, zone toggles)
  service/
    BeatFlareService.kt    Foreground service owning the pipeline
  ui/
    PartyOverlay.kt          Full-screen color visualization
  MainActivity.kt            Main UI
```

## Spike findings

During development we validated on actual hardware:

| Measurement | Result |
|-------------|--------|
| SDK refresh rate | ~2291 fps (non-blocking IPC) |
| Brightness range | 0-4095 (12-bit) |
| LED count (Phone 3a) | 36 (C:20, A:11, B:5) |
| Audio buffer latency | ~46ms (2048 samples @ 44.1 kHz) |

## License

[PolyForm Noncommercial 1.0.0](LICENSE) -- free for personal and non-commercial use.
