## ADDED Requirements

### Requirement: Custom app icon
The system SHALL use a custom adaptive icon with a distinct foreground graphic and colored background. The icon SHALL be provided as Android vector drawables.

#### Scenario: Icon visible on home screen
- **WHEN** the app is installed
- **THEN** the home screen shows the custom icon, not the default Android icon

### Requirement: Accent color palette
The system SHALL define a primary and secondary accent color derived from the app icon. These colors SHALL be applied consistently to: start/stop button, spectrum bars, beat indicator, slider track, party mode button, widget running state, and notification icon tint.

#### Scenario: Accent colors applied
- **WHEN** the user opens the app
- **THEN** interactive elements use the accent palette instead of default Material white

### Requirement: Card-based layout
The main activity SHALL group related controls into visually distinct cards with slight elevation over the background. Settings, visualization, and actions SHALL each be in separate cards.

#### Scenario: Settings grouped in card
- **WHEN** the user views the main screen
- **THEN** brightness slider, zone toggles, and theme selector are grouped in a single card with a subtle background

### Requirement: Debug info collapsed by default
The analysis debug values (raw, floor, peak log numbers) SHALL be hidden by default behind a collapsible toggle. The spectrum bars and beat indicator SHALL remain visible.

#### Scenario: Debug values hidden
- **WHEN** the visualizer is running and the user has not expanded debug
- **THEN** spectrum bars and beat indicator are visible but raw/floor/peak values are not

#### Scenario: Debug values shown
- **WHEN** the user expands the debug section
- **THEN** the raw/floor/peak log values become visible

## MODIFIED Requirements

### Requirement: Widget reflects current state
The widget SHALL use the accent color palette for its running-state background instead of the current green.

#### Scenario: Widget running state
- **WHEN** the visualizer is running
- **THEN** the widget background uses the primary accent color

### Requirement: Full-screen color visualization
The spectrum bars in the analysis display SHALL use the primary accent color instead of the hardcoded cyan (#4FC3F7).

#### Scenario: Spectrum bars styled
- **WHEN** the visualizer is running
- **THEN** spectrum bars render in the primary accent color
