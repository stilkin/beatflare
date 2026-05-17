## MODIFIED Requirements

### Requirement: Monochrome theme accepts a user color
The Monochrome theme SHALL render the screen in a user-configured color (default white) with brightness modulated by bass amplitude and the quiet-state baseline pulse. The user SHALL be able to pick the color via a control in the Party tab.

#### Scenario: Default mono color
- **WHEN** the user has not customized the Mono color
- **THEN** the Mono theme renders white with audio-driven brightness

#### Scenario: Custom mono color persists
- **WHEN** the user picks a magenta color for Mono and restarts the app
- **THEN** the Mono theme continues to use the chosen magenta color

#### Scenario: Color picker only visible when Mono is selected
- **WHEN** the user is on the Party tab and Mono is not the selected theme
- **THEN** no color picker is shown

- **WHEN** the user selects Mono
- **THEN** a hue + saturation picker (with a live preview swatch) appears beneath the theme selector. Brightness is intentionally audio-driven and not user-configurable.
