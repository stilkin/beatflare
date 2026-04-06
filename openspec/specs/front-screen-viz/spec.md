## ADDED Requirements

### Requirement: Full-screen color visualization
The system SHALL provide a full-screen color visualization mode ("party mode") that fills the display with colors that shift and pulse in sync with the audio analysis data.

#### Scenario: Party mode activated
- **WHEN** the user taps the party mode button
- **THEN** the screen fills with a color wash that shifts hue based on the dominant frequency band and pulses brightness with the overall amplitude

#### Scenario: Party mode deactivated
- **WHEN** the user taps the party mode button again or presses back
- **THEN** the screen returns to the default controls view

### Requirement: Party mode is off by default
The system SHALL start with party mode disabled. The front screen SHALL show minimal controls/status by default to conserve battery.

#### Scenario: App launched
- **WHEN** the app is first opened
- **THEN** the screen shows the controls UI, not the full-screen visualization

### Requirement: Screen-off glyph operation
The system SHALL continue driving the glyph LEDs when the screen is off, as long as the foreground service is running.

#### Scenario: Screen turned off during visualization
- **WHEN** the user turns off the screen while the visualizer is running
- **THEN** the glyph LEDs continue to visualize audio from the microphone
