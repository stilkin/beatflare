## ADDED Requirements

### Requirement: Flash output settings persist
The system SHALL persist the Flash output settings — whether Flash is enabled and its intensity — and SHALL restore them when the app or service starts. Devices without a camera flash SHALL ignore these settings without error.

#### Scenario: Flash enable persists
- **WHEN** the user enables Flash and force-kills the app
- **THEN** Flash is still enabled on next launch

#### Scenario: Flash intensity persists
- **WHEN** the user sets a Flash intensity and restarts
- **THEN** the same intensity is restored after restart

#### Scenario: Backwards-compatible default
- **WHEN** the app is upgraded from a version that did not persist Flash settings
- **THEN** Flash settings fall back to their defaults and no migration error occurs
