## MODIFIED Requirements

### Requirement: Full-screen color visualization
The system SHALL provide a full-screen color visualization mode ("party mode") that fills the display with colors that shift and pulse in sync with the audio analysis data.

#### Scenario: Party mode activated
- **WHEN** the user starts the visualizer with the Party output enabled (or, on a non-Nothing device, simply taps Start)
- **THEN** the screen fills with a color wash driven by the selected [PartyTheme]

#### Scenario: Party mode deactivated
- **WHEN** the user taps the overlay to exit, or presses system back
- **THEN** the overlay closes and the tabbed main view is restored; capture continues only if another consumer (such as a persistent session) still needs it, and is released if the overlay was the sole consumer

### Requirement: System back dismisses the overlay
The system back gesture or button, when invoked while the party overlay is visible, SHALL dismiss the overlay and return to the tabbed main view without finishing the activity.

#### Scenario: Back from overlay
- **WHEN** the party overlay is visible and the user presses back
- **THEN** the overlay closes and the previously selected tab is restored; capture is released if the overlay was the sole consumer, otherwise it continues
