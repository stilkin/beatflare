<p align="center">
  <img src="docs/beatflare_icon.svg" alt="BeatFlare" width="128" />
</p>

<h1 align="center">BeatFlare</h1>

<p align="center">
  <a href="https://developer.android.com/about/versions/14"><img src="https://img.shields.io/badge/Android-14%2B-3DDC84?logo=android&logoColor=white" alt="Android 14+" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-PolyForm%20NC%201.0-blue" alt="License" /></a>
  <a href="https://ko-fi.com/stilkin"><img src="https://img.shields.io/badge/Ko--fi-F16061?logo=ko-fi&logoColor=white" alt="Ko-fi" /></a>
</p>

<p align="center">
  A live music visualizer for <strong>Nothing Phones</strong> that drives the glyph LEDs from microphone audio.<br>
  Designed for concerts and festivals -- hold your phone face-down and watch the back light up in sync with the music.
</p>

---

## What it does

BeatFlare captures live ambient audio, runs real-time FFT analysis, and maps the frequency spectrum onto the glyph LEDs. Depending on your device, LEDs are split into zones for spectrum visualization, bass VU metering, and beat detection flashes.

The visualizer runs as a foreground service, so it keeps working when the screen is off -- ideal for saving battery at a festival while your phone's back pulses to the music.

### Features

- **Live microphone input** -- reacts to ambient sound around you, not just on-device playback
- **Adaptive volume** -- automatically adjusts from quiet room to festival stage
- **Works with screen off** -- lock your phone and the glyphs keep going (saves battery)
- **Tabbed UI** -- Beacon / Play / Show / Glyphs (the Glyphs tab is hidden on non-Nothing devices)
- **Beacon tab** -- a full-screen "find me" mode with a hue-only background, optional emoji or up to 4 characters of text in a chosen colour, and a React-to-sound toggle (works permission-free when off)
- **Output toggles** -- choose Glyphs, Show, or both, and Start activates whichever are enabled
- **Seven Show themes, grouped** -- Spectrum (Spectrum, Rainbow), Mood (Fire, Ocean), Pulse (Breathe, Sweep, Strobe), each with a one-line subtitle
- **Quiet-state pulse** -- every theme keeps gently breathing during silent passages instead of going black
- **Glyph zones + brightness** -- enable/disable spectrum, bass, and beat zones independently, and tune overall LED intensity
- **Notification control** -- start/stop without opening the app

## Supported Devices

| Device | LEDs | Glyph visualization | Show / Beacon |
|---|---|---|---|
| Nothing Phone (1) | 15 | Yes | Yes |
| Nothing Phone (2) | 33 | Yes | Yes |
| Nothing Phone (2a) | 26 | Yes | Yes |
| Nothing Phone (2a) Plus | 26 | Yes | Yes |
| Nothing Phone (3a) / (3a) Pro | 36 | Yes | Yes |
| Nothing Phone (4a) | 6 | Yes | Yes |
| Any Android 14+ device | -- | No | Yes |

## Installation

<a href="https://play.google.com/store/apps/details?id=be.pocito.glyphsense"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80"></a>

After installing from the Play Store:

1. **Enable the Glyph SDK on your phone** (Nothing phones on Android 14-15 only — not needed on Android 16+):
   Connect your phone via USB and run:
   ```
   adb shell settings put global nt_glyph_interface_debug_enable 1
   ```
   This grants third-party apps access to the glyph LEDs. It expires after 48 hours, so you'll need to re-run it periodically. On Android 16+ this restriction was removed by Nothing and the step can be skipped.
2. Open BeatFlare, grant microphone permission, and hit start

> **Want to build from source?** See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Support

If you enjoy BeatFlare and want to support its development:

[![Ko-fi](https://img.shields.io/badge/Ko--fi-F16061?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/stilkin)

## License

[PolyForm Noncommercial 1.0.0](LICENSE) -- free for personal and non-commercial use.
