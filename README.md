<p align="center">
  <img src="docs/beatflare_icon.jpg" alt="BeatFlare" width="128" />
</p>

<h1 align="center">BeatFlare</h1>

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
- **Brightness slider** -- adjust overall LED intensity
- **Zone toggles** -- enable/disable spectrum, bass, and beat zones independently
- **Party mode** -- full-screen front-of-phone color visualization synced to audio
- **Notification control** -- start/stop without opening the app

## Supported Devices

| Device | LEDs | Glyph visualization | Party mode |
|---|---|---|---|
| Nothing Phone (1) | 15 | Yes | Yes |
| Nothing Phone (2) | 33 | Yes | Yes |
| Nothing Phone (2a) | 26 | Yes | Yes |
| Nothing Phone (2a) Plus | 26 | Yes | Yes |
| Nothing Phone (3a) / (3a) Pro | 36 | Yes | Yes |
| Nothing Phone (4a) | 6 | Yes | Yes |
| Any Android 14+ device | -- | No | Yes |

## Installation

1. Download [`beatflare.apk`](release/beatflare.apk) from this repository
2. Transfer it to your phone and install (you may need to allow installs from unknown sources)
3. Enable glyph debug mode by connecting your phone via USB and running:
   ```
   adb shell settings put global nt_glyph_interface_debug_enable 1
   ```
   *(this expires after 48 hours -- re-run as needed)*
4. Open BeatFlare, grant microphone permission, and hit start

> **Want to build from source?** See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Support

If you enjoy BeatFlare and want to support its development:

[![Ko-fi](https://img.shields.io/badge/Ko--fi-F16061?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/stilkin)

## License

[PolyForm Noncommercial 1.0.0](LICENSE) -- free for personal and non-commercial use.
