# widget-toggle Specification

## Purpose
The home-screen widget that launches the Beacon overlay with a single tap using
the user's persisted Beacon settings.
## Requirements
### Requirement: Home screen widget
The system SHALL provide an Android home screen widget that launches the Beacon overlay with a single tap. Tapping the widget SHALL open the app's main activity directly into the Beacon overlay, rendered with the user's currently persisted Beacon settings (background hue, centred text, text colour, and react-to-sound). The widget SHALL be a stateless launcher: it SHALL NOT start or stop the persistent visualizer service and SHALL NOT reflect a running/stopped state.

#### Scenario: Widget tap launches the Beacon
- **WHEN** the user taps the widget from the home screen
- **THEN** the app opens directly into the Beacon overlay using the current Beacon settings

#### Scenario: Beacon uses last-used settings
- **WHEN** the user has previously configured the Beacon hue, text, text colour, or react-to-sound and taps the widget
- **THEN** the Beacon overlay renders with those persisted settings

#### Scenario: Widget tap while the app is already open
- **WHEN** the app is already running (foreground or background) and the user taps the widget
- **THEN** the existing activity is brought forward showing the Beacon overlay, without creating a duplicate activity

#### Scenario: Dismissal happens on the screen
- **WHEN** the Beacon overlay is showing after a widget launch
- **THEN** the user dismisses it on the screen (back gesture or tap), and the widget plays no part in dismissal

