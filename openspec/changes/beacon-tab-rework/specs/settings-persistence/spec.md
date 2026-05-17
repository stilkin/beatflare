## MODIFIED Requirements

### Requirement: Settings survive app restart
The system SHALL persist all user settings to local storage. Persisted settings SHALL include: brightness, zone toggles, selected theme, Beacon background hue, Beacon text, Beacon text colour, React-to-sound toggle, and the Glyphs / Show output toggles. Settings SHALL be restored when the app or service starts.

#### Scenario: Brightness persists
- **WHEN** the user sets brightness to 50% and force-kills the app
- **THEN** on next launch, brightness is still 50%

#### Scenario: Zone toggles persist
- **WHEN** the user disables the Beat zone and restarts
- **THEN** the Beat zone toggle is still off after restart

#### Scenario: Theme persists
- **WHEN** the user selects the Fire theme and restarts
- **THEN** the Fire theme is still selected after restart

#### Scenario: Beacon hue persists
- **WHEN** the user picks a beacon hue and restarts
- **THEN** the same hue is still configured for Beacon after restart

#### Scenario: Beacon text persists
- **WHEN** the user sets beacon text and restarts
- **THEN** the same text is still configured after restart

#### Scenario: Beacon text colour persists
- **WHEN** the user selects a beacon text colour and restarts
- **THEN** the same text colour is still selected after restart

#### Scenario: React-to-sound toggle persists
- **WHEN** the user disables React-to-sound and restarts
- **THEN** React-to-sound is still disabled after restart

#### Scenario: Output toggles persist
- **WHEN** the user turns off the Glyphs output toggle and restarts
- **THEN** the Glyphs output toggle is still off after restart

#### Scenario: Backwards-compatible defaults
- **WHEN** the app is upgraded from a version that did not persist Beacon settings or React-to-sound
- **THEN** Beacon hue defaults to white-equivalent (0°, but effectively any hue at full brightness is bright), beacon text defaults to empty, beacon text colour defaults to White, React-to-sound defaults to ON, both output toggles default to enabled, and no migration error occurs

## ADDED Requirements

### Requirement: Migrate Monochrome and overlay-text settings
On first load after upgrading to a build with Beacon, the system SHALL perform a one-shot migration:
1. If `partyTheme == MONOCHROME` SHALL be remapped to `SPECTRUM`.
2. If `monoColor` is set and `beaconHue` is not, the hue component of `monoColor` SHALL be extracted (HSV conversion) and stored as `beaconHue`. The saturation and value components SHALL be discarded.
3. If `partyOverlayText` is set and `beaconText` is not, its value SHALL be copied into `beaconText`.

After successful migration, the legacy keys (`monoColor`, `partyOverlayText`) MAY be cleared.

#### Scenario: User with Mono theme upgrades
- **WHEN** the user had `partyTheme = MONOCHROME` with `monoColor = #FF6600` (orange) and opens the new build
- **THEN** `partyTheme` is now `SPECTRUM`, and `beaconHue` reflects the hue of `#FF6600`

#### Scenario: User with overlay text upgrades
- **WHEN** the user had `partyOverlayText = "JD"` and opens the new build
- **THEN** `beaconText` is `"JD"` and the Beacon overlay (when launched) renders "JD"

#### Scenario: Fresh install
- **WHEN** the user installs the app for the first time
- **THEN** no migration runs and all settings start at their defaults
