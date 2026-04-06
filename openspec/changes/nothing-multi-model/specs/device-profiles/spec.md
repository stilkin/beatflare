## ADDED Requirements

### Requirement: Automatic device detection and profile selection
The system SHALL detect the Nothing Phone model at runtime and select the appropriate LED zone profile automatically. No user configuration SHALL be required.

#### Scenario: Phone (3a) detected
- **WHEN** the app runs on a Nothing Phone (3a)
- **THEN** the 36-LED profile is used with zones C(20), A(11), B(5)

#### Scenario: Phone (4a) detected
- **WHEN** the app runs on a Nothing Phone (4a)
- **THEN** the 6-LED profile is used with all LEDs as spectrum, no bass or beat zones

#### Scenario: Phone (1) detected
- **WHEN** the app runs on a Nothing Phone (1)
- **THEN** the 15-LED profile is used with D1 as spectrum, C1-C4 as bass, A1+B1+E1 as beat

### Requirement: Graceful degradation for small LED counts
The system SHALL adapt visualization to the available LED count. Models with fewer than 10 LEDs SHALL use all LEDs for spectrum only, with no separate bass or beat zones.

#### Scenario: All-spectrum mode on Phone (4a)
- **WHEN** the Phone (4a) profile is active (6 LEDs)
- **THEN** all 6 LEDs show spectrum analysis, bass and beat visualization are disabled

### Requirement: Configurable spectrum band count
The audio band splitter SHALL produce a number of spectrum sub-bands matching the spectrum zone LED count of the active device profile, rather than a hardcoded 20.

#### Scenario: 16-band spectrum on Phone (2)
- **WHEN** the Phone (2) profile is active with 16 spectrum LEDs
- **THEN** the band splitter produces 16 log-spaced spectrum sub-bands

## MODIFIED Requirements

### Requirement: LED zone mapping
The system SHALL map audio frequency bands to the active device profile's zone configuration instead of the hardcoded Phone (3a) layout. Zone assignments (which indices are spectrum, bass, beat) SHALL come from the device profile.

#### Scenario: Spectrum displayed on device-specific zone
- **WHEN** FFT produces N sub-band energy values (matching spectrum zone LED count)
- **THEN** each LED in the spectrum zone is set to brightness proportional to its corresponding sub-band energy
