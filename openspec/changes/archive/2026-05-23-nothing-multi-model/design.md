## Context

GlyphDriver hardcodes the Phone (3a) layout: 36 LEDs, zones C(0-19), A(20-30), B(31-35). Other Nothing Phone models have different LED counts and zone structures. We need a device-profile abstraction that lets the driver adapt.

## Goals / Non-Goals

**Goals:**
- Support all Nothing Phone models: (1), (2), (2a), (2a) Plus, (3a)/(3a) Pro, (4a)
- Automatic device detection — no user configuration needed
- Graceful degradation for models with few LEDs (e.g. Phone 4a has only 6)

**Non-Goals:**
- Phone (3) dot matrix support (that's a different SDK: GlyphMatrixManager)
- Custom per-device visualization modes
- User-configurable zone assignments

## Decisions

### 1. Device profile data class

```
DeviceProfile(
    ledCount: Int,
    spectrumRange: IntRange,    // zone for spectrum analyzer
    bassRange: IntRange?,       // zone for bass VU (null if not enough LEDs)
    beatRange: IntRange?,       // zone for beat flash (null if not enough LEDs)
)
```

For devices with too few LEDs to split into 3 zones, all LEDs become spectrum.

### 2. Profile definitions

| Model | LEDs | Spectrum | Bass | Beat |
|-------|------|----------|------|------|
| Phone (1) | 15 | D1 (7-14, 8 LEDs) | C1-C4 (2-5, 4 LEDs) | A1+B1+E1 (0,1,6) |
| Phone (2) | 33 | C1 (3-18, 16 LEDs) | D1 (25-32, 8 LEDs) | A1+A2+B1+E1 (0-2,24) |
| Phone (2a/2a+) | 26 | C (0-23, 24 LEDs) | A (25, 1 LED) | B (24, 1 LED) |
| Phone (3a/3a Pro) | 36 | C (0-19, 20 LEDs) | A (20-30, 11 LEDs) | B (31-35, 5 LEDs) |
| Phone (4a) | 6 | A (0-5, 6 LEDs) | null | null |

### 3. GlyphDriver changes

- Constructor takes a `DeviceProfile` instead of hardcoded constants
- `renderSpectrum` adapts to variable-length spectrum zone
- `renderBass` skips if `bassRange` is null
- `renderBeat` skips if `beatRange` is null
- BandSplitter spectrum band count adapts to spectrum zone LED count

## Risks / Trade-offs

- **Untested on other devices**: Zone index mappings come from SDK README only. May have errors for models we can't physically test.
- **Phone (4a) with 6 LEDs**: Visualization will be minimal — just a 6-bar spectrum. Acceptable for an entry-level device.
- **BandSplitter coupling**: Currently hardcoded to 20 spectrum bands (matching Phone 3a zone C). Needs to become configurable based on profile.
