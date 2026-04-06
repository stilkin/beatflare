## Why

The current UI is functional but visually plain — default Material buttons, debug-style text, no color identity, and a "settings page" feel. For a music/festival app, the UI should feel energetic and polished. This change redesigns the main Activity layout, introduces an accent color scheme (derived from the app icon), adds a proper app icon, and optionally renames the product.

## What Changes

- Redesign MainActivity layout: replace flat text + dividers with cards, color accents, and visual hierarchy
- Replace default Material buttons with styled, branded alternatives
- Move the analysis debug info (raw/floor/peak log values) into a collapsible debug section — keep spectrum bars and beat indicator visible but styled
- Add a custom app icon (adaptive icon with vector drawable)
- Establish an accent color palette derived from the icon
- Apply accent colors to: start/stop button, party mode button, spectrum bars, beat indicator, slider, status indicator
- Consider product name (if changing from GlyphSense)
- Style the notification to match the new identity
- Style the widget to match the new identity

## Capabilities

### New Capabilities
- `app-branding`: App icon (adaptive vector), accent color palette, and visual identity applied consistently across the app, notification, and widget

### Modified Capabilities
- `front-screen-viz`: Spectrum bars and beat indicator styled with accent colors instead of default Material/cyan
- `widget-toggle`: Widget visual style updated to match the new brand identity

## Impact

- **Visual only**: No functional changes to the audio pipeline, glyph driver, or service
- **Files affected**: MainActivity.kt (layout restructure), Theme.kt/Color.kt (accent palette), widget layout XML, drawable resources (icon), possibly notification builder
- **Risk**: Low — purely cosmetic. If the icon isn't ready yet, the rest of the polish can proceed with placeholder colors
- **Dependency**: The accent color palette ideally comes from the app icon, so the icon should be designed first (or at least color-picked)
