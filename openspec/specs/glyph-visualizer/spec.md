# glyph-visualizer Specification

## Purpose
Driving the Nothing glyph LEDs from the audio analysis — frequency-band-to-zone
mapping, per-LED brightness control, Glyph SDK lifecycle, and target refresh rate.
## Requirements
### Requirement: LED zone mapping
The system SHALL map audio frequency bands to the active device profile's zone configuration instead of the hardcoded Phone (3a) layout. Zone assignments (which indices are spectrum, bass, beat) SHALL come from the device profile.

#### Scenario: Spectrum displayed on device-specific zone
- **WHEN** FFT produces N sub-band energy values (matching spectrum zone LED count)
- **THEN** each LED in the spectrum zone is set to brightness proportional to its corresponding sub-band energy

### Requirement: Per-LED brightness control
The system SHALL set individual LED brightness values using `setFrameColors(IntArray)` with an IntArray of 36 elements, one per LED.

#### Scenario: Frame update sent to SDK
- **WHEN** a new visualization frame is computed
- **THEN** an IntArray(36) with per-LED brightness values is passed to `setFrameColors`

### Requirement: Glyph SDK lifecycle management
The system SHALL initialize `GlyphManager` when the service starts, call `register()` and `openSession()` on service connect, and `closeSession()` + `unInit()` when the service stops.

#### Scenario: Service connects to glyph SDK
- **WHEN** the visualizer service starts
- **THEN** GlyphManager is initialized, registered for the detected Nothing device (the ID selected from the active device profile), and a session is opened

#### Scenario: Service disconnects from glyph SDK
- **WHEN** the visualizer service stops
- **THEN** the glyph session is closed, all LEDs are turned off, and GlyphManager is uninitialized

### Requirement: Target refresh rate
The system SHALL attempt to update the glyph LEDs at 20–30 fps. If the SDK cannot sustain this rate, the system SHALL gracefully degrade to the maximum achievable rate.

#### Scenario: SDK sustains target rate
- **WHEN** the SDK can process updates at 25 fps
- **THEN** the visualization runs at 25 fps

#### Scenario: SDK cannot sustain target rate
- **WHEN** the SDK throttles or drops frames beyond 15 fps
- **THEN** the system reduces its update rate to match and the visualization remains smooth at the lower rate

