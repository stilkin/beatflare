## ADDED Requirements

### Requirement: Camera flash as an audio-reactive rear output
The system SHALL provide a "Flash" output that drives the device's camera torch from the live audio analysis, so that the rear of the phone lights up in time with the music. The Flash output SHALL be available on any device that reports a flash-capable camera and SHALL run as a rear output of the persistent session.

#### Scenario: Flash reacts to audio
- **WHEN** the persistent session is running, Flash is enabled, and music is playing
- **THEN** the camera torch brightness (or on/off state) tracks the audio so the rear of the phone pulses with the beat

#### Scenario: Flash on a non-Nothing device
- **WHEN** the app runs on a non-Nothing device that has a camera flash
- **THEN** Flash is offered as the device's rear output and appears on the Lights tab

#### Scenario: Coexists with glyphs
- **WHEN** the app runs on a Nothing device that also has a camera flash
- **THEN** Flash is offered in addition to the glyph output and either may be enabled independently

### Requirement: No camera permission required
The system SHALL control the torch using `CameraManager` torch APIs that do not require the `CAMERA` permission, and SHALL NOT open a camera capture session for the flash output.

#### Scenario: Flash runs without CAMERA permission
- **WHEN** the app has not been granted the `CAMERA` permission
- **THEN** the Flash output still functions and the app does not request `CAMERA`

### Requirement: Brightness mode adapts to hardware capability
The system SHALL use smooth brightness modulation on hardware that supports more than one torch strength level, and SHALL fall back to on/off (threshold) operation on hardware that supports only a single level.

#### Scenario: Smooth modulation where supported
- **WHEN** the device reports a maximum torch strength level greater than 1
- **THEN** the torch brightness varies continuously with the audio intensity

#### Scenario: On/off fallback
- **WHEN** the device supports only a single torch level
- **THEN** the torch switches on and off against an intensity threshold (with hysteresis) instead of dimming

### Requirement: User controls Flash enable and intensity
The system SHALL let the user enable or disable the Flash output and set its intensity. Intensity SHALL scale the brightness ceiling of the audio mapping.

#### Scenario: Disable Flash
- **WHEN** the user disables Flash
- **THEN** the torch stays off even while the persistent session runs

#### Scenario: Adjust intensity
- **WHEN** the user lowers the Flash intensity
- **THEN** the peak torch brightness (or on-fraction in on/off mode) is reduced accordingly

### Requirement: Rate limiting and safe shutdown
The system SHALL cap torch updates to a bounded rate, SHALL skip redundant writes when the target state is unchanged, and SHALL turn the torch off whenever the Flash output stops, the session stops, or the service is destroyed. Torch errors SHALL be handled without crashing.

#### Scenario: Torch turns off on stop
- **WHEN** the persistent session stops or Flash is disabled while lit
- **THEN** the torch is turned off

#### Scenario: Torch error does not crash
- **WHEN** the torch is unavailable or throws a camera access error
- **THEN** the app backs off and continues without crashing, retrying on a later frame

#### Scenario: No update spam
- **WHEN** consecutive frames map to the same torch state
- **THEN** the system does not issue redundant torch writes
