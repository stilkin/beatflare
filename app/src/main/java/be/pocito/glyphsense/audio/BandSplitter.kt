package be.pocito.glyphsense.audio

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Splits an FFT magnitude spectrum into:
 *  - [bassEnergy]: total energy in 20–250 Hz
 *  - [spectrum]: 20 log-spaced bands from [SPECTRUM_LOW_HZ] to [SPECTRUM_HIGH_HZ]
 *  - [transientEnergy]: total energy in 60–250 Hz (kick-drum range, for beat detection)
 *
 * The 20-band count matches the 20 LEDs in zone C.
 */
class BandSplitter(
    private val sampleRate: Int,
    private val fftSize: Int,
    private val spectrumBands: Int = 20,
) {
    companion object {
        const val BASS_LOW_HZ = 20f
        const val BASS_HIGH_HZ = 250f
        const val TRANSIENT_LOW_HZ = 60f
        const val TRANSIENT_HIGH_HZ = 250f
        const val SPECTRUM_LOW_HZ = 80f
        const val SPECTRUM_HIGH_HZ = 8_000f
    }

    private val binHz: Float = sampleRate.toFloat() / fftSize
    private val halfFft: Int = fftSize / 2

    private val bassStartBin = hzToBin(BASS_LOW_HZ)
    private val bassEndBin = hzToBin(BASS_HIGH_HZ)
    private val transientStartBin = hzToBin(TRANSIENT_LOW_HZ)
    private val transientEndBin = hzToBin(TRANSIENT_HIGH_HZ)

    /** Per-spectrum-band bin range: [startBin, endBin), log-spaced on frequency. */
    private val spectrumBandBins: Array<IntRange> = buildBands()

    /** Output: aggregated spectrum values (one per band). Re-used each call. */
    val spectrum = FloatArray(spectrumBands)

    var bassEnergy: Float = 0f
        private set
    var transientEnergy: Float = 0f
        private set

    fun process(magnitudes: FloatArray) {
        bassEnergy = sumRange(magnitudes, bassStartBin, bassEndBin)
        transientEnergy = sumRange(magnitudes, transientStartBin, transientEndBin)
        for (i in 0 until spectrumBands) {
            val range = spectrumBandBins[i]
            spectrum[i] = averageRange(magnitudes, range.first, range.last + 1)
        }
    }

    private fun hzToBin(hz: Float): Int =
        (hz / binHz).roundToInt().coerceIn(0, halfFft - 1)

    private fun sumRange(arr: FloatArray, start: Int, end: Int): Float {
        var s = 0f
        for (i in start until end) s += arr[i]
        return s
    }

    private fun averageRange(arr: FloatArray, start: Int, end: Int): Float {
        if (end <= start) return 0f
        var s = 0f
        for (i in start until end) s += arr[i]
        return s / (end - start)
    }

    private fun buildBands(): Array<IntRange> {
        // Log-spaced edges across [SPECTRUM_LOW_HZ, SPECTRUM_HIGH_HZ]
        val lnLow = ln(SPECTRUM_LOW_HZ.toDouble())
        val lnHigh = ln(SPECTRUM_HIGH_HZ.toDouble())
        val edges = IntArray(spectrumBands + 1)
        for (i in 0..spectrumBands) {
            val frac = i.toDouble() / spectrumBands
            val hz = Math.E.pow(lnLow + frac * (lnHigh - lnLow)).toFloat()
            edges[i] = hzToBin(hz)
        }
        return Array(spectrumBands) { i ->
            val start = edges[i]
            val end = maxOf(edges[i + 1] - 1, start)
            start..end
        }
    }
}
