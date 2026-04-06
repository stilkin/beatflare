## ADDED Requirements

### Requirement: Home screen widget
The system SHALL provide an Android home screen widget that allows the user to start and stop the visualizer service with a single tap.

#### Scenario: Widget tap starts visualizer
- **WHEN** the visualizer is not running and the user taps the widget
- **THEN** the visualizer foreground service starts and the widget updates to show "running" state

#### Scenario: Widget tap stops visualizer
- **WHEN** the visualizer is running and the user taps the widget
- **THEN** the visualizer foreground service stops, glyph LEDs are turned off, and the widget updates to show "stopped" state

### Requirement: Widget reflects current state
The system SHALL update the widget appearance to reflect whether the visualizer service is currently running or stopped.

#### Scenario: Service started from app
- **WHEN** the visualizer is started from the main app UI
- **THEN** the widget updates to show "running" state

#### Scenario: Service stopped from notification
- **WHEN** the visualizer is stopped via the foreground service notification
- **THEN** the widget updates to show "stopped" state
