## ADDED Requirements

### Requirement: Optional centered character overlay
The party mode overlay SHALL support an optional user-configured character or short text (emoji, letter, or short combination) rendered centered over the color wash. When configured, the text SHALL be rendered large enough to read at a distance — sized to approximately half the shorter screen dimension — and SHALL include a soft drop-shadow for legibility against bright backgrounds.

#### Scenario: No overlay configured
- **WHEN** the user has not configured an overlay character
- **THEN** the party mode overlay shows only the color wash, with the "Tap to exit" hint

#### Scenario: Emoji overlay configured
- **WHEN** the user configures "❤️" as the overlay character and launches party mode
- **THEN** a large red heart is rendered centered over the color wash and remains legible across all themes

#### Scenario: Overlay persists
- **WHEN** the user sets an overlay character and restarts the app
- **THEN** the same character remains configured

#### Scenario: Multi-character overlay rotates to fit
- **WHEN** the user configures an overlay with more than one user-perceived character (e.g. "JD", "HI<3")
- **THEN** the text is rotated counter-clockwise so that the characters run along the screen's long axis, allowing each glyph to remain large

### Requirement: Output toggles drive what happens on Start
The system SHALL allow the user to independently enable or disable two output modes: **Glyphs** (Nothing devices only) and **Party mode** (front-screen color wash). Tapping Start SHALL activate the enabled outputs; the controls SHALL NOT permit both outputs to be disabled simultaneously.

#### Scenario: Both outputs enabled
- **WHEN** the user has both outputs enabled and taps Start
- **THEN** the glyph LEDs light up AND the party overlay opens automatically

#### Scenario: Party only
- **WHEN** the user has only Party mode enabled and taps Start
- **THEN** the party overlay opens AND the Glyph SDK session is not opened

#### Scenario: Glyphs only
- **WHEN** the user has only Glyphs enabled and taps Start
- **THEN** the glyph LEDs light up AND no party overlay is shown

#### Scenario: Auto-flip the last enabled toggle
- **WHEN** the user attempts to disable the last enabled output
- **THEN** the other output is automatically enabled so that at least one output is always active

#### Scenario: Non-Nothing device
- **WHEN** the app runs on a non-Nothing device
- **THEN** the Glyphs toggle is not shown and the Party output is implicit (always on)

### Requirement: Re-launch party overlay from the running visualizer
While the visualizer is running and the Party output is enabled, the system SHALL provide a way to re-open the party overlay after the user has dismissed it, without stopping the visualizer.

#### Scenario: Tap visualizer card to re-launch
- **WHEN** the visualizer is running, the Party output is enabled, and the user taps the spectrum visualizer card on the Play tab
- **THEN** the party overlay re-opens with the visualizer still running

### Requirement: System back dismisses the overlay
The system back gesture or button, when invoked while the party overlay is visible, SHALL dismiss the overlay and return to the tabbed main view without finishing the activity.

#### Scenario: Back from overlay
- **WHEN** the party overlay is visible and the user presses back
- **THEN** the overlay closes and the previously selected tab is restored; the visualizer continues running if it was running
