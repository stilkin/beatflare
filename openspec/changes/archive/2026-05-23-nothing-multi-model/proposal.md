## Why

GlyphSense currently hardcodes the Nothing Phone (3a) LED layout (36 LEDs, zones C/A/B). Other Nothing Phone models have different LED counts and zone configurations (Phone 1 has 15, Phone 2 has 33, Phone 2a has 26, Phone 4a has 6). Supporting them requires a device-specific zone mapping in GlyphDriver while keeping the audio pipeline and UI unchanged.

## What Changes

- Create a device-specific zone mapping configuration for each supported Nothing Phone model
- GlyphDriver selects the correct mapping at runtime based on `Common.isXXXXX()` detection
- Adapt spectrum/bass/beat rendering to work with varying zone sizes (e.g. Phone 4a has only 6 LEDs — all spectrum, no separate bass/beat zones)
- Update README with supported devices

## Capabilities

### New Capabilities
- `device-profiles`: Per-device LED zone configuration (count, index ranges, zone assignments) for all Nothing Phone models

### Modified Capabilities
- `glyph-visualizer`: Use device profile to determine LED count and zone mapping instead of hardcoded Phone (3a) layout

## Impact

- **Supported devices**: Phone (1), Phone (2), Phone (2a), Phone (2a) Plus, Phone (3a) / (3a) Pro, Phone (4a)
- **GlyphDriver changes**: Zone constants become dynamic; render methods adapt to variable zone sizes
- **GlyphController changes**: Minor — already handles device detection, just needs to expose the detected model
- **Testing**: Ideally needs physical devices or at minimum ADB-connected emulation. Can be structurally validated on Phone (3a) only.
- **Risk**: Without physical access to other models, zone index mappings are derived from SDK README documentation only — may need community feedback to validate
