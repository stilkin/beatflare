## Why

Tester feedback: when ambient audio is near silence, the visualization is jittery — small fluctuations in mic hiss get amplified by the adaptive normalizer and produce visible LED flicker. The quiet pulse only takes over at true `audioLevel == 0`, so there is a "dead zone" between true silence and a real signal where output looks twitchy. This undermines the calm fallback behaviour we introduced in `audio-feedback-improvements`.

## What Changes

- Add a noise gate in the audio analysis output: when the normalized `audioLevel` is below a small threshold (default ~0.08), force band energies and beat output to zero.
- Add hysteresis so the gate doesn't chatter at the boundary (open at ~0.10, close at ~0.06).
- The quiet-state baseline pulse already in `PartyTheme` handles the gated state — no new pulse logic needed.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `audio-capture`: Add a requirement that the analysis output is gated below a quiet threshold with hysteresis, so downstream consumers (glyph driver, party themes) see clean zeros during silence.

## Impact

- Code: `AudioAnalyzer` (gating logic) and possibly `RollingPeakNormalizer` (depending on where the threshold is best applied).
- No new dependencies, no API changes for the service or UI.
- Affects every theme and the glyph driver indirectly via cleaner zero-state input.
