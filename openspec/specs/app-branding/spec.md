# app-branding Specification

## Purpose
The app's visual identity and UI shell — the adaptive launcher icon, accent color
palette, card-based tabbed layout, debug section, and notification small-icon, plus
where those styles are applied across the UI and widget.

## Requirements

### Requirement: Custom app icon
The system SHALL use a custom adaptive icon with separate vector foreground and background layers. The icon SHALL be a flat-design rendering (no photorealistic shading) so it can be expressed entirely as Android VectorDrawable XML. Raster mipmap variants SHALL NOT be used.

#### Scenario: Icon visible on home screen
- **WHEN** the app is installed
- **THEN** the home screen shows the custom adaptive icon, rendered from vector layers

#### Scenario: Icon adapts to launcher masks
- **WHEN** the launcher applies a round, squircle, or rectangular mask
- **THEN** the icon background fills the mask shape cleanly and the foreground bars remain centered and visible

### Requirement: Accent color palette
The system SHALL define a primary and secondary accent color derived from the app icon. These colors SHALL be applied consistently to: start/stop button, spectrum bars, beat indicator, slider track, widget running state, and notification icon tint.

#### Scenario: Accent colors applied
- **WHEN** the user opens the app
- **THEN** interactive elements use the accent palette instead of default Material white

### Requirement: Card-based tabbed layout
The main activity SHALL organize controls into bottom-navigation tabs rather than a single scrollable column. Each tab SHALL group related controls into cards with subtle elevation over the background.

#### Scenario: Tab navigation
- **WHEN** the user opens the app
- **THEN** a bottom navigation bar shows Play, Party, and (on Nothing devices) Glyphs tabs
- **AND** the Play tab is selected by default

#### Scenario: Tab persists across rotation
- **WHEN** the user selects the Party tab and rotates the device
- **THEN** the Party tab remains selected

#### Scenario: Glyphs tab hidden on non-Nothing devices
- **WHEN** the app runs on a non-Nothing Android device
- **THEN** the bottom navigation shows only Play and Party tabs

### Requirement: Debug info collapsed by default
The analysis debug values (raw, floor, peak log numbers) SHALL be hidden by default behind a collapsible toggle. The spectrum bars and beat indicator SHALL remain visible.

#### Scenario: Debug values hidden
- **WHEN** the visualizer is running and the user has not expanded debug
- **THEN** spectrum bars and beat indicator are visible but raw/floor/peak values are not

#### Scenario: Debug values shown
- **WHEN** the user expands the debug section
- **THEN** the raw/floor/peak log values become visible

### Requirement: Dedicated notification small icon
The system SHALL provide a dedicated monochrome vector drawable for the foreground service notification's small icon. The icon SHALL be white on transparent background, following Android's small-icon masking conventions.

#### Scenario: Notification renders recognizable icon
- **WHEN** the visualizer is running and the foreground notification is visible
- **THEN** the status bar shows a recognizable BeatFlare silhouette, not a generic white blob

### Requirement: Full-screen color visualization
The spectrum bars in the analysis display SHALL use the primary accent color instead of the hardcoded cyan (#4FC3F7).

#### Scenario: Spectrum bars styled
- **WHEN** the visualizer is running
- **THEN** spectrum bars render in the primary accent color
