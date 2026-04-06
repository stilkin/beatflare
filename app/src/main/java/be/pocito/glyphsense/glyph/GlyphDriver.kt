package be.pocito.glyphsense.glyph

import be.pocito.glyphsense.audio.AudioAnalysis
import be.pocito.glyphsense.model.VisualizerSettings
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
    /** Frames the beat flash takes to decay to near-zero. */
    private val beatDecayFrames: Int = 6,
) {
    companion object {
        const val MAX_BRIGHTNESS = 4095
        const val LED_COUNT = GlyphController.LED_COUNT_PHONE_3A

        const val ZONE_C_START = 0
        const val ZONE_C_END = 20
        const val ZONE_A_START = 20
        const val ZONE_A_END = 31
        const val ZONE_B_START = 31
        const val ZONE_B_END = 36
    }

    private val out = IntArray(LED_COUNT)

    /** Frames remaining on the beat flash (0 when idle). */
    private var beatFrames: Int = 0

    /**
     * Render one frame. The returned array is mutated on the next call — do not retain.
     */
    fun render(analysis: AudioAnalysis, settings: VisualizerSettings = VisualizerSettings()): IntArray {
        for (i in out.indices) out[i] = 0

        if (settings.zoneCEnabled) renderSpectrum(analysis.spectrum, settings.brightness)
        if (settings.zoneAEnabled) renderBass(analysis.bassLevel, settings.brightness)
        if (analysis.beat) beatFrames = beatDecayFrames
        if (settings.zoneBEnabled) renderBeat(settings.brightness)
        if (beatFrames > 0) beatFrames--

        return out
    }

    /** Return an all-off frame (e.g. when visualization is paused). */
    fun blankFrame(): IntArray {
        for (i in out.indices) out[i] = 0
        return out
    }

    private fun renderSpectrum(spectrum: FloatArray, brightness: Float) {
        val n = minOf(spectrum.size, ZONE_C_END - ZONE_C_START)
        for (i in 0 until n) {
            out[ZONE_C_START + i] = scale(spectrum[i], brightness)
        }
    }

    private fun renderBass(bassLevel: Float, brightness: Float) {
        val zoneLen = ZONE_A_END - ZONE_A_START
        val lit = (bassLevel.coerceIn(0f, 1f) * zoneLen).roundToInt()
        val maxB = scale(1f, brightness)
        for (k in 0 until lit) {
            val idx = ZONE_A_END - 1 - k
            out[idx] = maxB
        }
    }

    private fun renderBeat(brightness: Float) {
        if (beatFrames <= 0) return
        val fraction = beatFrames.toFloat() / beatDecayFrames
        val level = scale(fraction, brightness)
        for (i in ZONE_B_START until ZONE_B_END) {
            out[i] = level
        }
    }

    private fun scale(level: Float, brightness: Float): Int {
        val v = (level * brightness * MAX_BRIGHTNESS).roundToInt()
        return v.coerceIn(0, MAX_BRIGHTNESS)
    }
}
