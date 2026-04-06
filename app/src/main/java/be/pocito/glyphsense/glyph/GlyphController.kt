package be.pocito.glyphsense.glyph

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphManager

/**
 * Thin wrapper around [GlyphManager] that handles the service binding lifecycle.
 *
 * Usage:
 * ```
 * controller.init { onReady ->
 *   // session is open, safe to call setFrameColors / turnOff
 * }
 * ...
 * controller.release()
 * ```
 *
 * For the Phone (3a) there are 36 LEDs, indexed 0..35 per the SDK README:
 *  - Zone C (C_1..C_20)  -> indices 0..19
 *  - Zone A (A_1..A_11)  -> indices 20..30
 *  - Zone B (B_1..B_5)   -> indices 31..35
 */
class GlyphController(private val context: Context) {

    companion object {
        const val TAG = "GlyphController"
        const val LED_COUNT_PHONE_3A = 36

        /** True if this device is any Nothing Phone model supported by the Glyph SDK. */
        fun isNothingDevice(): Boolean =
            Common.is20111() || Common.is22111() || Common.is23111() ||
            Common.is23113() || Common.is24111() || Common.is25111()
    }

    private var manager: GlyphManager? = null
    private var sessionOpen: Boolean = false
    private var onReady: (() -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    private val callback = object : GlyphManager.Callback {
        override fun onServiceConnected(componentName: ComponentName?) {
            Log.d(TAG, "onServiceConnected: $componentName")
            val mgr = manager ?: return
            try {
                val registered = when {
                    Common.is24111() -> mgr.register(Glyph.DEVICE_24111)
                    Common.is23111() -> mgr.register(Glyph.DEVICE_23111)
                    Common.is23113() -> mgr.register(Glyph.DEVICE_23113)
                    Common.is22111() -> mgr.register(Glyph.DEVICE_22111)
                    Common.is20111() -> mgr.register(Glyph.DEVICE_20111)
                    Common.is25111() -> mgr.register(Glyph.DEVICE_25111)
                    else -> mgr.register()
                }
                Log.d(TAG, "register() -> $registered")
                mgr.openSession()
                sessionOpen = true
                onReady?.invoke()
            } catch (e: GlyphException) {
                Log.e(TAG, "openSession failed: ${e.message}")
                onError?.invoke("openSession failed: ${e.message}")
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected: $componentName")
            sessionOpen = false
        }
    }

    fun init(onReady: () -> Unit, onError: (String) -> Unit = {}) {
        this.onReady = onReady
        this.onError = onError
        manager = GlyphManager.getInstance(context).also {
            it.init(callback)
        }
    }

    fun setFrameColors(values: IntArray) {
        val mgr = manager ?: return
        if (!sessionOpen) return
        try {
            mgr.setFrameColors(values)
        } catch (e: GlyphException) {
            Log.e(TAG, "setFrameColors failed: ${e.message}")
        }
    }

    fun turnOff() {
        manager?.turnOff()
    }

    fun release() {
        val mgr = manager ?: return
        try {
            if (sessionOpen) mgr.closeSession()
        } catch (e: GlyphException) {
            Log.e(TAG, "closeSession failed: ${e.message}")
        }
        try {
            mgr.unInit()
        } catch (e: Exception) {
            Log.e(TAG, "unInit failed: ${e.message}")
        }
        sessionOpen = false
        manager = null
    }

    fun isSessionOpen(): Boolean = sessionOpen

    fun deviceName(): String = when {
        Common.is24111() -> "Phone (3a) / (3a) Pro"
        Common.is23111() -> "Phone (2a)"
        Common.is23113() -> "Phone (2a) Plus"
        Common.is22111() -> "Phone (2)"
        Common.is20111() -> "Phone (1)"
        Common.is25111() -> "Phone (4a)"
        else -> "Unknown / non-Nothing device"
    }
}
