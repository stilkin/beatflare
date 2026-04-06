## ADDED Requirements

### Requirement: Continuous microphone audio capture
The system SHALL capture audio from the device microphone using `AudioRecord` at 44100 Hz, mono, PCM 16-bit. Capture SHALL run continuously while the visualizer service is active.

#### Scenario: Service starts capturing audio
- **WHEN** the visualizer service is started
- **THEN** the system begins capturing audio from the microphone at 44100 Hz mono PCM 16-bit

#### Scenario: Service stops capturing audio
- **WHEN** the visualizer service is stopped
- **THEN** the system releases the AudioRecord resource and stops capturing

### Requirement: Real-time FFT processing
The system SHALL perform FFT analysis on captured audio buffers of ~2048 samples (~46ms windows) and output frequency band energy values continuously.

#### Scenario: Audio buffer is processed
- **WHEN** a 2048-sample audio buffer is captured
- **THEN** the system computes FFT and produces frequency band energy values within 20ms

### Requirement: Frequency band splitting
The system SHALL split the FFT output into 3 bands: sub-bass/bass (20–250 Hz), mid/full spectrum (split across 20 sub-bands), and transient/beat detection.

#### Scenario: FFT output is split into bands
- **WHEN** FFT processing completes for a buffer
- **THEN** 3 band groups are produced: bass energy (single value), spectrum (20 values for zone C), and beat detection (boolean pulse)

### Requirement: Adaptive volume normalization
The system SHALL normalize audio levels using a rolling peak with exponential decay (default window: 5 seconds, half-life: ~3 seconds). Current amplitude SHALL be expressed as a percentage of the rolling peak.

#### Scenario: Volume adapts to loud environment
- **WHEN** ambient audio is consistently loud (e.g. festival main stage)
- **THEN** the rolling peak rises and the visualization uses the full LED brightness range relative to the current loudness

#### Scenario: Volume adapts after sudden spike
- **WHEN** a sudden loud spike occurs followed by normal levels
- **THEN** the rolling peak decays exponentially, restoring sensitivity within ~6 seconds

### Requirement: RECORD_AUDIO permission handling
The system SHALL request the `RECORD_AUDIO` runtime permission before starting audio capture. If denied, the system SHALL show an explanation and not crash.

#### Scenario: Permission granted
- **WHEN** the user grants RECORD_AUDIO permission
- **THEN** audio capture starts normally

#### Scenario: Permission denied
- **WHEN** the user denies RECORD_AUDIO permission
- **THEN** the system displays a message explaining why the permission is needed and does not attempt capture
