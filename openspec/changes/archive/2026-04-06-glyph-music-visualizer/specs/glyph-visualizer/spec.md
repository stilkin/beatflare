## ADDED Requirements

### Requirement: LED zone mapping
The system SHALL map audio frequency bands to the Nothing Phone (3a) glyph zones as follows:
- Zone C (20 LEDs, indices 0–19): full spectrum analyzer (20 frequency sub-bands)
- Zone A (11 LEDs, indices 20–30): bass energy level (fills bottom-up)
- Zone B (5 LEDs, indices 31–35): beat detection indicator

#### Scenario: Spectrum displayed on zone C
- **WHEN** FFT produces 20 sub-band energy values
- **THEN** each of the 20 LEDs in zone C is set to a brightness proportional to its corresponding sub-band energy

#### Scenario: Bass displayed on zone A
- **WHEN** bass energy is at 60% of the rolling peak
- **THEN** the bottom 6–7 LEDs of zone A are lit (proportional fill)

#### Scenario: Beat displayed on zone B
- **WHEN** a beat is detected in the transient band
- **THEN** all 5 LEDs in zone B light up briefly, then decay

### Requirement: Per-LED brightness control
The system SHALL set individual LED brightness values using `setFrameColors(IntArray)` with an IntArray of 36 elements, one per LED.

#### Scenario: Frame update sent to SDK
- **WHEN** a new visualization frame is computed
- **THEN** an IntArray(36) with per-LED brightness values is passed to `setFrameColors`

### Requirement: Glyph SDK lifecycle management
The system SHALL initialize `GlyphManager` when the service starts, call `register()` and `openSession()` on service connect, and `closeSession()` + `unInit()` when the service stops.

#### Scenario: Service connects to glyph SDK
- **WHEN** the visualizer service starts
- **THEN** GlyphManager is initialized, registered for device 24111 (Phone 3a), and a session is opened

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
