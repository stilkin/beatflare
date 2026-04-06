## 1. App Icon & Name

- [x] 1.1 Integrate app icon as raster mipmap at all densities (from beatflare_icon.jpg)
- [x] 1.2 Rename app to "BeatFlare" in strings.xml, notification, widget, README

## 2. Color Palette

- [x] 2.1 Define primary (#E91E8C magenta), secondary (#F4811E orange), surface (#1C1C1E), background (#0D0D0D) in Color.kt
- [x] 2.2 Update Theme.kt dark color scheme to use the new accent colors
- [x] 2.3 Apply magenta→orange gradient to spectrum bars (left=low freq, right=high freq)

## 3. Layout Restructure

- [x] 3.1 Promote spectrum bars + bass bar to hero position at top (large, in a dark card)
- [x] 3.2 Integrate beat indicator into the visualizer card (flash/pulse instead of separate box)
- [x] 3.3 Style start/stop as a large gradient-filled button (magenta when running)
- [x] 3.4 Style party mode button with orange accent
- [x] 3.5 Wrap settings (brightness, zone toggles) in a subtle dark card
- [x] 3.6 Replace "Visualizer: RUNNING/stopped" with a colored dot + text status
- [x] 3.7 Collapse debug values (raw/floor/peak) behind a tap

## 4. Widget & Notification

- [x] 4.1 Update widget_bg_running.xml to use the primary accent magenta
- [x] 4.2 Update notification accent color

## 5. Verification

- [ ] 5.1 Visual check on device — all accent colors consistent
- [ ] 5.2 Verify widget and notification match the new style
