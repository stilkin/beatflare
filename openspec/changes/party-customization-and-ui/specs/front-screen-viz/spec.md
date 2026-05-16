## ADDED Requirements

### Requirement: Optional centered character overlay
The party mode overlay SHALL support an optional user-configured character (emoji or letter) rendered centered over the color wash. When configured, the character SHALL be large (~60% of the shorter screen dimension) and SHALL include a soft shadow for legibility against bright color backgrounds.

#### Scenario: No overlay configured
- **WHEN** the user has not configured an overlay character
- **THEN** the party mode overlay shows only the color wash, with the "Tap to exit" hint

#### Scenario: Emoji overlay configured
- **WHEN** the user configures "❤️" as the overlay character and launches party mode
- **THEN** a large red heart is rendered centered over the color wash and remains legible across all themes

#### Scenario: Overlay persists
- **WHEN** the user sets an overlay character and restarts the app
- **THEN** the same character remains configured
