## Why

Existing Nothing Phone glyph visualizers only react to on-device media playback. There's no app that uses the microphone to visualize live ambient audio — the exact thing you'd want at a concert or festival. The Phone (3a) has 36 individually addressable LEDs across 3 zones, which is enough for a meaningful real-time visualization. The Glyph SDK now supports per-LED brightness control, making this feasible.

## What Changes

- New Android application (GlyphSense) built from scratch
- Real-time microphone audio capture via Android's `AudioRecord` API
- FFT-based frequency analysis mapped to 3 glyph LED zones (spectrum, bass, beats)
- Adaptive volume scaling with rolling peak normalization to handle extreme SPL at festivals
- Foreground service to keep audio capture and LED control alive
- Optional full-screen front-of-phone color visualization ("party mode")
- Home screen widget for quick start/stop toggle

## Capabilities

### New Capabilities
- `audio-capture`: Microphone input via AudioRecord, continuous FFT processing, rolling peak normalization for adaptive scaling
- `glyph-visualizer`: Maps frequency bands to 36 LEDs across 3 zones (C=spectrum, A=bass, B=beats), drives Glyph SDK at target refresh rate
- `front-screen-viz`: Full-screen color wash/pulse synced to audio, activated via "party mode" toggle (stretch goal)
- `widget-toggle`: Home screen widget to start/stop the visualizer service (stretch goal)

### Modified Capabilities
<!-- None - this is a new project -->

## Impact

- **New dependencies**: Nothing Glyph/Matrix SDK 2.0 AAR, Android AudioRecord API, Jetpack Compose
- **Permissions required**: `RECORD_AUDIO`, `FOREGROUND_SERVICE`, `com.nothing.ketchum.permission.ENABLE`
- **Device constraint**: Nothing Phone devices running Android 14+ only
- **Battery**: Continuous mic capture + LED updates will drain battery; mitigated by screen-off default mode and configurable refresh rate
- **Unknown risk**: Glyph SDK max refresh rate is undocumented — may limit visualization smoothness. Needs empirical testing early.
