## 1. Project Setup

- [x] 1.1 Set up Android Studio project (Kotlin, Compose, min SDK 34) with proper package structure
- [x] 1.2 Integrate Glyph SDK AAR into the project (add to libs, configure Gradle dependency)
- [x] 1.3 Add AndroidManifest permissions (RECORD_AUDIO, FOREGROUND_SERVICE, com.nothing.ketchum.permission.ENABLE) and NothingKey meta-data

## 2. Glyph SDK Spike

- [x] 2.1 Create a minimal test Activity that initializes GlyphManager, opens a session, and toggles a single LED on/off
- [x] 2.2 Test `setFrameColors(IntArray)` with varying brightness values to determine the valid brightness range
- [x] 2.3 Measure max achievable update rate by looping setFrameColors calls and timing — document the result

## 3. Audio Capture Pipeline

- [x] 3.1 Implement RECORD_AUDIO runtime permission request flow with explanation UI on denial
- [x] 3.2 Create AudioCapture class: initializes AudioRecord (44100 Hz, mono, PCM 16-bit), reads buffers of 2048 samples in a coroutine loop
- [x] 3.3 Implement FFT processor that takes a 2048-sample buffer and outputs frequency magnitudes
- [x] 3.4 Implement frequency band splitter: extract bass energy (20–250 Hz), 20 spectrum sub-bands (for zone C), and transient energy for beat detection
- [x] 3.5 Implement beat detector using transient energy threshold crossing
- [x] 3.6 Implement rolling peak normalizer with exponential decay (5s window, ~3s half-life)

## 4. Glyph Visualization Driver

- [ ] 4.1 Create GlyphDriver class that maps band data to IntArray(36) brightness values using the zone layout (C=spectrum, A=bass, B=beats)
- [ ] 4.2 Implement zone C mapping: 20 spectrum sub-band values → 20 LED brightness levels
- [ ] 4.3 Implement zone A mapping: bass energy → bottom-up fill of 11 LEDs
- [ ] 4.4 Implement zone B mapping: beat detection → flash all 5 LEDs with decay
- [ ] 4.5 Wire GlyphDriver to GlyphManager.setFrameColors() with frame timing at target fps

## 5. Foreground Service

- [ ] 5.1 Create GlyphSenseService as a foreground service with persistent notification (status + stop action)
- [ ] 5.2 Wire AudioCapture → FFT → BandSplitter → Normalizer → GlyphDriver pipeline inside the service
- [ ] 5.3 Expose band data via SharedFlow for front-screen visualization to observe
- [ ] 5.4 Handle service lifecycle: start/stop audio capture and glyph session, release resources on destroy

## 6. Main UI (Controls)

- [ ] 6.1 Create MainActivity with Compose UI: start/stop button, service status indicator
- [ ] 6.2 Bind to GlyphSenseService and observe running state
- [ ] 6.3 Add sensitivity/mode controls if applicable after spike results

## 7. Front-Screen Visualization (Stretch)

- [ ] 7.1 Create a full-screen Compose Canvas that renders a color wash (hue from dominant frequency, brightness from amplitude)
- [ ] 7.2 Add party mode toggle button in the main UI
- [ ] 7.3 Observe SharedFlow band data from service and drive the color visualization

## 8. Home Screen Widget (Stretch)

- [ ] 8.1 Create an AppWidgetProvider with a simple toggle button layout
- [ ] 8.2 Implement tap-to-start/stop: send intent to GlyphSenseService
- [ ] 8.3 Update widget state when service starts/stops (via broadcast)
