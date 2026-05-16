package be.pocito.glyphsense.model

import androidx.compose.ui.graphics.Color
import be.pocito.glyphsense.audio.AudioAnalysis
import kotlin.math.PI
import kotlin.math.sin

/**
 * Color themes for the front-screen party mode visualization.
 * Each theme defines its own color derivation from audio analysis data and wall-clock time.
 */
enum class PartyTheme(val label: String) {

    SPECTRUM("Spectrum") {
        // Smoothed frequency centroid 0..1 (low → high band). EMA across frames so the
        // colour doesn't jitter on noisy single-band peaks.
        private var smoothedCentroid: Float = 0.5f

        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color {
            val centroid = weightedCentroid(analysis.spectrum)
            smoothedCentroid += 0.3f * (centroid - smoothedCentroid)
            // Map 0..1 → 0°..280° (red → violet). Skips the wrap back to red so the
            // perceived spread covers the full visible spectrum without ambiguity.
            val hue = smoothedCentroid * 280f
            val lightness = 0.10f + analysis.bassLevel * 0.55f
            val flash = if (beatFlash > 0) 0.30f else 0f
            val baseline = quietPulse(nowMs, analysis.bassLevel) * 0.10f
            return Color.hsl(hue, 0.85f, (lightness + flash + baseline).coerceAtMost(1f))
        }
    },

    FIRE("Fire") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color {
            // Red (0°) → Orange (30°) → Yellow (50°), driven by bass
            val hue = 10f + analysis.bassLevel * 40f
            val lightness = 0.08f + analysis.bassLevel * 0.50f
            val flash = if (beatFlash > 0) 0.25f else 0f
            val baseline = quietPulse(nowMs, analysis.bassLevel) * 0.10f
            return Color.hsl(hue, 0.95f, (lightness + flash + baseline).coerceAtMost(1f))
        }
    },

    OCEAN("Ocean") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color {
            // Blue (200°) → Cyan (180°) → Teal (170°), mid-freq shifts hue
            val midAvg = analysis.spectrum.slice(5..14).average().toFloat()
            val hue = 200f - midAvg * 40f
            val lightness = 0.08f + analysis.bassLevel * 0.45f
            val flash = if (beatFlash > 0) 0.25f else 0f
            val baseline = quietPulse(nowMs, analysis.bassLevel) * 0.10f
            return Color.hsl(hue, 0.80f, (lightness + flash + baseline).coerceAtMost(1f))
        }
    },

    MONOCHROME("Mono") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color {
            val brightness = 0.03f + analysis.bassLevel * 0.60f
            val flash = if (beatFlash > 0) 0.30f else 0f
            val baseline = quietPulse(nowMs, analysis.bassLevel) * 0.10f
            val v = (brightness + flash + baseline).coerceIn(0f, 1f)
            return Color(v, v, v)
        }
    },

    RAINBOW("Rainbow") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color {
            // 8s full hue cycle, time-driven (no per-frame accumulator).
            val hue = ((nowMs % 8000L) / 8000f) * 360f
            val lightness = 0.25f + analysis.bassLevel * 0.65f
            val flash = if (beatFlash > 0) 0.20f else 0f
            val baseline = quietPulse(nowMs, analysis.bassLevel) * 0.05f
            return Color.hsl(hue, 0.85f, (lightness + flash + baseline).coerceAtMost(1f))
        }
    },

    STROBE("Strobe") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color {
            // Transient = detected beat OR bass running hot relative to its rolling peak.
            // 0.6 of the normalized 0..1 bass level reads as "loud right now."
            val transient = beatFlash > 0 || analysis.bassLevel > 0.6f
            if (transient) return Color.White
            // Quiet state: dim white pulse so the screen never looks frozen.
            val dim = 0.05f +
                analysis.bassLevel * 0.10f +
                quietPulse(nowMs, analysis.bassLevel) * 0.05f
            val v = dim.coerceIn(0f, 1f)
            return Color(v, v, v)
        }
    },

    BREATHE("Breathe") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color {
            // 4s breathing period, fixed hue. Calm/meditative mode.
            val phase = (nowMs % 4000L) / 4000f
            val breath = 0.5f + 0.5f * sin(phase * 2f * PI.toFloat())
            val lightness = 0.15f + breath * 0.25f + analysis.bassLevel * 0.40f
            return Color.hsl(280f, 0.70f, lightness.coerceIn(0f, 1f))
        }
    },

    SWEEP("Sweep") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color {
            // Slow back-and-forth sweep across the cool half of the wheel (blue ↔ magenta).
            val phase = (nowMs % 12000L) / 12000f
            val swing = 0.5f + 0.5f * sin(phase * 2f * PI.toFloat())
            val hue = 200f + swing * 120f
            val saturation = (0.55f + analysis.bassLevel * 0.45f).coerceIn(0f, 1f)
            val lightness = 0.20f + analysis.bassLevel * 0.45f
            val baseline = quietPulse(nowMs, analysis.bassLevel) * 0.05f
            return Color.hsl(hue, saturation, (lightness + baseline).coerceAtMost(1f))
        }
    };

    abstract fun deriveColor(analysis: AudioAnalysis, beatFlash: Int, nowMs: Long): Color

    companion object {
        /**
         * Slow ambient pulse, 0..1. Period ~2s. Fades out as audio rises so it only
         * shows during quiet passages — prevents themes from sitting on pitch black.
         */
        fun quietPulse(nowMs: Long, audioLevel: Float): Float {
            val phase = (nowMs % 4000L) / 4000f
            val sine = 0.5f + 0.5f * sin(phase * 2f * PI.toFloat())
            return sine * (1f - audioLevel.coerceIn(0f, 1f))
        }

        private fun weightedCentroid(spectrum: FloatArray): Float {
            if (spectrum.isEmpty()) return 0.5f
            var num = 0f
            var den = 0f
            for (i in spectrum.indices) {
                num += i * spectrum[i]
                den += spectrum[i]
            }
            return if (den > 0.001f) (num / den) / (spectrum.size - 1) else 0.5f
        }
    }
}
