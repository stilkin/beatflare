## MODIFIED Requirements

### Requirement: LED zone mapping
The system SHALL map audio frequency bands to the active device profile's zone configuration instead of the hardcoded Phone (3a) layout. Zone assignments (which indices are spectrum, bass, beat) SHALL come from the device profile.

#### Scenario: Spectrum displayed on device-specific zone
- **WHEN** FFT produces N sub-band energy values (matching spectrum zone LED count)
- **THEN** each LED in the spectrum zone is set to brightness proportional to its corresponding sub-band energy
