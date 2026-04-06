## Context

The current UI uses default Material 3 styling — white-on-black, stock buttons, debug-style text. For a music/festival app, the visual identity should feel energetic and polished. The user is designing an app icon; accent colors will be derived from it.

## Goals / Non-Goals

**Goals:**
- Establish a visual identity (accent colors, styled components)
- Improve layout hierarchy — cards, spacing, visual grouping
- Custom adaptive app icon
- Move debug info (log values) into a collapsible section
- Consistent styling across app, notification, and widget

**Non-Goals:**
- Custom animations or transitions (keep it fast and simple)
- Dark/light theme toggle (dark only — matches Nothing Phone aesthetic and festival use)
- Custom font (system font is fine)

## Decisions

### 1. Color palette

Derived from the app icon once designed. Placeholder structure:
- **Primary accent**: Used for active/start button, spectrum bars, progress indicators
- **Secondary accent**: Used for beat flash, party mode button
- **Surface**: Dark card backgrounds (slight elevation over pure black)
- **On-surface**: White/light text

### 2. Layout restructure

```
┌─────────────────────────────────────────┐
│  App title + animated status indicator  │
├─────────────────────────────────────────┤
│  ┌───────────────────────────────────┐  │
│  │  Start / Stop (large, prominent)  │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  Settings card                    │  │
│  │  Brightness slider                │  │
│  │  Zone toggles (row)               │  │
│  │  Theme selector                   │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  Party mode button                │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  Live visualizer card             │  │
│  │  Spectrum bars (styled)           │  │
│  │  Bass bar + beat indicator        │  │
│  │  ▸ Debug values (collapsed)       │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### 3. App icon

Adaptive icon (Android standard): separate foreground + background layers.
Foreground: stylized waveform or pulse graphic as vector drawable.
Background: solid accent color.

### 4. Widget restyle

Match the new accent colors. Use the same primary accent for the "running" state background. Clean, minimal.

## Risks / Trade-offs

- **Icon design**: Limited to what we can express as Android vector drawables. Complex gradients or photorealistic elements aren't possible — geometric/line-art style works best.
- **Color dependency**: If the icon design changes, accent colors need updating. Using a centralized Color.kt makes this a single-file change.
- **Accessibility**: Ensure accent colors have sufficient contrast against dark backgrounds (WCAG AA minimum).
