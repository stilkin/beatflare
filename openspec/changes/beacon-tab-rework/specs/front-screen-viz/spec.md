## MODIFIED Requirements

### Requirement: Full-screen color visualization
The system SHALL provide a full-screen color visualization mode ("Show") that fills the display with colors that shift and pulse in sync with the audio analysis data.

#### Scenario: Show activated
- **WHEN** the user starts the visualizer with the Show output enabled (or, on a non-Nothing device, simply taps Start)
- **THEN** the screen fills with a color wash driven by the selected [PartyTheme]

#### Scenario: Show deactivated
- **WHEN** the user taps the overlay to exit, or presses system back
- **THEN** the overlay closes and the tabbed main view is restored; the visualizer continues running if it was running

### Requirement: Show is off by default
The system SHALL start with the Show overlay not visible. The front screen SHALL show the tabbed controls UI by default to conserve battery.

#### Scenario: App launched
- **WHEN** the app is first opened
- **THEN** the screen shows the tabbed controls UI, not the full-screen visualization

### Requirement: Output toggles drive what happens on Start
The system SHALL allow the user to independently enable or disable two output modes: **Glyphs** (Nothing devices only) and **Show** (front-screen color wash). Tapping Start SHALL activate the enabled outputs; the controls SHALL NOT permit both outputs to be disabled simultaneously.

#### Scenario: Both outputs enabled
- **WHEN** the user has both outputs enabled and taps Start
- **THEN** the glyph LEDs light up AND the Show overlay opens automatically

#### Scenario: Show only
- **WHEN** the user has only Show enabled and taps Start
- **THEN** the Show overlay opens AND the Glyph SDK session is not opened

#### Scenario: Glyphs only
- **WHEN** the user has only Glyphs enabled and taps Start
- **THEN** the glyph LEDs light up AND no Show overlay is shown

#### Scenario: Auto-flip the last enabled toggle
- **WHEN** the user attempts to disable the last enabled output
- **THEN** the other output is automatically enabled so that at least one output is always active

#### Scenario: Non-Nothing device
- **WHEN** the app runs on a non-Nothing device
- **THEN** the Glyphs toggle is not shown and the Show output is implicit (always on)

### Requirement: Re-launch Show overlay from the running visualizer
While the visualizer is running and the Show output is enabled, the system SHALL provide a way to re-open the Show overlay after the user has dismissed it, without stopping the visualizer.

#### Scenario: Tap visualizer card to re-launch
- **WHEN** the visualizer is running, the Show output is enabled, and the user taps the spectrum visualizer card on the Play tab
- **THEN** the Show overlay re-opens with the visualizer still running

## ADDED Requirements

### Requirement: Show overlay has no text
The Show overlay SHALL render only the color wash from the selected theme. It SHALL NOT render the find-me overlay text or any other user-configured text. (Beacon overlay owns the text-overlay feature.)

#### Scenario: No text on Show
- **WHEN** the user has configured beacon text and the Show overlay opens
- **THEN** only the color wash is rendered, with the "Tap to exit" hint at the bottom

## REMOVED Requirements

### Requirement: Optional centered character overlay
**Reason**: The overlay-text feature now belongs to the Beacon overlay. Mixing audio-reactive themes with a static find-me text on the same overlay confused users about which feature they were using.

**Migration**: Existing saved overlay text is preserved on next launch as `beaconText` and rendered by the Beacon overlay. See `settings-persistence` for migration details.
