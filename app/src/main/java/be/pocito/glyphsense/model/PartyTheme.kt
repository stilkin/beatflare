package be.pocito.glyphsense.model

import androidx.compose.ui.graphics.Color
import be.pocito.glyphsense.audio.AudioAnalysis

/**
 * Color themes for the front-screen party mode visualization.
 * Each theme defines its own color derivation from audio analysis data.
 */
enum class PartyTheme(val label: String) {

    SPECTRUM("Spectrum") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int): Color {
            val dominantIndex = analysis.spectrum.indices.maxByOrNull { analysis.spectrum[it] } ?: 0
            val hue = dominantIndex.toFloat() / analysis.spectrum.size.coerceAtLeast(1) * 360f
            val lightness = 0.10f + analysis.bassLevel * 0.55f
            val flash = if (beatFlash > 0) 0.30f else 0f
            return Color.hsl(hue, 0.85f, (lightness + flash).coerceAtMost(1f))
        }
    },

    FIRE("Fire") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int): Color {
            // Red (0°) → Orange (30°) → Yellow (50°), driven by bass
            val hue = 10f + analysis.bassLevel * 40f
            val lightness = 0.08f + analysis.bassLevel * 0.50f
            val flash = if (beatFlash > 0) 0.25f else 0f
            return Color.hsl(hue, 0.95f, (lightness + flash).coerceAtMost(1f))
        }
    },

    OCEAN("Ocean") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int): Color {
            // Blue (200°) → Cyan (180°) → Teal (170°), mid-freq shifts hue
            val midAvg = analysis.spectrum.slice(5..14).average().toFloat()
            val hue = 200f - midAvg * 40f
            val lightness = 0.08f + analysis.bassLevel * 0.45f
            val flash = if (beatFlash > 0) 0.25f else 0f
            return Color.hsl(hue, 0.80f, (lightness + flash).coerceAtMost(1f))
        }
    },

    MONOCHROME("Mono") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int): Color {
            val brightness = 0.03f + analysis.bassLevel * 0.60f
            val flash = if (beatFlash > 0) 0.30f else 0f
            val v = (brightness + flash).coerceIn(0f, 1f)
            return Color(v, v, v)
        }
    },

    RAINBOW("Rainbow") {
        private var hueOffset = 0f
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int): Color {
            hueOffset = if (analysis.beat) 0f else (hueOffset + 2f) % 360f
            val lightness = 0.10f + analysis.bassLevel * 0.50f
            val flash = if (beatFlash > 0) 0.25f else 0f
            return Color.hsl(hueOffset, 0.85f, (lightness + flash).coerceAtMost(1f))
        }
    },

    STROBE("Strobe") {
        override fun deriveColor(analysis: AudioAnalysis, beatFlash: Int): Color {
            return if (beatFlash > 0) Color.White else Color.Black
        }
    };

    abstract fun deriveColor(analysis: AudioAnalysis, beatFlash: Int): Color
}
