## 1. App Icon

- [ ] 1.1 Create or integrate the app icon as adaptive icon (foreground + background vector drawables)
- [ ] 1.2 Update mipmap references in AndroidManifest if needed

## 2. Color Palette

- [ ] 2.1 Define primary and secondary accent colors in Color.kt (derived from icon)
- [ ] 2.2 Update Theme.kt to use the new accent colors in the Material color scheme
- [ ] 2.3 Replace hardcoded Color(0xFF4FC3F7) in spectrum bars with theme primary

## 3. Layout Restructure

- [ ] 3.1 Wrap settings controls (brightness, zones, theme) in a Card composable
- [ ] 3.2 Wrap analysis display (spectrum, bass, beat) in a Card composable
- [ ] 3.3 Style start/stop button with accent color (filled when running, outlined when stopped)
- [ ] 3.4 Style party mode button with secondary accent
- [ ] 3.5 Move debug log values (raw/floor/peak) into a collapsible section within the analysis card
- [ ] 3.6 Improve status indicator — replace plain text with a styled chip or dot + text

## 4. Widget & Notification

- [ ] 4.1 Update widget_bg_running.xml to use the primary accent color
- [ ] 4.2 Update notification builder to use accent color tint if applicable

## 5. Verification

- [ ] 5.1 Visual check on device — all accent colors consistent
- [ ] 5.2 Verify widget and notification match the new style
