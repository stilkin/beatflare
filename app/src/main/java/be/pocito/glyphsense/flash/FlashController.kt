package be.pocito.glyphsense.flash

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import be.pocito.glyphsense.audio.AudioAnalysis
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Drives the rear camera torch ("flash") from the audio analysis — a single coarse
 * "one-LED glyph". Uses [CameraManager] torch control, which needs no CAMERA permission
 * and no open camera session.
 *
 * Capability is probed once at construction. Where the hardware reports more than one
 * strength level the torch dims smoothly; otherwise it falls back to on/off strobing.
 */
class FlashController(context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId: String? = findFlashCamera(cameraManager)

    /** True when this device has a torch we can drive. */
    val available: Boolean = cameraId != null

    private val maxLevel: Int = cameraId?.let { maxTorchLevel(cameraManager, it) } ?: 1
    private val smooth: Boolean = maxLevel > 1

    // Beat-boost decay state, mirroring GlyphDriver's beatFrames.
    private var beatFrames = 0

    // Last written torch state, for skip-if-unchanged.
    private var lastLevel = -1 // smooth mode: last strength level (0 = off, -1 = never written)
    private var lastOn = false // on/off mode
    private var torchOn = false // tracks any-mode on/off, for stop() idempotency
    private var lastWriteNanos = 0L

    /**
     * Map one analysis frame to the torch. [intensity] (0..1) is the user's ceiling.
     * No quiet-state floor: the torch rests off in silence to save battery.
     */
    fun render(analysis: AudioAnalysis, intensity: Float) {
        val id = cameraId ?: return

        // Run the beat decay every frame (independent of the torch write rate).
        if (analysis.beat) beatFrames = BEAT_DECAY_FRAMES
        val beatBoost = beatFrames.toFloat() / BEAT_DECAY_FRAMES
        if (beatFrames > 0) beatFrames--
        val target = (max(analysis.bassLevel, beatBoost) * intensity).coerceIn(0f, 1f)

        // Rate cap — torch hardware and battery don't need analysis-rate updates.
        val now = System.nanoTime()
        if (now - lastWriteNanos < MIN_WRITE_INTERVAL_NANOS) return

        if (smooth) renderSmooth(id, target) else renderOnOff(id, target)
        lastWriteNanos = now
    }

    private fun renderSmooth(id: String, target: Float) {
        val level = if (target < OFF_THRESHOLD) 0 else (target * maxLevel).roundToInt().coerceIn(1, maxLevel)
        if (level == lastLevel) return
        try {
            if (level == 0) {
                cameraManager.setTorchMode(id, false)
                torchOn = false
            } else {
                cameraManager.turnOnTorchWithStrengthLevel(id, level)
                torchOn = true
            }
            lastLevel = level
        } catch (e: CameraAccessException) {
            Log.w(TAG, "torch write failed: ${e.message}")
        }
    }

    private fun renderOnOff(id: String, target: Float) {
        // Hysteresis so a level hovering near the threshold doesn't chatter.
        val on = if (lastOn) target > OFF_HYSTERESIS else target > ON_THRESHOLD
        if (on == lastOn) return
        try {
            cameraManager.setTorchMode(id, on)
            lastOn = on
            torchOn = on
        } catch (e: CameraAccessException) {
            Log.w(TAG, "torch write failed: ${e.message}")
        }
    }

    /** Turn the torch off. Idempotent; safe to call on every disable/stop/destroy. */
    fun stop() {
        val id = cameraId ?: return
        if (!torchOn && lastLevel <= 0 && !lastOn) return // already off
        try {
            cameraManager.setTorchMode(id, false)
        } catch (e: CameraAccessException) {
            Log.w(TAG, "torch off failed: ${e.message}")
        }
        torchOn = false
        lastLevel = 0
        lastOn = false
    }

    companion object {
        private const val TAG = "FlashController"
        private const val BEAT_DECAY_FRAMES = 6
        private const val OFF_THRESHOLD = 0.04f // smooth mode rests off below this
        private const val ON_THRESHOLD = 0.5f // on/off: turn on above this
        private const val OFF_HYSTERESIS = 0.35f // on/off: stay on until below this
        private const val MIN_WRITE_INTERVAL_NANOS = 33_000_000L // ~30 Hz cap

        /** Lightweight capability probe for UI gating (no controller instance needed). */
        fun isAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return false
            return findFlashCamera(cm) != null
        }

        private fun findFlashCamera(cm: CameraManager): String? = try {
            cm.cameraIdList.firstOrNull { id ->
                cm.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: CameraAccessException) {
            Log.w(TAG, "camera enumeration failed: ${e.message}")
            null
        }

        private fun maxTorchLevel(cm: CameraManager, id: String): Int = try {
            cm.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
        } catch (e: CameraAccessException) {
            1
        }
    }
}
