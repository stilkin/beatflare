# audio-capture Specification

## Purpose
The microphone capture and analysis pipeline — continuous AudioRecord capture,
real-time FFT, frequency-band splitting, adaptive volume normalization, and
RECORD_AUDIO permission handling.
## Requirements
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

### Requirement: Quiet-state output gating
The system SHALL gate the analysis output to clean zeros when the overall frame level is below a quiet threshold, with hysteresis to prevent chattering at the boundary. Frame level SHALL be computed as the maximum of the normalized bass level and all normalized spectrum band values.

When the gate is closed, all of `bassLevel`, every value in `spectrum`, and `beat` SHALL be reported as zero / false. When the gate is open, the original normalized values SHALL pass through unchanged.

The gate SHALL open when the frame level rises to at least 0.10 and SHALL close when the frame level falls to at most 0.06. The gate SHALL start in the closed state.

#### Scenario: Output is gated during near-silence
- **WHEN** ambient audio is silent or near-silent and the frame level stays below 0.06
- **THEN** every analysis frame reports `bassLevel == 0`, all `spectrum` values == 0, and `beat == false`

#### Scenario: Gate opens when real signal arrives
- **WHEN** the frame level rises to 0.10 or higher
- **THEN** subsequent analysis frames report the original normalized bass, spectrum, and beat values until the gate closes again

#### Scenario: Hysteresis prevents chatter at the boundary
- **WHEN** the frame level is in the range [0.06, 0.10] after the gate was previously open
- **THEN** the gate remains open and frames pass through unchanged

#### Scenario: Hysteresis prevents chatter when previously closed
- **WHEN** the frame level is in the range [0.06, 0.10] after the gate was previously closed
- **THEN** the gate remains closed and frames are reported as zero

