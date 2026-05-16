## MODIFIED Requirements

### Requirement: Selectable party mode themes
The system SHALL provide multiple color themes for the front-screen party mode visualization. The user SHALL be able to select a theme from the settings panel.

#### Scenario: User selects a theme
- **WHEN** the user picks "Fire" from the theme selector in settings
- **THEN** the party mode overlay uses a red-orange-yellow palette driven by bass intensity

#### Scenario: Theme persists across restarts
- **WHEN** the user selects "Ocean" and restarts the app
- **THEN** the "Ocean" theme is still selected

### Requirement: Built-in themes
The system SHALL include these themes: Spectrum (default), Fire, Ocean, Monochrome, Rainbow, Strobe, Breathe, Sweep. Each theme SHALL map AudioAnalysis data and a wall-clock time value to a Color using a different strategy.

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

#### Scenario: Monochrome theme
- **WHEN** the Monochrome theme is active
- **THEN** screen is white only, overall amplitude drives brightness from black to white

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

### Requirement: Quiet-state baseline pulse
Every theme SHALL show some visible motion at all times, even when no audio is detected. When audio energy is near zero, themes SHALL render a low-amplitude pulse driven by a wall-clock sine wave so the screen never appears frozen or pitch black.

#### Scenario: No audio input
- **WHEN** the visualizer is running and the microphone picks up no significant audio for several seconds
- **THEN** the active theme renders a slow, low-brightness pulse (~5–15% peak brightness) rather than a static dark screen

#### Scenario: Audio resumes
- **WHEN** audio energy rises back above the noise floor after a quiet passage
- **THEN** the baseline pulse fades out and full audio-driven visualization resumes
