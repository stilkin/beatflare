package be.pocito.glyphsense.glyph

import be.pocito.glyphsense.audio.AudioAnalysis
import kotlin.math.roundToInt

/**
 * Maps [AudioAnalysis] output onto the 36-LED layout of the Nothing Phone (3a).
 *
 * Zone layout (from SDK README):
 *  - Zone C: indices  0..19 (20 LEDs, long strip) — spectrum analyzer
 *  - Zone A: indices 20..30 (11 LEDs, vertical strip) — bass VU meter, bottom-up
 *  - Zone B: indices 31..35 ( 5 LEDs, small cluster) — beat flash
 *
 * All output values are in the range 0..[MAX_BRIGHTNESS]. The driver keeps a
 * little internal state for beat decay, so the flash fades out smoothly across
 * successive frames.
 */
class GlyphDriver(
    /** Multiplier 0..1 applied to all outputs — overall brightness control. */
    private val brightness: Float = 1.0f,
    /** Frames the beat flash takes to decay to near-zero. */
    private val beatDecayFrames: Int = 6,
) {
    companion object {
        const val MAX_BRIGHTNESS = 4095
        const val LED_COUNT = GlyphController.LED_COUNT_PHONE_3A

        // Zone ranges (inclusive start, exclusive end)
        const val ZONE_C_START = 0
        const val ZONE_C_END = 20          // 20 LEDs
        const val ZONE_A_START = 20
        const val ZONE_A_END = 31          // 11 LEDs
        const val ZONE_B_START = 31
        const val ZONE_B_END = 36          // 5 LEDs

        // Physical orientation within zones (from SDK README)
        // Zone A: A_1 (top) = idx 20, A_11 (bottom) = idx 30  → fill bottom-up
    }

    // Reused output buffer — no per-frame allocation.
    private val out = IntArray(LED_COUNT)

    /** Frames remaining on the beat flash (0 when idle). */
    private var beatFrames: Int = 0

    /**
     * Render one frame into an internal buffer and return it. The returned
     * array is mutated on the next call — do not retain it.
     */
    fun render(analysis: AudioAnalysis): IntArray {
        for (i in out.indices) out[i] = 0

        renderSpectrum(analysis.spectrum)
        renderBass(analysis.bassLevel)
        if (analysis.beat) beatFrames = beatDecayFrames
        renderBeat()
        if (beatFrames > 0) beatFrames--

        return out
    }

    /** Convenience: return an all-off frame (e.g. when visualization is paused). */
    fun blankFrame(): IntArray {
        for (i in out.indices) out[i] = 0
        return out
    }

    private fun renderSpectrum(spectrum: FloatArray) {
        // Direct 1:1 mapping: spectrum[0] (lowest freq) → idx 0, spectrum[19] → idx 19
        val n = minOf(spectrum.size, ZONE_C_END - ZONE_C_START)
        for (i in 0 until n) {
            out[ZONE_C_START + i] = scale(spectrum[i])
        }
    }

    private fun renderBass(bassLevel: Float) {
        // Bottom-up fill. A_11 (idx 30) is the bottom, A_1 (idx 20) is the top.
        val zoneLen = ZONE_A_END - ZONE_A_START // 11
        val lit = (bassLevel.coerceIn(0f, 1f) * zoneLen).roundToInt()
        val maxB = scale(1f)
        // Light the BOTTOM [lit] LEDs first. Bottom is idx 30, top is idx 20.
        for (k in 0 until lit) {
            val idx = ZONE_A_END - 1 - k // 30, 29, 28, ...
            out[idx] = maxB
        }
    }

    private fun renderBeat() {
        if (beatFrames <= 0) return
        // Decay from full brightness over [beatDecayFrames] frames.
        val fraction = beatFrames.toFloat() / beatDecayFrames
        val level = scale(fraction)
        for (i in ZONE_B_START until ZONE_B_END) {
            out[i] = level
        }
    }

    private fun scale(level: Float): Int {
        val v = (level * brightness * MAX_BRIGHTNESS).roundToInt()
        return v.coerceIn(0, MAX_BRIGHTNESS)
    }
}
