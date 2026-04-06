## ADDED Requirements

### Requirement: Settings survive app restart
The system SHALL persist all user settings (brightness, zone toggles, selected theme) to local storage. Settings SHALL be restored when the app or service starts.

#### Scenario: Brightness persists
- **WHEN** the user sets brightness to 50% and force-kills the app
- **THEN** on next launch, brightness is still 50%

#### Scenario: Zone toggles persist
- **WHEN** the user disables the Beat zone and restarts
- **THEN** the Beat zone toggle is still off after restart

#### Scenario: Theme persists
- **WHEN** the user selects the Fire theme and restarts
- **THEN** the Fire theme is still selected after restart

### Requirement: Settings saved on every change
The system SHALL save settings immediately when the user changes any value (not only on app exit).

#### Scenario: Setting saved immediately
- **WHEN** the user drags the brightness slider
- **THEN** the new brightness value is persisted before the user exits the app

## MODIFIED Requirements

### Requirement: Guard glyph initialization for non-Nothing devices
The system SHALL detect whether the device is a Nothing Phone. If not, the system SHALL skip glyph SDK initialization and run the audio pipeline with front-screen visualization only.

#### Scenario: Non-Nothing device
- **WHEN** the app is launched on a Samsung Galaxy
- **THEN** the audio pipeline starts, party mode works, but no glyph LED control is attempted and glyph-specific UI (zone toggles, brightness slider) is hidden

#### Scenario: Nothing Phone (3a)
- **WHEN** the app is launched on a Nothing Phone (3a)
- **THEN** full functionality including glyph LED control is available
