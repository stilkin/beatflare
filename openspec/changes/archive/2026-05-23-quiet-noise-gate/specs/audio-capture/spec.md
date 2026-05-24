## ADDED Requirements

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
