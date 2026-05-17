## MODIFIED Requirements

### Requirement: Settings survive app restart
The system SHALL persist all user settings to local storage. Persisted settings SHALL include: brightness, zone toggles, selected theme, custom Mono color, party-overlay character, and the Glyphs / Party output toggles. Settings SHALL be restored when the app or service starts.

#### Scenario: Brightness persists
- **WHEN** the user sets brightness to 50% and force-kills the app
- **THEN** on next launch, brightness is still 50%

#### Scenario: Zone toggles persist
- **WHEN** the user disables the Beat zone and restarts
- **THEN** the Beat zone toggle is still off after restart

#### Scenario: Theme persists
- **WHEN** the user selects the Fire theme and restarts
- **THEN** the Fire theme is still selected after restart

#### Scenario: Mono custom color persists
- **WHEN** the user picks a custom color for the Mono theme and restarts
- **THEN** the same custom color is still configured for Mono after restart

#### Scenario: Overlay character persists
- **WHEN** the user sets a party-overlay emoji and restarts
- **THEN** the same emoji is still configured after restart

#### Scenario: Output toggles persist
- **WHEN** the user turns off the Glyphs output toggle and restarts
- **THEN** the Glyphs output toggle is still off after restart

#### Scenario: Backwards-compatible defaults
- **WHEN** the app is upgraded from a version that did not persist Mono color, overlay character, or output toggles
- **THEN** Mono color defaults to white, overlay character defaults to empty, both output toggles default to enabled, and no migration error occurs
