package be.pocito.glyphsense.audio

import kotlin.math.ln

/**
 * One analysis frame: normalized values ready for the glyph driver.
 *
 * @property bassLevel 0..1, normalized bass energy (20-250 Hz)
 * @property spectrum 20 values, each 0..1, log-spaced spectrum (80 Hz to 8 kHz)
 * @property beat true on the frame a beat was detected
 */
data class AudioAnalysis(
    val bassLevel: Float,
    val spectrum: FloatArray,
    val beat: Boolean,
    /** Debug/tuning values for the bass channel: raw energy, tracked floor, tracked peak. */
    val bassRaw: Float = 0f,
    val bassFloor: Float = 0f,
    val bassPeak: Float = 0f,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioAnalysis) return false
        return bassLevel == other.bassLevel &&
            beat == other.beat &&
            spectrum.contentEquals(other.spectrum)
    }

    override fun hashCode(): Int {
        var result = bassLevel.hashCode()
        result = 31 * result + spectrum.contentHashCode()
        result = 31 * result + beat.hashCode()
        return result
    }
}

/**
 * Composes FFT + band split + beat detect + per-channel normalization
 * into a single pipeline that converts raw PCM buffers into [AudioAnalysis].
 *
 * Not thread-safe — use from a single coroutine.
 */
class AudioAnalyzer(
    sampleRate: Int = AudioCapture.SAMPLE_RATE,
    private val fftSize: Int = AudioCapture.DEFAULT_BUFFER_SAMPLES,
    spectrumBands: Int = 20,
) {
    private val fft = Fft(fftSize)
    private val splitter = BandSplitter(sampleRate, fftSize, spectrumBands)
    private val beatDetector = BeatDetector()

    // Update rate ≈ sampleRate / fftSize (each buffer is one FFT)
    private val updateRate: Float = sampleRate.toFloat() / fftSize

    // All normalizers operate on log-scale energy (ln(1 + rawEnergy)) so that
    // a ~100 millionfold dynamic range collapses into ~11 log units.
    // minDynamicRange of 6 log units ≈ 26 dB — keeps small variations from
    // filling the bars in quiet environments.
    private val bassNormalizer = RollingPeakNormalizer(
        peakHalfLifeSec = 5f,
        floorRiseSec = 20f,
        updateRateHz = updateRate,
        minDynamicRange = 4f,
        minAbsolutePeak = 12f,
        // Absolute noise gate at ln(60k) ≈ 11. Typing peaks around here in a
        // quiet room, so it gets gated. Festival ambient is >>11 log units.
        absoluteFloor = 11f,
    )
    private val spectrumNormalizers = Array(spectrumBands) {
        RollingPeakNormalizer(
            peakHalfLifeSec = 3f,
            floorRiseSec = 15f,
            updateRateHz = updateRate,
            minDynamicRange = 3f,
            minAbsolutePeak = 10f,
            absoluteFloor = 8f,
        )
    }

    // Reusable output buffer so we don't allocate every frame.
    private val outSpectrum = FloatArray(spectrumBands)

    /**
     * Process one PCM buffer. Returns a fresh [AudioAnalysis] suitable for
     * passing to the LED driver. The returned [AudioAnalysis.spectrum] is a
     * defensive copy — safe to retain.
     */
    fun process(buffer: ShortArray): AudioAnalysis {
        fft.compute(buffer)
        splitter.process(fft.magnitudes)

        val bassRaw = splitter.bassEnergy
        val bassLog = ln(1f + bassRaw)
        val bass = bassNormalizer.normalize(bassLog)
        for (i in outSpectrum.indices) {
            val logVal = ln(1f + splitter.spectrum[i])
            outSpectrum[i] = spectrumNormalizers[i].normalize(logVal)
        }
        // Beat detector runs on log-scale transient energy too, so thresholds stay sensible.
        val beat = beatDetector.update(ln(1f + splitter.transientEnergy))

        return AudioAnalysis(
            bassLevel = bass,
            spectrum = outSpectrum.copyOf(),
            beat = beat,
            bassRaw = bassLog, // report log value now; floor/peak are also in log space
            bassFloor = bassNormalizer.currentFloor(),
            bassPeak = bassNormalizer.currentPeak(),
        )
    }
}
