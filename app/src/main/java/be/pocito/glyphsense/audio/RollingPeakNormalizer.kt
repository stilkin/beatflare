package be.pocito.glyphsense.audio

import kotlin.math.pow

/**
 * Dual-sided adaptive normalizer. Tracks both a rolling **peak** (signal ceiling)
 * and a rolling **floor** (noise baseline). Output = (value - floor) / (peak - floor).
 *
 * This gives us two important behaviors:
 *  - **Noise gate**: values at or below the floor normalize to 0 — ambient/typing noise
 *    doesn't max out the visualization.
 *  - **Adaptive range**: peak tracks the loudest recent sound; bars stay proportional
 *    whether you're at a festival or in a bedroom.
 *
 * The [minDynamicRange] parameter enforces a floor-to-peak minimum gap. Without it,
 * in a truly silent room the peak and floor converge and tiny fluctuations would
 * still max the bars. With it, small signal above noise must exceed an absolute
 * threshold to register.
 */
class RollingPeakNormalizer(
    /** Half-life of the peak's decay toward current value, in seconds. Longer = more stable. */
    peakHalfLifeSec: Float = 5f,
    /** Time constant for the floor rising when signal stays above it, in seconds. */
    floorRiseSec: Float = 20f,
    /** Expected update rate in Hz (used to compute per-update factors). */
    updateRateHz: Float = 22f,
    /** Minimum gap between floor and peak. Below this, bars stay small. Tune empirically. */
    private val minDynamicRange: Float = 1500f,
    /** Absolute minimum for [peak]. Prevents runaway sensitivity in silence. */
    private val minAbsolutePeak: Float = 2000f,
    /**
     * Absolute noise gate — values below this are treated as "silence" regardless of
     * the adaptive floor. Critical for quiet environments where the rolling floor
     * drops so low that typing/rustling would otherwise fill the bars.
     *
     * In loud environments (e.g. festivals) the tracked floor rises well above this,
     * so the gate has no effect and normalization stays fully adaptive.
     */
    private val absoluteFloor: Float = 0f,
) {
    private val peakDecayPerUpdate: Float = halfLifeToFactor(peakHalfLifeSec, updateRateHz)
    /** Per-update fraction that floor rises toward current value when value > floor. */
    private val floorRisePerUpdate: Float = timeConstantToAlpha(floorRiseSec, updateRateHz)

    private var peak: Float = minAbsolutePeak
    private var floor: Float = 0f
    private var initialized: Boolean = false

    fun normalize(value: Float): Float {
        if (!initialized) {
            peak = maxOf(value, minAbsolutePeak)
            floor = value * 0.5f
            initialized = true
        }
        // Peak: instant rise to new highs, exponential decay otherwise.
        peak = if (value > peak) value else peak * peakDecayPerUpdate
        if (peak < minAbsolutePeak) peak = minAbsolutePeak

        // Floor: instant drop to new lows, slow rise toward the current value.
        floor = if (value < floor) value
                else floor + (value - floor) * floorRisePerUpdate
        if (floor < 0f) floor = 0f

        // Clamp the floor up to the absolute gate — this is what makes typing
        // invisible in a quiet room while leaving festival audio fully dynamic.
        val effectiveFloor = maxOf(floor, absoluteFloor)
        // Enforce a minimum dynamic range so we don't amplify tiny fluctuations.
        val span = (peak - effectiveFloor).coerceAtLeast(minDynamicRange)
        val effectivePeak = effectiveFloor + span

        return ((value - effectiveFloor) / (effectivePeak - effectiveFloor)).coerceIn(0f, 1f)
    }

    fun currentPeak(): Float = peak
    fun currentFloor(): Float = floor

    fun reset() {
        peak = minAbsolutePeak
        floor = 0f
        initialized = false
    }

    companion object {
        /** Fraction per update such that after halfLifeSec * rateHz updates, value is halved. */
        private fun halfLifeToFactor(halfLifeSec: Float, rateHz: Float): Float {
            val n = halfLifeSec * rateHz
            return 0.5.pow(1.0 / n).toFloat()
        }
        /** EMA alpha achieving roughly-tau time constant. */
        private fun timeConstantToAlpha(tauSec: Float, rateHz: Float): Float =
            (1f / (tauSec * rateHz)).coerceIn(0.0001f, 1f)
    }
}
