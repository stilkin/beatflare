## 1. Device Profile Model

- [ ] 1.1 Create DeviceProfile data class: ledCount, spectrumRange, bassRange (nullable), beatRange (nullable)
- [ ] 1.2 Define profiles for all supported models: Phone (1), (2), (2a), (2a) Plus, (3a)/(3a) Pro, (4a)
- [ ] 1.3 Add profile detection function that maps Common.isXXXXX() to the correct profile

## 2. GlyphDriver Adaptation

- [ ] 2.1 Refactor GlyphDriver constructor to accept a DeviceProfile instead of hardcoded constants
- [ ] 2.2 Adapt renderSpectrum to use profile.spectrumRange with variable LED count
- [ ] 2.3 Adapt renderBass to skip when profile.bassRange is null, use variable LED count when present
- [ ] 2.4 Adapt renderBeat to skip when profile.beatRange is null, use variable LED count when present
- [ ] 2.5 Update GlyphSenseService to detect device and pass the correct profile to GlyphDriver

## 3. Audio Pipeline Adaptation

- [ ] 3.1 Make BandSplitter spectrum band count configurable (constructor param instead of hardcoded 20)
- [ ] 3.2 Make AudioAnalyzer accept a spectrumBands parameter and propagate to BandSplitter and normalizers
- [ ] 3.3 Wire the spectrum band count from the device profile through the service to the analyzer

## 4. Verification

- [ ] 4.1 Verify Phone (3a) still works identically (regression test)
- [ ] 4.2 Verify build compiles cleanly with all profile definitions
