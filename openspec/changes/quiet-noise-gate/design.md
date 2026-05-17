## Context

`AudioAnalyzer` already runs each channel through a `RollingPeakNormalizer` with an `absoluteFloor` and a `minDynamicRange`. In near-silence the normalizer floor and peak end up close together, so tiny fluctuations in mic hiss (or DSP rounding) get amplified to small but visible LED activity. The quiet-state baseline pulse in `PartyTheme` only takes over when `audioLevel == 0`, so output in the silent dead zone is "almost zero, but jittery" rather than "clean zero, breathing".

Two places we could intervene:

1. **Inside `RollingPeakNormalizer`** — gate the normalized output near zero.
2. **In `AudioAnalyzer`** — apply a single output gate based on a summary level (max of bass + spectrum bands).

We prefer (2) because the dead zone is a *frame-level* phenomenon: when overall energy is low we want *all* channels to read zero, not just the ones that happen to dip below the per-channel threshold. A summary gate also keeps the per-band normalizers untouched, so we don't risk regressing the loud-room behaviour.

## Goals / Non-Goals

**Goals:**
- Eliminate visible LED jitter and stray beat flashes during near-silence.
- Let the existing quiet pulse in `PartyTheme` own the "silent" visual state.
- Keep the loud/festival behaviour unchanged.
- One small, focused change — easy to revert if it over-gates.

**Non-Goals:**
- No new user-facing setting for the gate threshold (use sensible defaults).
- No changes to FFT, beat detector, or band splitter.
- Not addressing UI/tab restructuring (that ships separately as `beacon-tab-rework`).

## Decisions

### Where the gate lives

Add the gate at the end of `AudioAnalyzer.process()`, just before constructing `AudioAnalysis`. The gate looks at a single "frame level" derived from the already-normalized bass + spectrum and, when below threshold, zeros every output (bass, spectrum, beat).

Rationale: keeps the gating logic in one place, easy to test, easy to remove. Doesn't entangle with the normalizer's internal state.

### Threshold and hysteresis

- **Open threshold** (gate becomes transparent): frame level >= 0.10
- **Close threshold** (gate forces zero): frame level <= 0.06

Frame level = `max(bassLevel, max(spectrum))`. Using `max` (rather than mean) means a single active band can hold the gate open — important for sparse signals like a bass note over silence.

Hysteresis prevents chattering at the boundary. The gap (0.06–0.10) is wide enough that ordinary mic noise can't oscillate across it, but narrow enough that real signal entering or leaving doesn't feel laggy.

Alternative considered: a single hard threshold at 0.08. Rejected — measured during testing this still chatters at the edge with consistent low ambient noise.

### Beat handling

When the gate is closed, force `beat = false`. Beat detection in near-silence is almost certainly a false positive, and even one true beat flash mid-pulse would interrupt the quiet pulse's calmness.

### State

A single `Boolean` field on `AudioAnalyzer` tracking "gate open / closed". Initialized closed so the very first quiet frame is gated; opens when the open threshold is crossed.

## Risks / Trade-offs

- [Risk: A faint but musically meaningful signal — e.g. distant background music — gets gated and the viz looks dead] → Mitigation: thresholds chosen against normalized output (0..1), not raw audio. By the time something is "audible enough to want to visualize", the normalizer should have pushed it well above 0.10.
- [Risk: A slowly fading-out song hits the gate edge and the last few seconds become jerky] → Mitigation: hysteresis. Once the gate is open, it only closes when level drops below 0.06, so a smooth fade-out drops cleanly through both thresholds.
- [Risk: First few buffers after start-up are gated → user sees "nothing happens"] → Mitigation: the normalizer already produces small non-zero outputs during its warm-up; if it doesn't reach 0.10 within a second or two the quiet pulse will be visible anyway, which is the correct fallback.

## Migration Plan

Code-only change. No persisted state, no settings, no UI. Rollback = revert the commit.

## Open Questions

None. Threshold values are starting points — we'll confirm on-device with the test group and adjust constants if needed.
