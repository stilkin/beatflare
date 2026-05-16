## 1. API change: add time parameter

- [ ] 1.1 Update `PartyTheme.deriveColor` signature to accept `nowMs: Long`
- [ ] 1.2 Update `PartyOverlay` to pass `System.currentTimeMillis()` per frame
- [ ] 1.3 Refactor `RAINBOW.hueOffset` field → compute from `nowMs` directly

## 2. Quiet-state baseline pulse

- [ ] 2.1 Add a private `quietPulse(nowMs, bassLevel)` helper inside `PartyTheme` returning a 0..1 factor (sine wave * (1 - bassLevel))
- [ ] 2.2 Apply the baseline to all existing themes' lightness floor so screen never fully blacks out during quiet passages

## 3. Strobe rework

- [ ] 3.1 Define "transient" = `analysis.beat || bassLevel > 0.6f`
- [ ] 3.2 On transient: flash white (full brightness)
- [ ] 3.3 No transient: dim white pulse driven by bass + quiet baseline (5–15% brightness)

## 4. Spectrum rework

- [ ] 4.1 Compute frequency centroid: weighted average of band index using band value as weight
- [ ] 4.2 Map centroid to hue: 0° (red) for low bands, 280° (violet) for high bands
- [ ] 4.3 Smooth the centroid frame-to-frame (EMA, ~0.3 alpha) to avoid jitter
- [ ] 4.4 Keep bass-driven brightness + quiet baseline

## 5. Rainbow polish

- [ ] 5.1 Increase bass-driven brightness range (0.25 base, 0.65 modulation)
- [ ] 5.2 Remove "reset on beat" jump — keep cycle continuous
- [ ] 5.3 Compute `hueOffset` from `nowMs` instead of accumulating a field

## 6. New themes

- [ ] 6.1 Add `BREATHE` theme: fixed hue 280°, slow sine on brightness, bass adds intensity
- [ ] 6.2 Add `SWEEP` theme: narrow hue range (200–320°), time-driven slow rotation, bass modulates saturation
- [ ] 6.3 Ensure theme selector in `MainActivity` renders all 8 themes (currently uses `chunked(3)` — should adapt automatically)

## 7. Verification

- [ ] 7.1 Build and install: `./gradlew :app:assembleDebug` then `adb install -r ...`
- [ ] 7.2 With music playing on phone speaker: each theme visibly reacts to audio
- [ ] 7.3 With music paused (silence): every theme shows gentle baseline motion, not pitch black
- [ ] 7.4 Strobe specifically: flashes on loud transients, dim pulse otherwise (no static black)
- [ ] 7.5 Spectrum vs Rainbow: visibly distinct behavior (Spectrum tracks frequency content, Rainbow cycles)
- [ ] 7.6 ktlint passes: `./gradlew ktlintCheck`
