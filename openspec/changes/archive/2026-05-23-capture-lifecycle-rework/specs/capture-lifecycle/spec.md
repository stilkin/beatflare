## ADDED Requirements

### Requirement: Microphone runs only when a consumer needs it
The system SHALL run microphone capture if and only if at least one capture consumer is active. The consumers are: a single persistent output session (the hands-off output — Glyphs, or Flash on non-Nothing devices), the Show overlay, and the Beacon overlay (the latter only when React-to-sound is enabled). When the last active consumer is released, the system SHALL stop capture and release the microphone and foreground service.

#### Scenario: No consumers means no capture
- **WHEN** no persistent session is running and no reactive overlay is visible
- **THEN** the microphone is not captured and no foreground service runs

#### Scenario: First consumer starts capture
- **WHEN** a consumer becomes active while capture is stopped
- **THEN** the system starts the foreground service and begins microphone capture

#### Scenario: Last consumer stops capture
- **WHEN** the only remaining active consumer is released
- **THEN** the system stops capture and releases the microphone

### Requirement: Each consumer releases only what it started
When a consumer is released, the system SHALL stop capture only if no other consumer is still active. A consumer SHALL NOT stop capture that another consumer still requires. This guarantees that an overlay never tears down a persistent session it did not start, and a persistent session never silences an overlay.

#### Scenario: Reactive Beacon started capture, then is dismissed
- **WHEN** React-to-sound is on, no session was running, the user lights up the Beacon (starting capture), then dismisses it
- **THEN** capture stops and the microphone is released, restoring the prior stopped state

#### Scenario: Beacon dismissed while a persistent session runs
- **WHEN** a persistent session is already running, the user opens the Beacon over it, then dismisses the Beacon
- **THEN** the Beacon's consumer is released but capture continues for the persistent session

#### Scenario: Persistent session stopped while an overlay is up
- **WHEN** both a persistent session and a reactive overlay are active and the user stops the persistent session
- **THEN** capture continues for the overlay until the overlay is also dismissed

### Requirement: Persistent capture session survives screen lock
The persistent consumer SHALL be started and stopped only by explicit user action — the Start/Stop control or the home-screen widget — and SHALL keep capture running while the screen is off, independent of any overlay.

#### Scenario: Persistent session runs with screen off
- **WHEN** the user starts the persistent session and turns off the screen
- **THEN** capture continues and the hands-off output keeps visualizing

#### Scenario: Explicit stop ends the persistent session
- **WHEN** the user taps Stop (or toggles the widget off)
- **THEN** the persistent consumer is released and, if it was the last consumer, capture stops

### Requirement: Overlay capture is bound to overlay visibility
The Show and Beacon overlays SHALL acquire capture when launched and release it when dismissed by tap or system back. Show SHALL always acquire capture (its themes are audio-reactive); Beacon SHALL acquire capture only when React-to-sound is enabled.

#### Scenario: Launching Show acquires capture
- **WHEN** the user launches the Show overlay
- **THEN** the system ensures capture is running for the duration the overlay is visible

#### Scenario: Dismissing an overlay releases its capture
- **WHEN** a reactive overlay that started capture is dismissed
- **THEN** its consumer is released and capture stops unless another consumer still needs it

### Requirement: Static Beacon requires no microphone
When React-to-sound is off, the Beacon SHALL render from stored settings only — it SHALL NOT acquire capture, start the foreground service, or request the microphone.

#### Scenario: Non-reactive Beacon needs no capture
- **WHEN** React-to-sound is off and the user lights up the Beacon
- **THEN** a solid hue is shown with no microphone capture and no foreground service or capture notification

#### Scenario: Dismissing a static Beacon changes no capture state
- **WHEN** a non-reactive Beacon is dismissed
- **THEN** no capture is stopped because none was started, and any unrelated persistent session is unaffected
