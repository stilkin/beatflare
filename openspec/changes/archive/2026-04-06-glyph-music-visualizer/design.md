## Context

GlyphSense is a new Android app for the Nothing Phone (3a). The phone has 36 addressable glyph LEDs on the back in 3 zones. The official Glyph SDK (AAR) provides per-LED brightness control via `setFrameColors(IntArray)`. No existing app uses microphone input for live ambient audio visualization on the glyphs — all current solutions react to on-device media playback only.

The user's primary scenario is festivals/concerts: phone in pocket or on table, glyphs pulsing to live music.

## Goals / Non-Goals

**Goals:**
- Real-time audio visualization on glyph LEDs using microphone input
- Adaptive to extreme volume levels (festivals can hit 100+ dB)
- Battery-conscious by default (screen off, glyphs only)
- Optional "party mode" with front-screen color visualization
- Quick access via home screen widget

**Non-Goals:**
- Reacting to on-device media playback (Visualizer API) — microphone only for now
- Configurable frequency-to-zone mapping (sensible defaults first)
- Supporting other Nothing Phone models (3a only initially, though the architecture shouldn't preclude it)
- Music identification or recording

## Decisions

### 1. Audio pipeline: AudioRecord + manual FFT

**Choice**: `AudioRecord` with a manual FFT implementation (or lightweight library like TarsosDSP).

**Why not `Visualizer` API**: Visualizer attaches to audio output sessions, not the microphone. Our core use case is ambient mic capture at live events.

**Why not `MediaRecorder`**: MediaRecorder is designed for recording to file, not real-time streaming analysis.

**Parameters**:
- Sample rate: 44100 Hz (standard, widely supported)
- Channel: MONO (sufficient for frequency analysis, halves data)
- Encoding: PCM 16-bit
- Buffer size: ~2048 samples per FFT window (~46ms at 44.1kHz) — good balance between frequency resolution and latency

### 2. Frequency band mapping to LED zones

**Choice**: Split FFT output into 3 bands mapped to the 3 physical zones.

| Zone | LEDs | Frequency Band | Visualization |
|------|------|----------------|---------------|
| C (20 LEDs) | 0–19 | Full spectrum (20 bands) | Spectrum analyzer bar graph |
| A (11 LEDs) | 20–30 | Sub-bass + bass (20–250 Hz) | Bass energy level, fills bottom-up |
| B (5 LEDs) | 31–35 | Transients / beat detection | Lights up on detected beats |

**Alternatives considered**:
- All zones as one big spectrum (36 bands) — less visually distinct, harder to read
- Only amplitude (no FFT) — too simplistic, wastes the 3-zone layout

### 3. Adaptive volume scaling

**Choice**: Rolling peak normalization with exponential decay.

- Track peak amplitude over a sliding window (default: 5 seconds)
- Current amplitude expressed as percentage of rolling peak
- Peak decays exponentially (half-life ~3s) so the visualization recovers after sudden loud moments
- Prevents permanent max-out at loud festivals and dead visualization during quiet parts

**Alternative considered**: Fixed thresholds with sensitivity presets — less adaptive, requires user fiddling.

### 4. Architecture: Foreground service + bound Activity

```
┌─────────────────────────────────────────────────────┐
│                  GlyphSenseService                   │
│              (Foreground Service)                     │
│                                                       │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────┐ │
│  │ AudioCapture │──▶│  FFT + Band  │──▶│  Glyph   │ │
│  │  (AudioRecord)│   │  Splitter +  │   │  Driver  │ │
│  │              │   │  Normalizer  │   │(SDK calls)│ │
│  └──────────────┘   └──────┬───────┘   └──────────┘ │
│                            │                          │
│                            ▼                          │
│                    ┌──────────────┐                   │
│                    │ SharedFlow   │                   │
│                    │ (band data)  │                   │
│                    └──────┬───────┘                   │
│                           │                           │
└───────────────────────────┼───────────────────────────┘
                            │ bind
                            ▼
              ┌──────────────────────────┐
              │     MainActivity          │
              │   (Jetpack Compose)       │
              │                          │
              │  ┌────────────────────┐  │
              │  │ Front-screen viz   │  │
              │  │ (party mode)       │  │
              │  └────────────────────┘  │
              │  ┌────────────────────┐  │
              │  │ Controls / Status  │  │
              │  └────────────────────┘  │
              └──────────────────────────┘

              ┌──────────────────────────┐
              │     Widget               │
              │  (Start/Stop toggle)     │
              └──────────────────────────┘
```

**Why foreground service**: Android kills background audio processing. A foreground service with a persistent notification keeps the pipeline alive even when the screen is off — the primary use mode.

**Why SharedFlow**: The service produces band data continuously. SharedFlow lets the Activity (when visible) observe the same data stream for the front-screen visualization without coupling them.

### 5. Glyph SDK integration

**Choice**: Use the official SDK's `GlyphManager` lifecycle with direct `setFrameColors(IntArray)` for per-LED brightness.

- `init()` in service `onCreate`, `unInit()` in `onDestroy`
- `openSession()` / `closeSession()` on start/stop
- Build IntArray(36) each frame, mapping band energies to brightness values (0–4095 range, TBD from empirical testing)
- Target update rate: 20–30 fps (constrained by SDK, needs testing)

### 6. Front-screen visualization (party mode)

**Choice**: Full-screen Compose Canvas with color wash that shifts hue based on dominant frequency and pulses opacity with amplitude.

Simple and performant — no complex particle systems. Just a satisfying color pulse. Activated by a button in the main UI, off by default to save battery.

## Spike Findings (validated)

- **Refresh rate**: ~2291 fps achievable — non-blocking IPC, no throttling. Target 30 fps is trivially met.
- **Brightness range**: 0–4095 (12-bit). Value of 255 produces visibly dim LEDs.
- **Per-LED control**: `GlyphManager.setFrameColors(IntArray(36))` works on Phone (3a).
- **LED mapping confirmed**: C=0-19 (diagonal strip through top-left), A=20-30 (vertical strip), B=31-35 (small cluster).

## Risks / Trade-offs

- **[Glyph SDK refresh rate]** → RESOLVED: 2291 fps measured, no constraint.
- **[Battery drain]** → Continuous mic + LED updates. Mitigated by screen-off default, and we can offer a lower refresh rate option.
- **[Mic clipping at high SPL]** → Phone mic has hardware limits. The rolling normalizer handles this at the software level, but severely clipped audio means less frequency discrimination. Acceptable for the use case — it's a light show, not audio analysis.
- **[Foreground service notification]** → Android requires a persistent notification. Slight UX annoyance but unavoidable. We'll make it useful (show status, quick stop action).
