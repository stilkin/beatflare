## Why

Real-world testing surfaced three issues with the current audio-reactive themes:

1. **Strobe is dead on weak sources.** It only flashes on detected beats. When the source is a phone speaker or any audio with weak transients, no beats fire and the screen stays pitch black — the user thinks the app is broken.
2. **Rainbow and Spectrum feel like duplicates.** Both end up sweeping through hues; Spectrum's dominant-bin approach reads as jittery noise rather than as "this color = this frequency."
3. **When the music goes quiet (a breakdown, ambient passage), every theme freezes.** Nothing moves. The visualizer feels broken instead of alive.

This change makes the visualizer feel alive even during quiet passages, separates Rainbow from Spectrum, and adds a couple of motion-based themes that have been requested.

## What Changes

- **Strobe rework**: redefine as transient-driven high-contrast flash, with a dim bass-modulated pulse as ambient state when no transients have fired recently.
- **Spectrum rework**: lock hue directly to band frequency (low = red, high = violet) instead of "hue follows dominant bin index." Read becomes "this color = this part of the audio."
- **Rainbow rework**: keep the time-driven hue cycle, but modulate brightness with bass (audio stays relevant even when the cycle is autonomous).
- **Quiet-state fallback**: every theme adds a dim baseline pulse driven by bass when audio is below the noise floor, so the screen never sits black for more than a few hundred ms.
- **Two new themes**: `Breathe` (slow sine pulse on a fixed hue, bass adds brightness) and `Sweep` (radial gradient that rotates over time, bass adds saturation/brightness).

No changes to the audio analysis pipeline (FFT, beat detector, normalizer) — those are working as designed and the user's festival test will validate the beat detector on real PA systems.

## Capabilities

### Modified Capabilities
- `party-themes`: Strobe/Rainbow/Spectrum semantics change; two new themes added; quiet-state fallback documented.

## Impact

- **Files affected**: `PartyTheme.kt` (main), `PartyOverlay.kt` (read fallback state if needed)
- **Risk**: Low — all changes are inside `PartyTheme.deriveColor`. No service/audio/glyph changes.
- **Backwards compatibility**: Theme enum gains two values. Persisted `partyTheme` setting stays valid (existing themes keep their names).
- **Tests**: Visual verification on device — there's no good unit test for "does this look alive."
