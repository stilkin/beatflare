## MODIFIED Requirements

### Requirement: Full-screen color visualization
The system SHALL provide a full-screen color visualization mode ("Show") that fills the display with colors that shift and pulse in sync with the audio analysis data.

#### Scenario: Show activated
- **WHEN** the user launches the Show overlay (from the Show tab, or by tapping the live monitor on the Lights tab while a session is running)
- **THEN** the screen fills with a color wash driven by the selected [PartyTheme]

#### Scenario: Show deactivated
- **WHEN** the user taps the overlay to exit, or presses system back
- **THEN** the overlay closes and the tabbed main view is restored; capture continues only if another consumer (such as a persistent session) still needs it, and is released if the overlay was the sole consumer

## ADDED Requirements

### Requirement: Show is off by default
The system SHALL start with the Show overlay not visible. The front screen SHALL show the tabbed controls UI by default to conserve battery.

#### Scenario: App launched
- **WHEN** the app is first opened
- **THEN** the screen shows the tabbed controls UI, not the full-screen visualization

### Requirement: Show overlay has no text
The Show overlay SHALL render only the color wash from the selected theme. It SHALL NOT render the find-me overlay text or any other user-configured text. (Beacon overlay owns the text-overlay feature.)

#### Scenario: No text on Show
- **WHEN** the user has configured beacon text and the Show overlay opens
- **THEN** only the color wash is rendered, with the "Tap to exit" hint at the bottom

### Requirement: Re-launch Show overlay from the running visualizer
While a persistent session is running, the system SHALL provide a way to re-open the Show overlay after the user has dismissed it, without stopping the session.

#### Scenario: Tap the live monitor to re-launch
- **WHEN** a persistent session is running and the user taps the spectrum monitor card on the Lights tab
- **THEN** the Show overlay re-opens with the session still running

## REMOVED Requirements

### Requirement: Party mode is off by default
**Reason**: Renamed to "Show is off by default" (the front-screen overlay is now the Show overlay). Re-added under the new name in this change so the archive applies a clean remove + add rather than a name-mismatched modify.

### Requirement: Output toggles drive what happens on Start
**Reason**: The capture-lifecycle-rework removed the Glyphs/Show output toggles. Glyphs run via the persistent Start/Stop on the Lights tab; the Show overlay is launched from its own tab. Which output runs is now expressed by which tab's action you invoke, not a toggle. (capture-lifecycle-rework archived before this change without removing the requirement, so it is retired here.)

**Migration**: None — the persisted `glyphsOutputEnabled`/`partyOutputEnabled` fields remain readable but unused; no user action needed.

### Requirement: Re-launch party overlay from the running visualizer
**Reason**: Renamed to "Re-launch Show overlay from the running visualizer" and rehomed from the removed Play tab to the Lights tab's live monitor (per capture-lifecycle-rework). Re-added under the new name above.

### Requirement: Optional centered character overlay
**Reason**: The overlay-text feature now belongs to the Beacon overlay. Mixing audio-reactive themes with a static find-me text on the same overlay confused users about which feature they were using.

**Migration**: Existing saved overlay text is preserved on next launch as `beaconText` and rendered by the Beacon overlay. See `settings-persistence` for migration details.
