# beacon Specification

## Purpose
TBD - created by archiving change beacon-tab-rework. Update Purpose after archive.
## Requirements
### Requirement: Beacon tab in main navigation
The system SHALL include a Beacon tab as the leftmost destination in the bottom navigation, with order `Beacon | Show | Lights`. Beacon SHALL be the default selected tab on app launch. The Lights tab SHALL be present only on devices with a rear output (Nothing glyphs or a camera torch).

#### Scenario: Beacon tab is reachable
- **WHEN** the user opens the app
- **THEN** the bottom navigation shows the Beacon, Show, and Lights tabs (Lights only when a rear output exists), and Beacon is selected by default

#### Scenario: Switching to Beacon
- **WHEN** the user taps the Beacon tab
- **THEN** the Beacon configuration UI is shown (find-me overlay, hue picker, text colour, React-to-sound toggle, Light-up button)

### Requirement: Beacon overlay rendering
The system SHALL provide a full-screen Beacon overlay that fills the display with a single bright colour and, optionally, a centred text/emoji. The overlay SHALL be launched only from the Beacon tab's "Light up beacon" button.

The background colour SHALL be `HSV(beaconHue, 1.0, brightness)` where `brightness` is 1.0 when "React to sound" is OFF and is audio-modulated (with the quiet-state baseline pulse) when ON.

#### Scenario: Static beacon (React-to-sound OFF)
- **WHEN** the user has React-to-sound disabled and taps "Light up beacon"
- **THEN** the screen fills with the chosen hue at full brightness, no microphone activity is required, and the colour does not change over time

#### Scenario: Reactive beacon (React-to-sound ON)
- **WHEN** the user has React-to-sound enabled and taps "Light up beacon"
- **THEN** the audio pipeline is started if not already running and the chosen hue is rendered with audio-driven brightness, falling back to a slow quiet-state pulse in near-silence

### Requirement: Beacon configuration on the Beacon tab
The Beacon tab SHALL expose: a find-me overlay control (emoji presets + free text input, capped at 4 graphemes), a hue-only background colour picker with live preview swatch, a text-colour preset row, a "React to sound" toggle, and a "Light up beacon" launch button.

#### Scenario: Hue picker is hue-only
- **WHEN** the user is on the Beacon tab
- **THEN** the colour control SHALL expose only a hue slider (no saturation or brightness controls)

#### Scenario: Text colour preset row
- **WHEN** the user is on the Beacon tab
- **THEN** the text-colour control SHALL be a row of five chips: White, Black, Yellow, Pink, Lime — with the current selection visually indicated. Default selection is White.

#### Scenario: React-to-sound toggle
- **WHEN** the user toggles React-to-sound off
- **THEN** the next launch of the Beacon overlay SHALL render a constant-brightness colour and SHALL NOT auto-start the audio service

### Requirement: Beacon auto-starts service when reactive
The "Light up beacon" button SHALL auto-start the audio service when "React to sound" is ON and the service is not already running. The button SHALL NEVER auto-stop the service when the overlay is dismissed.

#### Scenario: Auto-start
- **WHEN** the service is not running, React-to-sound is ON, and the user taps "Light up beacon"
- **THEN** the audio service starts and the Beacon overlay opens

#### Scenario: Beacon dismiss leaves service running
- **WHEN** the user dismisses the Beacon overlay
- **THEN** the audio service continues running if it was running, and only the Lights tab Start/Stop can stop it

### Requirement: Beacon overlay dismissal
The Beacon overlay SHALL be dismissible by tapping anywhere on the screen or by pressing the system back gesture/button. The overlay SHALL display a faint "Tap to exit" hint near the bottom.

#### Scenario: Tap to dismiss
- **WHEN** the user taps anywhere on the Beacon overlay
- **THEN** the overlay closes and the previously selected tab is restored

#### Scenario: Back to dismiss
- **WHEN** the user presses the system back gesture while the Beacon overlay is visible
- **THEN** the overlay closes and the previously selected tab is restored without finishing the activity

### Requirement: Centered text overlay on Beacon
The Beacon overlay SHALL render the user-configured text (1–4 graphemes) centered over the background colour, in the user-selected text colour from the preset row. Text SHALL be auto-sized to fill approximately 85% of the available axis. Multi-grapheme strings SHALL be rotated 90° counter-clockwise so they read along the screen's long axis.

#### Scenario: Single-grapheme upright
- **WHEN** the user has configured "❤️" and launches Beacon
- **THEN** the heart is rendered upright, centred, sized to fill ~85% of the shorter axis

#### Scenario: Multi-grapheme rotated
- **WHEN** the user has configured "HERE" and launches Beacon
- **THEN** the text is rotated counter-clockwise and rendered to fill ~85% of the long axis

#### Scenario: No text
- **WHEN** the user has not configured beacon text
- **THEN** the Beacon overlay shows only the colour wash, with the "Tap to exit" hint

### Requirement: Beacon and Show are mutually exclusive
The Beacon overlay and the Show overlay SHALL NOT be displayed simultaneously. Launching Beacon while the Show overlay is visible SHALL dismiss the Show overlay first, and vice versa.

#### Scenario: Beacon launched over Show
- **WHEN** the Show overlay is visible and the user navigates to the Beacon tab and taps "Light up beacon"
- **THEN** the Show overlay is dismissed and the Beacon overlay is shown

#### Scenario: Show launched over Beacon
- **WHEN** the Beacon overlay is visible and the user launches the Show overlay from the Show tab
- **THEN** the Beacon overlay is dismissed and the Show overlay is shown

### Requirement: Beacon is independent of Glyphs
Glyph LED output SHALL be controlled solely by the Glyphs output toggle on the Lights tab. Launching, dismissing, or configuring Beacon SHALL NOT affect glyph output state.

#### Scenario: Glyphs continue under Beacon
- **WHEN** the audio service is running with Glyphs enabled and the user launches Beacon
- **THEN** the glyph LEDs continue to visualize audio on the back of the phone while Beacon renders on the front

