# front-screen-viz Specification

## Purpose
Full-screen, audio-reactive color visualization on the phone's front screen — the
"Show" overlay (formerly "party mode"): a theme-driven color wash, an optional
centered text/emoji, and the rules for launching and dismissing the overlay. The
overlay's microphone lifetime is owned by the capture-lifecycle consumer model.
## Requirements
### Requirement: Full-screen color visualization
The system SHALL provide a full-screen color visualization mode ("Show") that fills the display with colors that shift and pulse in sync with the audio analysis data.

#### Scenario: Show activated
- **WHEN** the user launches the Show overlay (from the Show tab, or by tapping the live monitor on the Lights tab while a session is running)
- **THEN** the screen fills with a color wash driven by the selected [PartyTheme]

#### Scenario: Show deactivated
- **WHEN** the user taps the overlay to exit, or presses system back
- **THEN** the overlay closes and the tabbed main view is restored; capture continues only if another consumer (such as a persistent session) still needs it, and is released if the overlay was the sole consumer

### Requirement: Screen-off glyph operation
The system SHALL continue driving the glyph LEDs when the screen is off, as long as the foreground service is running.

#### Scenario: Screen turned off during visualization
- **WHEN** the user turns off the screen while the visualizer is running
- **THEN** the glyph LEDs continue to visualize audio from the microphone

### Requirement: System back dismisses the overlay
The system back gesture or button, when invoked while the party overlay is visible, SHALL dismiss the overlay and return to the tabbed main view without finishing the activity.

#### Scenario: Back from overlay
- **WHEN** the party overlay is visible and the user presses back
- **THEN** the overlay closes and the previously selected tab is restored; capture is released if the overlay was the sole consumer, otherwise it continues

### Requirement: Show is off by default
The system SHALL start with the Show overlay not visible. The front screen SHALL show the tabbed controls UI by default to conserve battery.

#### Scenario: App launched
- **WHEN** the app is first opened
- **THEN** the screen shows the tabbed controls UI, not the full-screen visualization

### Requirement: Show overlay has no text
The Show overlay SHALL render only the color wash from the selected theme. It SHALL NOT render the find-me overlay text or any other user-configured text. (Beacon overlay owns the text-overlay feature.)

#### Scenario: No text on Show
- **WHEN** the user has configured beacon text and the Show overlay opens
- **THEN** only the color wash is rendered, with the "Tap to exit" hint at the bottom

### Requirement: Re-launch Show overlay from the running visualizer
While a persistent session is running, the system SHALL provide a way to re-open the Show overlay after the user has dismissed it, without stopping the session.

#### Scenario: Tap the live monitor to re-launch
- **WHEN** a persistent session is running and the user taps the spectrum monitor card on the Lights tab
- **THEN** the Show overlay re-opens with the session still running

