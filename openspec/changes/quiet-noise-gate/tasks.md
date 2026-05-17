## 1. Implement the gate

- [x] 1.1 Add `OPEN_THRESHOLD = 0.10f` and `CLOSE_THRESHOLD = 0.06f` constants in `AudioAnalyzer` (companion object)
- [x] 1.2 Add a `gateOpen: Boolean` field in `AudioAnalyzer`, initialized to `false`
- [x] 1.3 At the end of `process()` after normalization, compute `frameLevel = max(bass, spectrum.max())`
- [x] 1.4 Update `gateOpen`: set to `true` when `frameLevel >= OPEN_THRESHOLD`, `false` when `frameLevel <= CLOSE_THRESHOLD`; otherwise keep current value
- [x] 1.5 When `gateOpen` is `false`, return an `AudioAnalysis` with `bassLevel = 0f`, a zeroed spectrum array, and `beat = false` (preserve `bassRaw`/`bassFloor`/`bassPeak` for debug UI)

## 2. Validate

- [x] 2.1 Build the debug APK: `./gradlew ktlintCheck :app:assembleDebug`
- [x] 2.2 Install on the Nothing Phone (3a) and run with the room silent — confirm glyphs stop flickering and the quiet pulse takes over cleanly
- [ ] 2.3 Play music at moderate volume — confirm full visualization still works (gate opens promptly, no perceptible lag)
- [ ] 2.4 Fade music out — confirm the transition into silence is smooth (no jerky edge), the gate closes within ~1s of true silence
