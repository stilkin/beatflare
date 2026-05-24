## MODIFIED Requirements

### Requirement: Selectable party mode themes
The system SHALL provide multiple color themes for the Show overlay. The user SHALL be able to select a theme from the Show tab.

#### Scenario: User selects a theme
- **WHEN** the user picks "Fire" from the theme list on the Show tab
- **THEN** the Show overlay uses a red-orange-yellow palette driven by bass intensity

#### Scenario: Theme persists across restarts
- **WHEN** the user selects "Ocean" and restarts the app
- **THEN** the "Ocean" theme is still selected

### Requirement: Built-in themes
The system SHALL include these themes: Spectrum (default), Rainbow, Fire, Ocean, Breathe, Sweep, Strobe. Each theme SHALL map AudioAnalysis data and a wall-clock time value to a Color using a different strategy. The Monochrome theme has been removed; its role is replaced by the Beacon overlay.

#### Scenario: Spectrum theme tracks frequency content
- **WHEN** the Spectrum theme is active and audio is playing
- **THEN** hue tracks a smoothed frequency centroid (low frequencies → red/orange, high frequencies → blue/violet) and brightness pulses with bass

#### Scenario: Spectrum theme does not flicker on transient noise
- **WHEN** the Spectrum theme is active and a sharp transient occurs in a single FFT band
- **THEN** the hue shifts smoothly toward the new dominant frequency rather than jumping discontinuously

#### Scenario: Fire theme
- **WHEN** the Fire theme is active
- **THEN** colors stay within red-orange-yellow range, bass amplitude drives intensity

#### Scenario: Ocean theme
- **WHEN** the Ocean theme is active
- **THEN** colors stay within blue-teal-cyan range, mid-frequency content shifts hue

#### Scenario: Rainbow theme cycles continuously
- **WHEN** the Rainbow theme is active
- **THEN** hue cycles continuously as a function of wall-clock time, and brightness is modulated by bass; beats do NOT reset the cycle position

#### Scenario: Strobe theme flashes on transients
- **WHEN** the Strobe theme is active and a transient is detected (beat OR bass level above threshold)
- **THEN** the screen flashes high-contrast white

#### Scenario: Strobe theme remains alive during quiet passages
- **WHEN** the Strobe theme is active and no transient has fired for at least 500 ms
- **THEN** the screen shows a dim white pulse driven by bass and a baseline pulse, not solid black

#### Scenario: Breathe theme
- **WHEN** the Breathe theme is active
- **THEN** the screen shows a fixed-hue color with a slow sine-wave brightness modulation; bass amplitude additively increases brightness

#### Scenario: Sweep theme
- **WHEN** the Sweep theme is active
- **THEN** the hue rotates slowly over time within a narrow range (cool hues), and bass amplitude drives saturation

## ADDED Requirements

### Requirement: Themes are grouped on the Show tab
The Show tab SHALL present themes organised into three groups, each labelled with a heading: **Spectrum**, **Mood**, **Pulse**. Group membership:
- Spectrum: Spectrum, Rainbow
- Mood: Fire, Ocean
- Pulse: Breathe, Sweep, Strobe

Each theme entry SHALL display its name and a one-line subtitle describing its behaviour.

#### Scenario: Show tab renders three groups
- **WHEN** the user opens the Show tab
- **THEN** the visible UI contains three labelled groups (Spectrum, Mood, Pulse) listing the themes specified above, each with a one-line subtitle

#### Scenario: Subtitle visible per theme
- **WHEN** the Show tab is rendered
- **THEN** each theme row SHALL display a one-line subtitle, not just the theme name
