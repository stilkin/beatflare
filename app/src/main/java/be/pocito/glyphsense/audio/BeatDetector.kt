package be.pocito.glyphsense.audio

/**
 * Simple energy-based beat detector.
 *
 * Tracks a running average of transient energy (EMA). When instantaneous energy
 * exceeds `avg * threshold`, emits a beat. A refractory period suppresses
 * double-triggers.
 *
 * Call [update] once per FFT frame.
 */
class BeatDetector(
    /** Multiplier over the EMA to fire. Typical: 1.3–1.8. */
    private val threshold: Float = 1.5f,
    /** EMA smoothing factor. Lower = longer memory. 0.05 ≈ 1-second memory at 22 fps. */
    private val emaAlpha: Float = 0.05f,
    /** Minimum time between beats, ms. */
    private val refractoryMs: Long = 120L,
) {
    private var ema: Float = 0f
    private var lastBeatNanos: Long = 0L

    /**
     * @return true if a beat was detected this frame.
     */
    fun update(instantaneousEnergy: Float, nowNanos: Long = System.nanoTime()): Boolean {
        // Warm-start EMA on first non-trivial sample
        if (ema == 0f) {
            ema = instantaneousEnergy
            return false
        }
        val sinceLastMs = (nowNanos - lastBeatNanos) / 1_000_000L
        val canFire = sinceLastMs >= refractoryMs
        val isBeat = canFire && instantaneousEnergy > ema * threshold
        // Update EMA with the instantaneous value (include beat frames)
        ema = ema + emaAlpha * (instantaneousEnergy - ema)
        if (isBeat) lastBeatNanos = nowNanos
        return isBeat
    }

    fun reset() {
        ema = 0f
        lastBeatNanos = 0L
    }
}
