## ADDED Requirements

### Requirement: Selectable party mode themes
The system SHALL provide multiple color themes for the front-screen party mode visualization. The user SHALL be able to select a theme from the settings panel.

#### Scenario: User selects a theme
- **WHEN** the user picks "Fire" from the theme selector in settings
- **THEN** the party mode overlay uses a red-orange-yellow palette driven by bass intensity

#### Scenario: Theme persists across restarts
- **WHEN** the user selects "Ocean" and restarts the app
- **THEN** the "Ocean" theme is still selected

### Requirement: Six built-in themes
The system SHALL include these themes: Spectrum (default), Fire, Ocean, Monochrome, Rainbow, Strobe. Each theme SHALL map AudioAnalysis data to a Color using a different strategy.

#### Scenario: Spectrum theme (default)
- **WHEN** the Spectrum theme is active
- **THEN** hue shifts based on the dominant frequency band, brightness pulses with bass

#### Scenario: Fire theme
- **WHEN** the Fire theme is active
- **THEN** colors stay within red-orange-yellow range, bass amplitude drives intensity

#### Scenario: Ocean theme
- **WHEN** the Ocean theme is active
- **THEN** colors stay within blue-teal-cyan range, mid-frequency content shifts hue

#### Scenario: Monochrome theme
- **WHEN** the Monochrome theme is active
- **THEN** screen is white only, overall amplitude drives brightness from black to white

#### Scenario: Rainbow theme
- **WHEN** the Rainbow theme is active
- **THEN** hue cycles continuously over time, beat detection resets the cycle position

#### Scenario: Strobe theme
- **WHEN** the Strobe theme is active
- **THEN** screen alternates between black and white only on detected beats
