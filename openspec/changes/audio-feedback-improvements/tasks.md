## 1. API change: add time parameter

- [x] 1.1 Update `PartyTheme.deriveColor` signature to accept `nowMs: Long`
- [x] 1.2 Update `PartyOverlay` to pass `System.currentTimeMillis()` per frame
- [x] 1.3 Refactor `RAINBOW.hueOffset` field → compute from `nowMs` directly

## 2. Quiet-state baseline pulse

- [x] 2.1 Add a private `quietPulse(nowMs, bassLevel)` helper inside `PartyTheme` returning a 0..1 factor (sine wave * (1 - bassLevel))
- [x] 2.2 Apply the baseline to all existing themes' lightness floor so screen never fully blacks out during quiet passages

## 3. Strobe rework

- [x] 3.1 Define "transient" = `analysis.beat || bassLevel > 0.6f`
- [x] 3.2 On transient: flash white (full brightness)
- [x] 3.3 No transient: dim white pulse driven by bass + quiet baseline (5–15% brightness)

## 4. Spectrum rework

- [x] 4.1 Compute frequency centroid: weighted average of band index using band value as weight
- [x] 4.2 Map centroid to hue: 0° (red) for low bands, 280° (violet) for high bands
- [x] 4.3 Smooth the centroid frame-to-frame (EMA, ~0.3 alpha) to avoid jitter
- [x] 4.4 Keep bass-driven brightness + quiet baseline

## 5. Rainbow polish

- [x] 5.1 Increase bass-driven brightness range (0.25 base, 0.65 modulation)
- [x] 5.2 Remove "reset on beat" jump — keep cycle continuous
- [x] 5.3 Compute `hueOffset` from `nowMs` instead of accumulating a field

## 6. New themes

- [x] 6.1 Add `BREATHE` theme: fixed hue 280°, slow sine on brightness, bass adds intensity
- [x] 6.2 Add `SWEEP` theme: narrow hue range (200–320°), time-driven slow rotation, bass modulates saturation
- [x] 6.3 Ensure theme selector in `MainActivity` renders all 8 themes (currently uses `chunked(3)` — should adapt automatically)

## 7. Verification

- [x] 7.1 Build and install: `./gradlew :app:assembleDebug` then `adb install -r ...`
- [ ] 7.2 With music playing on phone speaker: each theme visibly reacts to audio
- [ ] 7.3 With music paused (silence): every theme shows gentle baseline motion, not pitch black
- [ ] 7.4 Strobe specifically: flashes on loud transients, dim pulse otherwise (no static black)
- [ ] 7.5 Spectrum vs Rainbow: visibly distinct behavior (Spectrum tracks frequency content, Rainbow cycles)
- [x] 7.6 ktlint passes: `./gradlew ktlintCheck`
